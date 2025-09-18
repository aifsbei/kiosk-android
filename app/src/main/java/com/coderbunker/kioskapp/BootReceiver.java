package com.coderbunker.kioskapp;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // not really needed. receives only boot action
        }
        Intent launchIntent = new Intent(context, KioskActivity.class);
        launchIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);
    }
}
