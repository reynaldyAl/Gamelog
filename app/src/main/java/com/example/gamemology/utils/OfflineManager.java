package com.example.gamemology.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.gamemology.database.DatabaseHelper;
import com.example.gamemology.database.GameDatabaseContract;
import com.example.gamemology.models.User;
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
    private final DatabaseHelper dbHelper;

    public static synchronized OfflineManager getInstance(Context context) {
        if (instance == null) {
            instance = new OfflineManager(context.getApplicationContext());
        }
        return instance;
    }

    private OfflineManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Check if database maintenance is needed and perform cleanup if necessary.
     */
    public void performMaintenance() {
        long now = System.currentTimeMillis();
        long lastCleanup = prefs.getLong(KEY_LAST_CLEANUP, 0);

        if (now - lastCleanup > CLEANUP_INTERVAL) {
            Log.d(TAG, "Performing cache maintenance");

            // Get current user
            User currentUser = SessionManager.getInstance(context).getUser();
            int userId = currentUser != null ? currentUser.getId() : 1;

            // Clean up old cache entries for the current user
            dbHelper.cleanupOldCache(userId);

            // Update timestamp
            prefs.edit().putLong(KEY_LAST_CLEANUP, now).apply();
        }
    }

    /**
     * Pre-cache commonly accessed data for offline use.
     */
    public void prefetchCommonData() {
        // Only prefetch if user is logged in
        User currentUser = SessionManager.getInstance(context).getUser();
        if (currentUser == null) {
            return;
        }

        if (NetworkUtils.isNetworkAvailable(context)) {
            Log.d(TAG, "Pre-fetching common data for offline use");

            // Prefetch using the repository (which will handle proper user context)
            GameRepository repository = new GameRepository(context);
            repository.getTrendingGames();
            repository.getPopularGames();
            repository.getGenres();
            repository.getPlatforms();
        }
    }

    /**
     * Clear all cached data (e.g., for low storage situations)
     * This clears data for all users
     */
    public void clearAllCachedData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            db.delete(GameDatabaseContract.CachedGamesEntry.TABLE_NAME, null, null);
            db.delete(GameDatabaseContract.GameDetailsEntry.TABLE_NAME, null, null);
            db.delete(GameDatabaseContract.CategoryEntry.TABLE_NAME, null, null);
            db.delete(GameDatabaseContract.SyncInfoEntry.TABLE_NAME, null, null);

            db.setTransactionSuccessful();
            Log.d(TAG, "All cached data cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing cache", e);
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }
    }

    /**
     * Clear cached data for a specific user
     */
    public void clearUserCache(int userId) {
        if (userId <= 0) {
            Log.e(TAG, "Invalid user ID for cache clearing");
            return;
        }

        dbHelper.clearUserCache(userId);
        Log.d(TAG, "User-specific cache cleared for user ID: " + userId);
    }
}