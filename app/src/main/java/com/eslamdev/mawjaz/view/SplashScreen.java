package com.eslamdev.mawjaz.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.eslamdev.mawjaz.MyApplication;
import com.eslamdev.mawjaz.util.AppOpenAdManager;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.core.splashscreen.SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        MyApplication myApplication = (MyApplication) getApplication();
        AppOpenAdManager appOpenAdManager = myApplication.getAppOpenAdManager();

        appOpenAdManager.showAdIfAvailable(
                this,
                () -> {
                    navigateToMainActivity();
                });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}