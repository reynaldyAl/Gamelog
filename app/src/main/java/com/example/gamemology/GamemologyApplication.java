package com.example.gamemology;

import android.app.Application;

import com.example.gamemology.utils.OfflineManager;

public class GamemologyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize offline support
        OfflineManager offlineManager = OfflineManager.getInstance(this);
        offlineManager.performMaintenance();

        // Pre-fetch data when app starts (if online)
        offlineManager.prefetchCommonData();
    }
}