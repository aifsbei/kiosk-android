package com.stayintouch.kioskapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.stayintouch.kioskapp.config.Configuration;
import com.stayintouch.kioskapp.config.SettingsActivity;


public class KioskActivity extends Activity {

    private final Activity context = this;
    private WebView webView;

    private Dialog passwordDialog;

    private KioskWebViewClient webViewClient;
    private ReloadTimer reloadTimer;

    @Override
    public void onBackPressed() {
        //on back pressed
    }

    @SuppressWarnings("deprecation")
    @SuppressLint({"ClickableViewAccessibility", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        removeTitleBar();
        doNotLockScreen();
        setupLockTask();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);

        // debug only
        // TrafficMonitor trafficMonitor = new TrafficMonitor(this, 1000);
        // trafficMonitor.start();

        setContentView(R.layout.activity_kiosk);


        webView = findViewById(R.id.webview);

        webViewClient = new KioskWebViewClient();
        webView.setWebViewClient(webViewClient);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setAppCacheMaxSize(200 * 1024 * 1024);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        hideSystemUI(webView);

        Configuration.withLocalConfig(context, new Configuration.OnConfigChanged() {
            @Override
            public void OnConfigChanged(Configuration configuration) {
                String url = configuration.getUrl();
                webViewClient.loadConfiguration(configuration);
                webView.loadUrl(url);
                Log.d("KioskActivity", "withLocalConfig: config fetched");

                reloadTimer = new ReloadTimer(context, configuration.getCacheLifetime());
                reloadTimer.start();

                String otp = configuration.getPassphrase();

                if (otp == null) {
                    Intent intent = new Intent(KioskActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        webView.setOnTouchListener(new View.OnTouchListener() {

            private long lastTouchTime;
            private int count;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (MotionEvent.ACTION_UP == motionEvent.getAction()) {
                    long touchTime = System.currentTimeMillis();
                    if (touchTime - lastTouchTime >= 400) {
                        count = 0;
                    }
                    count++;
                    if (count >= 4) {
                        askPassword();
                        count = 0;
                    }
                    lastTouchTime = touchTime;
                }
                return false;
            }
        });

        passwordDialog = new PasswordDialog(this, new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(KioskActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupLockTask() {
        ComponentName mAdminComponentName = KioskDeviceAdminReceiver.getComponentName(this);
        DevicePolicyManager mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
        intentFilter.addCategory(Intent.CATEGORY_HOME);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mDevicePolicyManager.addPersistentPreferredActivity(
                    mAdminComponentName,
                    intentFilter,
                    new ComponentName(getPackageName(), KioskActivity.class.getName())
            );
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String packageName = context.getPackageName();
            mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, new String[] { packageName });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, true);
            }
            mDevicePolicyManager.setGlobalSetting(
                    mAdminComponentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    String.valueOf(
                            (BatteryManager.BATTERY_PLUGGED_AC
                                    | BatteryManager.BATTERY_PLUGGED_USB
                                    | BatteryManager.BATTERY_PLUGGED_WIRELESS)
                    )
            );
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().getDecorView().setSystemUiVisibility(
                    (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            );
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
            startLockTask();
            KioskApplication.isTaskLocked = true;
        }
    }

    private void doNotLockScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    private void removeTitleBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }


    // This snippet hides the system bars.
    private void hideSystemUI(View view) {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        view.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideSystemUI(webView);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return blockKeys(keyCode, event);
    }

    private boolean blockKeys(int keyCode, KeyEvent event) {
        return event.isSystem();
    }

    private void askPassword() {
        passwordDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        passwordDialog.dismiss();
        reloadTimer.stop();
        webViewClient.dispose();
        super.onDestroy();
    }
}
