package com.example.gamemology.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.gamemology.ui.MainActivity; // Correct package import
import com.example.gamemology.utils.ThemeUtils;

public class SplashActivity extends AppCompatActivity {

    // Splash display duration in milliseconds
    private static final long SPLASH_DISPLAY_DURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle the splash screen transition
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Apply saved theme preferences
        ThemeUtils.applyTheme(this);

        // Keep the splash screen visible until we navigate away
        splashScreen.setKeepOnScreenCondition(() -> true);

        // Optional: Add a delay before moving to MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Create intent for MainActivity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);

            // Close this activity
            finish();
        }, SPLASH_DISPLAY_DURATION);
    }
}