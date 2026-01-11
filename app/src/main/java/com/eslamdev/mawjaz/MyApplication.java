package com.eslamdev.mawjaz;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.preference.PreferenceManager;

import com.eslamdev.mawjaz.util.AppOpenAdManager;
import com.google.android.gms.ads.MobileAds;

import java.util.Locale;

public class MyApplication extends Application implements SharedPreferences.OnSharedPreferenceChangeListener, LifecycleObserver {

    private AppOpenAdManager appOpenAdManager;

    @Override
    public void onCreate() {
        super.onCreate();

        appOpenAdManager = new AppOpenAdManager();

        MobileAds.initialize(this, initializationStatus -> {});

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        applyTheme(sharedPreferences.getString("theme_preference", "system"));
        applyLanguage(sharedPreferences.getString("language_preference", "system"));
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected void onMoveToForeground() {
        // The AppOpenAdManager will handle showing the ad.
        // For this to work best, the ad should be shown from the current activity.
        // This setup prepares the ad to be shown from the SplashScreen.
    }

    public AppOpenAdManager getAppOpenAdManager() {
        return appOpenAdManager;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("theme_preference".equals(key)) {
            applyTheme(sharedPreferences.getString(key, "system"));
        }
        if ("language_preference".equals(key)) {

        }
    }

    private void applyTheme(String themeValue) {
        switch (themeValue) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private void applyLanguage(String langValue) {
        if ("system".equals(langValue)) {
            return;
        }
        Locale locale = new Locale(langValue);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}