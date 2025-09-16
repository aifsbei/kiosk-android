package com.coderbunker.kioskapp;


import android.app.Application;
import android.content.Context;

public class KioskApplication extends Application {
    private static KioskApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }
}