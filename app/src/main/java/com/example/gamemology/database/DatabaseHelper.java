package com.example.gamemology.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.gamemology.api.responses.GameListResponse;
import com.example.gamemology.api.responses.GameResponse;
import com.example.gamemology.api.responses.GenreResponse;
import com.example.gamemology.api.responses.PlatformResponse;
import com.example.gamemology.models.Game;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Informasi database
    private static final String DATABASE_NAME = "gamemology.db";
    private static final int DATABASE_VERSION = 2; // Increased version for schema changes

    // Cache expiration time (in milliseconds)
    private static final long CACHE_EXPIRY_HOME = 24 * 60 * 60 * 1000; // 24 hours for home data
    private static final long CACHE_EXPIRY_DETAILS = 7 * 24 * 60 * 60 * 1000; // 7 days for game details
    private static final long CACHE_EXPIRY_CATEGORIES = 7 * 24 * 60 * 60 * 1000; // 7 days for categories

    // Singleton instance
    private static DatabaseHelper instance;
    private final Gson gson = new Gson();

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Favorites table
        final String SQL_CREATE_GAME_TABLE = "CREATE TABLE " +
                GameDatabaseContract.GameEntry.TABLE_NAME + " (" +
                GameDatabaseContract.GameEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                GameDatabaseContract.GameEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                GameDatabaseContract.GameEntry.COLUMN_RELEASED + " TEXT, " +
                GameDatabaseContract.GameEntry.COLUMN_BACKGROUND_IMAGE + " TEXT, " +
                GameDatabaseContract.GameEntry.COLUMN_RATING + " REAL, " +
                GameDatabaseContract.GameEntry.COLUMN_DESCRIPTION + " TEXT" +
                ");";

        // Cached games table for home screen
        final String SQL_CREATE_CACHED_GAMES_TABLE = "CREATE TABLE " +
                GameDatabaseContract.CachedGamesEntry.TABLE_NAME + " (" +
                GameDatabaseContract.CachedGamesEntry.COLUMN_ID + " INTEGER, " +
                GameDatabaseContract.CachedGamesEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                GameDatabaseContract.CachedGamesEntry.COLUMN_RELEASED + " TEXT, " +
                GameDatabaseContract.CachedGamesEntry.COLUMN_BACKGROUND_IMAGE + " TEXT, " +
                GameDatabaseContract.CachedGamesEntry.COLUMN_RATING + " REAL, " +
                GameDatabaseContract.CachedGamesEntry.COLUMN_CATEGORY + " TEXT NOT NULL, " +
                GameDatabaseContract.CachedGamesEntry.COLUMN_TIMESTAMP + " INTEGER, " +
                GameDatabaseContract.CachedGamesEntry.COLUMN_PAGE + " INTEGER, " +
                "PRIMARY KEY (" + GameDatabaseContract.CachedGamesEntry.COLUMN_ID + ", " +
                GameDatabaseContract.CachedGamesEntry.COLUMN_CATEGORY + ", " +
                GameDatabaseContract.CachedGamesEntry.COLUMN_PAGE + ")" +
                ");";

        // Game details table
        final String SQL_CREATE_GAME_DETAILS_TABLE = "CREATE TABLE " +
                GameDatabaseContract.GameDetailsEntry.TABLE_NAME + " (" +
                GameDatabaseContract.GameDetailsEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                GameDatabaseContract.GameDetailsEntry.COLUMN_FULL_DATA + " TEXT NOT NULL, " +
                GameDatabaseContract.GameDetailsEntry.COLUMN_TIMESTAMP + " INTEGER" +
                ");";

        // Categories table (genres, platforms, publishers)
        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " +
                GameDatabaseContract.CategoryEntry.TABLE_NAME + " (" +
                GameDatabaseContract.CategoryEntry.COLUMN_ID + " INTEGER, " +
                GameDatabaseContract.CategoryEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                GameDatabaseContract.CategoryEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                GameDatabaseContract.CategoryEntry.COLUMN_IMAGE + " TEXT, " +
                GameDatabaseContract.CategoryEntry.COLUMN_GAMES_COUNT + " INTEGER, " +
                GameDatabaseContract.CategoryEntry.COLUMN_TIMESTAMP + " INTEGER, " +
                "PRIMARY KEY (" + GameDatabaseContract.CategoryEntry.COLUMN_ID + ", " +
                GameDatabaseContract.CategoryEntry.COLUMN_TYPE + ")" +
                ");";

        // Sync info table
        final String SQL_CREATE_SYNC_INFO_TABLE = "CREATE TABLE " +
                GameDatabaseContract.SyncInfoEntry.TABLE_NAME + " (" +
                GameDatabaseContract.SyncInfoEntry.COLUMN_TYPE + " TEXT PRIMARY KEY, " +
                GameDatabaseContract.SyncInfoEntry.COLUMN_LAST_SYNC + " INTEGER" +
                ");";

        db.execSQL(SQL_CREATE_GAME_TABLE);
        db.execSQL(SQL_CREATE_CACHED_GAMES_TABLE);
        db.execSQL(SQL_CREATE_GAME_DETAILS_TABLE);
        db.execSQL(SQL_CREATE_CATEGORY_TABLE);
        db.execSQL(SQL_CREATE_SYNC_INFO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For simplicity in development, drop tables and recreate
        // In production, you'd want to do proper schema migration
        db.execSQL("DROP TABLE IF EXISTS " + GameDatabaseContract.GameEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GameDatabaseContract.CachedGamesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GameDatabaseContract.GameDetailsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GameDatabaseContract.CategoryEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GameDatabaseContract.SyncInfoEntry.TABLE_NAME);
        onCreate(db);
    }

    // --- FAVORITES METHODS (EXISTING) ---

    // Insert game to favorites
    public long addGameToFavorites(Game game) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(GameDatabaseContract.GameEntry.COLUMN_ID, game.getId());
        values.put(GameDatabaseContract.GameEntry.COLUMN_NAME, game.getName());
        values.put(GameDatabaseContract.GameEntry.COLUMN_RELEASED, game.getReleased());
        values.put(GameDatabaseContract.GameEntry.COLUMN_BACKGROUND_IMAGE, game.getBackgroundImage());
        values.put(GameDatabaseContract.GameEntry.COLUMN_RATING, game.getRating());
        values.put(GameDatabaseContract.GameEntry.COLUMN_DESCRIPTION, game.getDescription());

        return db.insert(GameDatabaseContract.GameEntry.TABLE_NAME, null, values);
    }

    // Remove game from favorites
    public int removeGameFromFavorites(int gameId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(GameDatabaseContract.GameEntry.TABLE_NAME,
                GameDatabaseContract.GameEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(gameId)});
    }

    // Check if game is favorite
    public boolean isGameFavorite(int gameId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                GameDatabaseContract.GameEntry.TABLE_NAME,
                null,
                GameDatabaseContract.GameEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(gameId)},
                null,
                null,
                null
        );
        boolean isFavorite = cursor.getCount() > 0;
        cursor.close();
        return isFavorite;
    }

    // Get all favorite games
    public List<Game> getAllFavoriteGames() {
        List<Game> favoriteGames = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                GameDatabaseContract.GameEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                GameDatabaseContract.GameEntry.COLUMN_NAME + " ASC"
        );

        if (cursor.moveToFirst()) {
            do {
                Game game = new Game();
                game.setId(cursor.getInt(cursor.getColumnIndexOrThrow(GameDatabaseContract.GameEntry.COLUMN_ID)));
                game.setName(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.GameEntry.COLUMN_NAME)));
                game.setReleased(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.GameEntry.COLUMN_RELEASED)));
                game.setBackgroundImage(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.GameEntry.COLUMN_BACKGROUND_IMAGE)));
                game.setRating(cursor.getDouble(cursor.getColumnIndexOrThrow(GameDatabaseContract.GameEntry.COLUMN_RATING)));
                game.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.GameEntry.COLUMN_DESCRIPTION)));
                game.setFavorite(true);

                favoriteGames.add(game);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return favoriteGames;
    }

    // --- NEW CACHING METHODS FOR OFFLINE MODE ---

    // Save games for a specific category to the cache (e.g., "trending", "popular")
    public void cacheGames(List<GameResponse> games, String category, int page) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            // If it's page 1, clear existing cache for this category to avoid duplicates
            if (page == 1) {
                db.delete(GameDatabaseContract.CachedGamesEntry.TABLE_NAME,
                        GameDatabaseContract.CachedGamesEntry.COLUMN_CATEGORY + " = ?",
                        new String[]{category});
            }

            long now = System.currentTimeMillis();

            for (GameResponse game : games) {
                ContentValues values = new ContentValues();
                values.put(GameDatabaseContract.CachedGamesEntry.COLUMN_ID, game.getId());
                values.put(GameDatabaseContract.CachedGamesEntry.COLUMN_NAME, game.getName());
                values.put(GameDatabaseContract.CachedGamesEntry.COLUMN_RELEASED, game.getReleased());
                values.put(GameDatabaseContract.CachedGamesEntry.COLUMN_BACKGROUND_IMAGE, game.getBackgroundImage());
                values.put(GameDatabaseContract.CachedGamesEntry.COLUMN_RATING, game.getRating());
                values.put(GameDatabaseContract.CachedGamesEntry.COLUMN_CATEGORY, category);
                values.put(GameDatabaseContract.CachedGamesEntry.COLUMN_TIMESTAMP, now);
                values.put(GameDatabaseContract.CachedGamesEntry.COLUMN_PAGE, page);

                db.insertWithOnConflict(
                        GameDatabaseContract.CachedGamesEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                );
            }

            // Update sync timestamp
            updateSyncTimestamp(category + "_games", now);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error caching games: ", e);
        } finally {
            db.endTransaction();
        }
    }

    // Get cached games for a category (e.g., for home screen)
    public List<Game> getCachedGames(String category, int page) {
        List<Game> games = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                GameDatabaseContract.CachedGamesEntry.TABLE_NAME,
                null,
                GameDatabaseContract.CachedGamesEntry.COLUMN_CATEGORY + " = ? AND " +
                        GameDatabaseContract.CachedGamesEntry.COLUMN_PAGE + " = ?",
                new String[]{category, String.valueOf(page)},
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                Game game = new Game();
                game.setId(cursor.getInt(cursor.getColumnIndexOrThrow(GameDatabaseContract.CachedGamesEntry.COLUMN_ID)));
                game.setName(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.CachedGamesEntry.COLUMN_NAME)));
                game.setReleased(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.CachedGamesEntry.COLUMN_RELEASED)));
                game.setBackgroundImage(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.CachedGamesEntry.COLUMN_BACKGROUND_IMAGE)));
                game.setRating(cursor.getDouble(cursor.getColumnIndexOrThrow(GameDatabaseContract.CachedGamesEntry.COLUMN_RATING)));
                game.setFavorite(isGameFavorite(game.getId()));

                games.add(game);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return games;
    }

    // Check if cache for a category needs refreshing
    public boolean isCacheExpired(String type) {
        SQLiteDatabase db = getReadableDatabase();

        long expiryTime;
        switch (type) {
            case "home_trending":
            case "home_popular":
                expiryTime = CACHE_EXPIRY_HOME;
                break;
            case "game_details":
                expiryTime = CACHE_EXPIRY_DETAILS;
                break;
            default:
                expiryTime = CACHE_EXPIRY_CATEGORIES;
        }

        long lastSync = getLastSyncTimestamp(type);
        return (System.currentTimeMillis() - lastSync) > expiryTime;
    }

    // Cache game details for offline viewing
    public void cacheGameDetails(int gameId, String gameDetailsJson) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(GameDatabaseContract.GameDetailsEntry.COLUMN_ID, gameId);
        values.put(GameDatabaseContract.GameDetailsEntry.COLUMN_FULL_DATA, gameDetailsJson);
        values.put(GameDatabaseContract.GameDetailsEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());

        db.insertWithOnConflict(
                GameDatabaseContract.GameDetailsEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    // Get cached game details
    public String getCachedGameDetails(int gameId) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                GameDatabaseContract.GameDetailsEntry.TABLE_NAME,
                new String[]{GameDatabaseContract.GameDetailsEntry.COLUMN_FULL_DATA},
                GameDatabaseContract.GameDetailsEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(gameId)},
                null,
                null,
                null
        );

        String gameDetailsJson = null;

        if (cursor.moveToFirst()) {
            gameDetailsJson = cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.GameDetailsEntry.COLUMN_FULL_DATA));
        }

        cursor.close();
        return gameDetailsJson;
    }

    // Cache category items (genres, platforms, etc)
    public void cacheCategories(List<?> items, String categoryType) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            // Clear existing cached items of this type
            db.delete(
                    GameDatabaseContract.CategoryEntry.TABLE_NAME,
                    GameDatabaseContract.CategoryEntry.COLUMN_TYPE + " = ?",
                    new String[]{categoryType}
            );

            long now = System.currentTimeMillis();

            // Insert new items based on type
            for (Object item : items) {
                ContentValues values = new ContentValues();

                // Handle different category types
                if (categoryType.equals("genre") && item instanceof GenreResponse.Genre) {
                    GenreResponse.Genre genre = (GenreResponse.Genre) item;
                    values.put(GameDatabaseContract.CategoryEntry.COLUMN_ID, genre.getId());
                    values.put(GameDatabaseContract.CategoryEntry.COLUMN_NAME, genre.getName());
                    values.put(GameDatabaseContract.CategoryEntry.COLUMN_TYPE, categoryType);
                    values.put(GameDatabaseContract.CategoryEntry.COLUMN_IMAGE, genre.getImageBackground());
                    values.put(GameDatabaseContract.CategoryEntry.COLUMN_GAMES_COUNT, genre.getGamesCount());

                } else if (categoryType.equals("platform") && item instanceof PlatformResponse.Platform) {
                    PlatformResponse.Platform platform = (PlatformResponse.Platform) item;
                    values.put(GameDatabaseContract.CategoryEntry.COLUMN_ID, platform.getId());
                    values.put(GameDatabaseContract.CategoryEntry.COLUMN_NAME, platform.getName());
                    values.put(GameDatabaseContract.CategoryEntry.COLUMN_TYPE, categoryType);
                    values.put(GameDatabaseContract.CategoryEntry.COLUMN_IMAGE, platform.getImageBackground());
                    values.put(GameDatabaseContract.CategoryEntry.COLUMN_GAMES_COUNT, platform.getGamesCount());

                } // Add other types as needed (publishers, stores)

                values.put(GameDatabaseContract.CategoryEntry.COLUMN_TIMESTAMP, now);

                db.insert(GameDatabaseContract.CategoryEntry.TABLE_NAME, null, values);
            }

            // Update sync timestamp
            updateSyncTimestamp(categoryType, now);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error caching categories: ", e);
        } finally {
            db.endTransaction();
        }
    }

    // Get cached categories by type
    public <T> List<T> getCachedCategories(String categoryType, Class<T> categoryClass) {
        List<T> categories = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                GameDatabaseContract.CategoryEntry.TABLE_NAME,
                null,
                GameDatabaseContract.CategoryEntry.COLUMN_TYPE + " = ?",
                new String[]{categoryType},
                null,
                null,
                GameDatabaseContract.CategoryEntry.COLUMN_NAME + " ASC"
        );

        if (cursor.moveToFirst()) {
            do {
                try {
                    // Convert DB data to appropriate object
                    // This is simplified - in reality you might need a more complex approach
                    String json = "{" +
                            "\"id\":" + cursor.getInt(cursor.getColumnIndexOrThrow(GameDatabaseContract.CategoryEntry.COLUMN_ID)) + "," +
                            "\"name\":\"" + cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.CategoryEntry.COLUMN_NAME)) + "\"," +
                            "\"image_background\":\"" + cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.CategoryEntry.COLUMN_IMAGE)) + "\"," +
                            "\"games_count\":" + cursor.getInt(cursor.getColumnIndexOrThrow(GameDatabaseContract.CategoryEntry.COLUMN_GAMES_COUNT)) +
                            "}";

                    T category = gson.fromJson(json, categoryClass);
                    categories.add(category);
                } catch (Exception e) {
                    Log.e(TAG, "Error deserializing category from cache: ", e);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return categories;
    }

    // Update the last sync timestamp for a data type
    private void updateSyncTimestamp(String type, long timestamp) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(GameDatabaseContract.SyncInfoEntry.COLUMN_TYPE, type);
        values.put(GameDatabaseContract.SyncInfoEntry.COLUMN_LAST_SYNC, timestamp);

        db.insertWithOnConflict(
                GameDatabaseContract.SyncInfoEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    // Get the last sync timestamp for a data type
    private long getLastSyncTimestamp(String type) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                GameDatabaseContract.SyncInfoEntry.TABLE_NAME,
                new String[]{GameDatabaseContract.SyncInfoEntry.COLUMN_LAST_SYNC},
                GameDatabaseContract.SyncInfoEntry.COLUMN_TYPE + " = ?",
                new String[]{type},
                null,
                null,
                null
        );

        long timestamp = 0; // Default to 0 (never synced)

        if (cursor.moveToFirst()) {
            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(GameDatabaseContract.SyncInfoEntry.COLUMN_LAST_SYNC));
        }

        cursor.close();
        return timestamp;
    }

    // Clear old cached data to prevent database from growing too large
    public void cleanupOldCache() {
        SQLiteDatabase db = getWritableDatabase();

        // Delete expired home screen data
        long homeCutoff = System.currentTimeMillis() - CACHE_EXPIRY_HOME;
        db.delete(
                GameDatabaseContract.CachedGamesEntry.TABLE_NAME,
                GameDatabaseContract.CachedGamesEntry.COLUMN_TIMESTAMP + " < ?",
                new String[]{String.valueOf(homeCutoff)}
        );

        // Delete expired game details
        long detailsCutoff = System.currentTimeMillis() - CACHE_EXPIRY_DETAILS;
        db.delete(
                GameDatabaseContract.GameDetailsEntry.TABLE_NAME,
                GameDatabaseContract.GameDetailsEntry.COLUMN_TIMESTAMP + " < ?",
                new String[]{String.valueOf(detailsCutoff)}
        );

        // Keep categories longer since they don't change often
    }
}