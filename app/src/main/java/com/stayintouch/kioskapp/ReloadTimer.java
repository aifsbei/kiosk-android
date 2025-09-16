package com.stayintouch.kioskapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

public class ReloadTimer {

    private final Handler handler = new Handler();
    private final long intervalMillis;
    private final Runnable reloadTask;

    public ReloadTimer(final Activity activity, long intervalMillis) {
        this.intervalMillis = intervalMillis;

        this.reloadTask = new Runnable() {
            @Override
            public void run() {
                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    Intent reloadIntent = new Intent(activity, KioskActivity.class);
                    reloadIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.finish();
                    activity.startActivity(reloadIntent);
                    scheduleNext();
                }
            }
        };
    }

    public void start() {
        stop();
        scheduleNext();
    }

    public void stop() {
        handler.removeCallbacks(reloadTask);
    }

    private void scheduleNext() {
        handler.postDelayed(reloadTask, intervalMillis);
    }
}
