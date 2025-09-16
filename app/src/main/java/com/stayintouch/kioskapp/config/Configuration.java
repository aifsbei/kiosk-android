package com.stayintouch.kioskapp.config;

import android.content.Context;
import android.content.SharedPreferences;

public class Configuration {
    private SharedPreferences preferences;
    private static final String NAME = "com.stayintouch.kioskapp";
    private String url;
    private String passphrase;
    private long cacheLifetime;
    private long retryDelay;

    public Configuration(Context context, String url, String passphrase, long cacheLifetime, long retryDelay) {
        preferences = context.getSharedPreferences(
                NAME, Context.MODE_PRIVATE);
        setUrl(url);
        setPassphrase(passphrase);
        setCacheLifetime(cacheLifetime);
        setRetryDelay(retryDelay);
    }

    private Configuration(Context context) {
        preferences = context.getSharedPreferences(
                NAME, Context.MODE_PRIVATE);
        url = preferences.getString("url", "https://adv.stayintouch.ru/index.html?video=1");
        passphrase = preferences.getString("passphrase", null); //TODO change def value
        cacheLifetime = preferences.getLong("cacheLifetime", 12 * 60 * 60 * 1000L); // 12 hours
        retryDelay = preferences.getLong("retryDelay", 90 * 1000L); // 90 seconds
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

    public long getRetryDelay() {
        return retryDelay;
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

    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
        set("retryDelay", retryDelay);
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
        private long retryDelay;

        public void setUrl(String url) {
            this.url = url;
        }

        public void setPassphrase(String passphrase) {
            this.passphrase = passphrase;
        }

        public void setCacheLifetime(long cacheLifetime) {
            this.cacheLifetime = cacheLifetime;
        }

        public void setRetryDelay(long retryDelay) {
            this.retryDelay = retryDelay;
        }

        public Configuration build(Context context) {
            return new Configuration(context, url, passphrase, cacheLifetime, retryDelay);
        }
    }

    public interface OnConfigChanged {
        void OnConfigChanged(Configuration configuration);
    }
}
