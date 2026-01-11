package com.eslamdev.mawjaz.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

public class AppOpenAdManager {

    private static final String LOG_TAG = "AppOpenAdManager";
    private static final String AD_UNIT_ID = "ca-app-pub-6321231117080513/3427813307";

    private AppOpenAd appOpenAd = null;
    private boolean isLoadingAd = false;
    private boolean isShowingAd = false;

    public interface OnShowAdCompleteListener {
        void onShowAdComplete();
    }

    public AppOpenAdManager() {}

    public void loadAd(Context context) {
        if (isLoadingAd || isAdAvailable()) {
            return;
        }

        isLoadingAd = true;
        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(
                context,
                AD_UNIT_ID,
                request,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        appOpenAd = ad;
                        isLoadingAd = false;
                        Log.d(LOG_TAG, "App Open Ad was loaded.");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        isLoadingAd = false;
                        Log.e(LOG_TAG, "App Open Ad failed to load: " + loadAdError.getMessage());
                    }
                });
    }

    public boolean isAdAvailable() {
        return appOpenAd != null;
    }

    public void showAdIfAvailable(
            @NonNull final Activity activity,
            @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {

        if (isShowingAd) {
            Log.d(LOG_TAG, "The app open ad is already showing.");
            return;
        }

        if (!isAdAvailable()) {
            Log.d(LOG_TAG, "The app open ad is not ready yet.");
            onShowAdCompleteListener.onShowAdComplete();
            loadAd(activity);
            return;
        }

        appOpenAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        appOpenAd = null;
                        isShowingAd = false;
                        Log.d(LOG_TAG, "Ad dismissed fullscreen content.");
                        onShowAdCompleteListener.onShowAdComplete();
                        loadAd(activity);
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        appOpenAd = null;
                        isShowingAd = false;
                        Log.e(LOG_TAG, "Ad failed to show fullscreen content: " + adError.getMessage());
                        onShowAdCompleteListener.onShowAdComplete();
                        loadAd(activity);
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(LOG_TAG, "Ad showed fullscreen content.");
                    }
                });

        isShowingAd = true;
        appOpenAd.show(activity);
    }
}