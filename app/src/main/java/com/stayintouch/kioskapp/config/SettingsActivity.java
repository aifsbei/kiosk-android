package com.stayintouch.kioskapp.config;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.stayintouch.kioskapp.KioskApplication;
import com.stayintouch.kioskapp.MainActivity;
import com.stayintouch.kioskapp.R;
import com.stayintouch.kioskapp.lib.TimeUtils;

import static android.widget.Toast.LENGTH_LONG;

import java.io.File;

public class SettingsActivity extends Activity {

    private Context context = this;
    private EditText editURL;
    private Button btnSave;

    private Configuration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        editURL = findViewById(R.id.editText_URL);
        btnSave = findViewById(R.id.btnSave);
        Button btnToggleKioskMode = findViewById(R.id.toggleKioskModeBtn);
        Button btnClearCache = findViewById(R.id.clearCacheBtn);
        final EditText cacheLifetimeInputHours = findViewById(R.id.cacheLifetimeInputHours);
        final EditText cacheLifetimeInputMinutes = findViewById(R.id.cacheLifetimeInputMinutes);

        cacheLifetimeInputHours.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void afterTextChanged(Editable editable) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        updateCacheLifetime(cacheLifetimeInputMinutes.getText().toString(), charSequence.toString());
                    }
                }
        );

        cacheLifetimeInputMinutes.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void afterTextChanged(Editable editable) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        updateCacheLifetime(charSequence.toString(), cacheLifetimeInputHours.getText().toString());
                    }
                }
        );

        Button claimTabletBtn = findViewById(R.id.claimTabletBtn);
        claimTabletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ClaimDialog(SettingsActivity.this).show();
            }
        });

        btnToggleKioskMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (KioskApplication.isTaskLocked) {
                        stopLockTask();
                        Toast.makeText(context, "Task unlocked", Toast.LENGTH_SHORT).show();
                    } else {
                        startLockTask();
                        Toast.makeText(context, "Task locked", Toast.LENGTH_SHORT).show();
                    }
                    KioskApplication.isTaskLocked = !KioskApplication.isTaskLocked;
                }
            }
        });

        btnClearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(context.getFilesDir(), "Video");
                FileUtils.deleteRecursive(file);
                Toast.makeText(context, "Cache cleared!", Toast.LENGTH_SHORT).show();
            }
        });

        Configuration.withLocalConfig(this, new Configuration.OnConfigChanged() {
            @Override
            public void OnConfigChanged(final Configuration configuration) {
                SettingsActivity.this.configuration = configuration;
                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = editURL.getText().toString();

                        if (!url.isEmpty() && URLUtil.isValidUrl(url)) {
                            configuration.setUrl(url);
                            Toast.makeText(context, "Changes saved!", LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, "Invalid URL!", LENGTH_LONG).show();
                        }
                    }
                });

                String otp = configuration.getPassphrase();
                String url = configuration.getUrl();

                editURL.setText(url);

                int[] hoursAndMinutes = TimeUtils.getHoursAndMinutesFromMillis(configuration.getCacheLifetime());
                cacheLifetimeInputHours.setText(String.valueOf(hoursAndMinutes[0]));
                cacheLifetimeInputMinutes.setText(String.valueOf(hoursAndMinutes[1]));

                if (otp == null) {
                    otp = new ConfigEncrypter().hashPassphrase("123456");
                    configuration.setPassphrase(otp);
                }
            }
        });
    }

    private void updateCacheLifetime(String minutes, String hours) {
        try {
            int hoursValue = hours.isEmpty() ? 0 : Integer.parseInt(hours);
            int minutesValue = minutes.isEmpty() ? 0 : Integer.parseInt(minutes);
            configuration.setCacheLifetime((hoursValue * 60L + minutesValue) * 60L * 1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
