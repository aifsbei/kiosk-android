package com.stayintouch.kioskapp;

import android.app.admin.DeviceAdminReceiver;
import android.content.ComponentName;
import android.content.Context;

public class KioskDeviceAdminReceiver extends DeviceAdminReceiver {

    static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), KioskDeviceAdminReceiver.class);
    }
}