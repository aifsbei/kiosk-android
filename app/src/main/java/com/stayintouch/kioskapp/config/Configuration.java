package com.stayintouch.kioskapp.config;

import android.content.Context;
import android.content.SharedPreferences;

public class Configuration {
    private SharedPreferences preferences;
    private static final String NAME = "com.stayintouch.kioskapp";
    private String url;
    private String passphrase;

    public Configuration(Context context, String url, String passphrase) {
        preferences = context.getSharedPreferences(
                NAME, Context.MODE_PRIVATE);
        setUrl(url);
        setPassphrase(passphrase);
    }

    private Configuration(Context context) {
        preferences = context.getSharedPreferences(
                NAME, Context.MODE_PRIVATE);
        url = preferences.getString("url", "http://f1169204.xsph.ru/index.html?video=1");
        passphrase = preferences.getString("passphrase", null); //TODO change def value
    }

    public String getUrl() {
        return url;
    }

    public String getPassphrase() {
        return passphrase;
    }

    private void set(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    private void set(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    public void setUrl(String url) {
        this.url = url;
        set("url", url);
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
        set("passphrase", passphrase);
    }

    public static Configuration loadFromPreferences(Context context) {
        return new Configuration(context);
    }

    public static void withLocalConfig(Context context, OnConfigChanged onConfigChanged) {
        Configuration localConfig = loadFromPreferences(context);
        onConfigChanged.OnConfigChanged(localConfig);
    }

    public static class ConfigurationBuilder {
        private String url;
        private String passphrase;

        public void setUrl(String url) {
            this.url = url;
        }

        public void setPassphrase(String passphrase) {
            this.passphrase = passphrase;
        }

        public Configuration build(Context context) {
            return new Configuration(context, url, passphrase);
        }
    }

    public interface OnConfigChanged {
        void OnConfigChanged(Configuration configuration);
    }
}
