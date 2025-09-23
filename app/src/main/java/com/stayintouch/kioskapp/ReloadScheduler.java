package com.stayintouch.kioskapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class ReloadScheduler {

    private static final String ACTION_RELOAD_ACTIVITY = "com.example.RELOAD_ACTIVITY";

    private BroadcastReceiver reloadReceiver;

    public void scheduleReload(Activity activity, long intervalMillis) {
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(activity, ReloadReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                activity,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        long triggerAt = System.currentTimeMillis() + intervalMillis;

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                intervalMillis,
                pendingIntent
        );
        Log.d("ReloadScheduler", "scheduleReload: scheduled for " + String.valueOf(intervalMillis));
    }

    public void registerActivityReload(final Activity activity) {
        Log.d("ReloadScheduler", "registerActivityReload: registered");
        reloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                if (ACTION_RELOAD_ACTIVITY.equals(intent.getAction())) {
                    Intent reloadIntent = new Intent(activity, KioskActivity.class);
                    reloadIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.finish();
                    activity.startActivity(reloadIntent);
                    Log.d("ReloadScheduler", "registerActivityReload: activity reloaded");
                }
            }
        };
        activity.registerReceiver(reloadReceiver, new IntentFilter(ACTION_RELOAD_ACTIVITY));
    }

    public void unregister(Context context) {
        Log.d("ReloadScheduler", "unregister: unregistered");
        if (reloadReceiver != null) {
            context.unregisterReceiver(reloadReceiver);
            reloadReceiver = null;
        }
    }

    public static class ReloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("ReloadScheduler", "onReceive: ReloadReceiver received and send broadcast now");
            context.sendBroadcast(new Intent(ACTION_RELOAD_ACTIVITY));
        }
    }
}
