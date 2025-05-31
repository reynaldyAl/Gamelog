package com.example.gamemology.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.gamemology.repository.GameRepository;

public class OfflineManager {

    private static final String TAG = "OfflineManager";
    private static final String PREFS_NAME = "offline_prefs";
    private static final String KEY_LAST_CLEANUP = "last_cache_cleanup";

    // Cleanup cache every 3 days
    private static final long CLEANUP_INTERVAL = 3 * 24 * 60 * 60 * 1000;

    // Singleton instance
    private static OfflineManager instance;

    private final Context context;
    private final SharedPreferences prefs;
    private final GameRepository repository;

    public static synchronized OfflineManager getInstance(Context context) {
        if (instance == null) {
            instance = new OfflineManager(context.getApplicationContext());
        }
        return instance;
    }

    private OfflineManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.repository = new GameRepository(context);
    }

    /**
     * Check if database maintenance is needed and perform cleanup if necessary.
     */
    public void performMaintenance() {
        long now = System.currentTimeMillis();
        long lastCleanup = prefs.getLong(KEY_LAST_CLEANUP, 0);

        if (now - lastCleanup > CLEANUP_INTERVAL) {
            Log.d(TAG, "Performing cache maintenance");

            // Clean up old cache entries
            repository.cleanupOldCache();

            // Update timestamp
            prefs.edit().putLong(KEY_LAST_CLEANUP, now).apply();
        }
    }

    /**
     * Pre-cache commonly accessed data for offline use.
     */
    public void prefetchCommonData() {
        if (NetworkUtils.isNetworkAvailable(context)) {
            Log.d(TAG, "Pre-fetching common data for offline use");

            // Fetch and cache home screen data
            repository.getTrendingGames();

            // Fetch and cache categories
            repository.getGenres();

            // You could add more pre-fetching here
        }
    }

    /**
     * Clear all cached data (e.g., for logout or low storage situations)
     */
    public void clearAllCachedData() {
        // Implementation would depend on your database structure
        // You'd need to add these methods to your DatabaseHelper
    }
}