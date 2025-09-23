package com.stayintouch.kioskapp.config;

import android.content.Context;
import android.content.SharedPreferences;

public class Configuration {
    private SharedPreferences preferences;
    private static final String NAME = "com.stayintouch.kioskapp";
    private String url;
    private String passphrase;
    private long cacheLifetime;

    public Configuration(Context context, String url, String passphrase, long cacheLifetime) {
        preferences = context.getSharedPreferences(
                NAME, Context.MODE_PRIVATE);
        setUrl(url);
        setPassphrase(passphrase);
        setCacheLifetime(cacheLifetime);
    }

    private Configuration(Context context) {
        preferences = context.getSharedPreferences(
                NAME, Context.MODE_PRIVATE);
        url = preferences.getString("url", "http://f1169204.xsph.ru/index.html?video=1");
        passphrase = preferences.getString("passphrase", null); //TODO change def value
        cacheLifetime = preferences.getLong("cacheLifetime", 12 * 60 * 60 * 1000L); // 12 hours
    }

    public String getUrl() {
        return url;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public long getCacheLifetime() {
        return cacheLifetime;
    }

    private void set(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    private void set(String key, long value) {
        preferences.edit().putLong(key, value).apply();
    }

    public void setUrl(String url) {
        this.url = url;
        set("url", url);
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
        set("passphrase", passphrase);
    }

    public void setCacheLifetime(long cacheLifetime) {
        this.cacheLifetime = cacheLifetime;
        set("cacheLifetime", cacheLifetime);
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
        private long cacheLifetime;

        public void setUrl(String url) {
            this.url = url;
        }

        public void setPassphrase(String passphrase) {
            this.passphrase = passphrase;
        }

        public void setCacheLifetime(long cacheLifetime) {
            this.cacheLifetime = cacheLifetime;
        }

        public Configuration build(Context context) {
            return new Configuration(context, url, passphrase, cacheLifetime);
        }
    }

    public interface OnConfigChanged {
        void OnConfigChanged(Configuration configuration);
    }
}
