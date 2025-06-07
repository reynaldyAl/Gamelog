package com.example.gamemology;

import android.app.Application;
import android.util.Log;

import com.example.gamemology.database.DatabaseHelper;
import com.example.gamemology.utils.OfflineManager;

public class GamemologyApplication extends Application {
    private static final String TAG = "GamemologyApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize database and ensure all tables exist
        try {
            Log.d(TAG, "Initializing database tables...");
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            dbHelper.ensureTablesExist();
            dbHelper.logDatabaseStatus(); // Log database tables for debugging
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database", e);
        }

        // Initialize offline support
        OfflineManager offlineManager = OfflineManager.getInstance(this);
        offlineManager.performMaintenance();

        // Pre-fetch data when app starts (if online)
        offlineManager.prefetchCommonData();
    }
}