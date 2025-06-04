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
import com.example.gamemology.models.User;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Informasi database
    private static final String DATABASE_NAME = "gamemology.db";
    private static final int DATABASE_VERSION = 3; // Increased version for user tables

    // Cache expiration time (in milliseconds)
    private static final long CACHE_EXPIRY_HOME = 24 * 60 * 60 * 1000; // 24 hours for home data
    private static final long CACHE_EXPIRY_DETAILS = 7 * 24 * 60 * 60 * 1000; // 7 days for game details
    private static final long CACHE_EXPIRY_CATEGORIES = 7 * 24 * 60 * 60 * 1000; // 7 days for categories

    // Singleton instance
    private static DatabaseHelper instance;
    private final Gson gson = new Gson();

    // User table creation statement
    private static final String CREATE_USER_TABLE =
            "CREATE TABLE " + GameDatabaseContract.UserEntry.TABLE_NAME + " (" +
                    GameDatabaseContract.UserEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    GameDatabaseContract.UserEntry.COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                    GameDatabaseContract.UserEntry.COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, " +
                    GameDatabaseContract.UserEntry.COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    GameDatabaseContract.UserEntry.COLUMN_PROFILE_IMAGE + " TEXT, " +
                    GameDatabaseContract.UserEntry.COLUMN_JOIN_DATE + " INTEGER)";

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
                GameDatabaseContract.GameEntry.COLUMN_ID + " INTEGER, " +
                GameDatabaseContract.GameEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                GameDatabaseContract.GameEntry.COLUMN_RELEASED + " TEXT, " +
                GameDatabaseContract.GameEntry.COLUMN_BACKGROUND_IMAGE + " TEXT, " +
                GameDatabaseContract.GameEntry.COLUMN_RATING + " REAL, " +
                GameDatabaseContract.GameEntry.COLUMN_DESCRIPTION + " TEXT, " +
                "user_id INTEGER DEFAULT 1, " +
                "PRIMARY KEY (" + GameDatabaseContract.GameEntry.COLUMN_ID + ", user_id)" +
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
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Add user table
            db.execSQL(CREATE_USER_TABLE);

            // Modify favorites table to include user_id if upgrading from version < 3
            try {
                db.execSQL("ALTER TABLE " + GameDatabaseContract.GameEntry.TABLE_NAME +
                        " ADD COLUMN user_id INTEGER DEFAULT 1");
            } catch (Exception e) {
                // Table might already have this column
                Log.e(TAG, "Error adding user_id column: " + e.getMessage());
            }

            // Update primary key to include user_id
            try {
                // Copy data to temp table with new schema
                db.execSQL("CREATE TABLE temp_favorites AS SELECT * FROM " + GameDatabaseContract.GameEntry.TABLE_NAME);
                db.execSQL("DROP TABLE " + GameDatabaseContract.GameEntry.TABLE_NAME);
                db.execSQL("CREATE TABLE " + GameDatabaseContract.GameEntry.TABLE_NAME + " (" +
                        GameDatabaseContract.GameEntry.COLUMN_ID + " INTEGER, " +
                        GameDatabaseContract.GameEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        GameDatabaseContract.GameEntry.COLUMN_RELEASED + " TEXT, " +
                        GameDatabaseContract.GameEntry.COLUMN_BACKGROUND_IMAGE + " TEXT, " +
                        GameDatabaseContract.GameEntry.COLUMN_RATING + " REAL, " +
                        GameDatabaseContract.GameEntry.COLUMN_DESCRIPTION + " TEXT, " +
                        "user_id INTEGER DEFAULT 1, " +
                        "PRIMARY KEY (" + GameDatabaseContract.GameEntry.COLUMN_ID + ", user_id)" +
                        ");");
                db.execSQL("INSERT INTO " + GameDatabaseContract.GameEntry.TABLE_NAME +
                        " SELECT * FROM temp_favorites");
                db.execSQL("DROP TABLE temp_favorites");
            } catch (Exception e) {
                // Error with migration, fallback to recreate
                Log.e(TAG, "Error migrating favorites table: " + e.getMessage());
                db.execSQL("DROP TABLE IF EXISTS " + GameDatabaseContract.GameEntry.TABLE_NAME);
                db.execSQL("CREATE TABLE " + GameDatabaseContract.GameEntry.TABLE_NAME + " (" +
                        GameDatabaseContract.GameEntry.COLUMN_ID + " INTEGER, " +
                        GameDatabaseContract.GameEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        GameDatabaseContract.GameEntry.COLUMN_RELEASED + " TEXT, " +
                        GameDatabaseContract.GameEntry.COLUMN_BACKGROUND_IMAGE + " TEXT, " +
                        GameDatabaseContract.GameEntry.COLUMN_RATING + " REAL, " +
                        GameDatabaseContract.GameEntry.COLUMN_DESCRIPTION + " TEXT, " +
                        "user_id INTEGER DEFAULT 1, " +
                        "PRIMARY KEY (" + GameDatabaseContract.GameEntry.COLUMN_ID + ", user_id)" +
                        ");");
            }
        }
    }

    // --- USER AUTHENTICATION METHODS ---

    /**
     * Register a new user
     *
     * @param username Username (must be unique)
     * @param email Email address (must be unique)
     * @param password User password (will be hashed)
     * @return User ID if successful, -1 if failed
     */
    public long registerUser(String username, String email, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(GameDatabaseContract.UserEntry.COLUMN_USERNAME, username);
        values.put(GameDatabaseContract.UserEntry.COLUMN_EMAIL, email);
        values.put(GameDatabaseContract.UserEntry.COLUMN_PASSWORD, hashPassword(password));
        values.put(GameDatabaseContract.UserEntry.COLUMN_JOIN_DATE, System.currentTimeMillis());

        return db.insert(GameDatabaseContract.UserEntry.TABLE_NAME, null, values);
    }

    /**
     * Authenticate a user with username and password
     *
     * @param username Username
     * @param password Password (plaintext)
     * @return User object if authentication successful, null if failed
     */
    public User loginUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                GameDatabaseContract.UserEntry.COLUMN_ID,
                GameDatabaseContract.UserEntry.COLUMN_USERNAME,
                GameDatabaseContract.UserEntry.COLUMN_EMAIL,
                GameDatabaseContract.UserEntry.COLUMN_PASSWORD,
                GameDatabaseContract.UserEntry.COLUMN_PROFILE_IMAGE,
                GameDatabaseContract.UserEntry.COLUMN_JOIN_DATE
        };

        String selection = GameDatabaseContract.UserEntry.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(
                GameDatabaseContract.UserEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        User user = null;

        if (cursor.moveToFirst()) {
            String hashedPassword = cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.UserEntry.COLUMN_PASSWORD));

            if (verifyPassword(password, hashedPassword)) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(GameDatabaseContract.UserEntry.COLUMN_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.UserEntry.COLUMN_USERNAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.UserEntry.COLUMN_EMAIL)));
                user.setProfileImage(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.UserEntry.COLUMN_PROFILE_IMAGE)));
                user.setJoinDate(cursor.getLong(cursor.getColumnIndexOrThrow(GameDatabaseContract.UserEntry.COLUMN_JOIN_DATE)));
            }
        }

        cursor.close();
        return user;
    }

    /**
     * Check if a username is already taken
     *
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                GameDatabaseContract.UserEntry.TABLE_NAME,
                new String[]{GameDatabaseContract.UserEntry.COLUMN_ID},
                GameDatabaseContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Check if an email address is already registered
     *
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                GameDatabaseContract.UserEntry.TABLE_NAME,
                new String[]{GameDatabaseContract.UserEntry.COLUMN_ID},
                GameDatabaseContract.UserEntry.COLUMN_EMAIL + " = ?",
                new String[]{email},
                null,
                null,
                null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Update a user's profile information
     *
     * @param user Updated User object
     * @return Number of rows affected
     */
    public int updateUserProfile(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(GameDatabaseContract.UserEntry.COLUMN_EMAIL, user.getEmail());
        values.put(GameDatabaseContract.UserEntry.COLUMN_PROFILE_IMAGE, user.getProfileImage());

        return db.update(
                GameDatabaseContract.UserEntry.TABLE_NAME,
                values,
                GameDatabaseContract.UserEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(user.getId())}
        );
    }

    /**
     * Update a user's password
     *
     * @param userId User ID
     * @param newPassword New password (will be hashed)
     * @return Number of rows affected
     */
    public int updateUserPassword(int userId, String newPassword) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(GameDatabaseContract.UserEntry.COLUMN_PASSWORD, hashPassword(newPassword));

        return db.update(
                GameDatabaseContract.UserEntry.TABLE_NAME,
                values,
                GameDatabaseContract.UserEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
    }

    /**
     * Delete a user account
     *
     * @param userId User ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            // Delete user's favorites
            db.delete(
                    GameDatabaseContract.GameEntry.TABLE_NAME,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );

            // Delete the user
            int result = db.delete(
                    GameDatabaseContract.UserEntry.TABLE_NAME,
                    GameDatabaseContract.UserEntry.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(userId)}
            );

            if (result > 0) {
                db.setTransactionSuccessful();
                return true;
            }

            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Get a user by ID
     *
     * @param userId User ID
     * @return User object or null if not found
     */
    public User getUserById(int userId) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                GameDatabaseContract.UserEntry.COLUMN_ID,
                GameDatabaseContract.UserEntry.COLUMN_USERNAME,
                GameDatabaseContract.UserEntry.COLUMN_EMAIL,
                GameDatabaseContract.UserEntry.COLUMN_PROFILE_IMAGE,
                GameDatabaseContract.UserEntry.COLUMN_JOIN_DATE
        };

        Cursor cursor = db.query(
                GameDatabaseContract.UserEntry.TABLE_NAME,
                projection,
                GameDatabaseContract.UserEntry.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );

        User user = null;

        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(GameDatabaseContract.UserEntry.COLUMN_ID)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.UserEntry.COLUMN_USERNAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.UserEntry.COLUMN_EMAIL)));
            user.setProfileImage(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.UserEntry.COLUMN_PROFILE_IMAGE)));
            user.setJoinDate(cursor.getLong(cursor.getColumnIndexOrThrow(GameDatabaseContract.UserEntry.COLUMN_JOIN_DATE)));
        }

        cursor.close();
        return user;
    }

    /**
     * Hash a password using SHA-256
     * Note: In a production app, use a more secure method like BCrypt
     *
     * @param password Password to hash
     * @return Hashed password
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error hashing password", e);
            return "";
        }
    }

    /**
     * Verify a password against a hash
     *
     * @param inputPassword Plaintext password
     * @param storedHash Stored hash
     * @return true if match, false otherwise
     */
    private boolean verifyPassword(String inputPassword, String storedHash) {
        String inputHash = hashPassword(inputPassword);
        return inputHash.equals(storedHash);
    }

    // --- FAVORITES METHODS (UPDATED FOR USER SUPPORT) ---

    /**
     * Add game to user's favorites
     *
     * @param game Game to add
     * @param userId User ID
     * @return Database row ID or -1 if failed
     */
    public long addGameToFavorites(Game game, int userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(GameDatabaseContract.GameEntry.COLUMN_ID, game.getId());
        values.put(GameDatabaseContract.GameEntry.COLUMN_NAME, game.getName());
        values.put(GameDatabaseContract.GameEntry.COLUMN_RELEASED, game.getReleased());
        values.put(GameDatabaseContract.GameEntry.COLUMN_BACKGROUND_IMAGE, game.getBackgroundImage());
        values.put(GameDatabaseContract.GameEntry.COLUMN_RATING, game.getRating());
        values.put(GameDatabaseContract.GameEntry.COLUMN_DESCRIPTION, game.getDescription());
        values.put("user_id", userId);

        return db.insert(GameDatabaseContract.GameEntry.TABLE_NAME, null, values);
    }

    /**
     * Add game to favorites for default user (backward compatibility)
     *
     * @param game Game to add
     * @return Database row ID or -1 if failed
     */
    public long addGameToFavorites(Game game) {
        return addGameToFavorites(game, 1); // default user_id
    }

    /**
     * Remove game from user's favorites
     *
     * @param gameId Game ID to remove
     * @param userId User ID
     * @return Number of rows affected
     */
    public int removeGameFromFavorites(int gameId, int userId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(GameDatabaseContract.GameEntry.TABLE_NAME,
                GameDatabaseContract.GameEntry.COLUMN_ID + " = ? AND user_id = ?",
                new String[]{String.valueOf(gameId), String.valueOf(userId)});
    }

    /**
     * Remove game from favorites for default user (backward compatibility)
     *
     * @param gameId Game ID to remove
     * @return Number of rows affected
     */
    public int removeGameFromFavorites(int gameId) {
        return removeGameFromFavorites(gameId, 1); // default user_id
    }

    /**
     * Check if a game is in user's favorites
     *
     * @param gameId Game ID to check
     * @param userId User ID
     * @return true if favorited, false otherwise
     */
    public boolean isGameFavorite(int gameId, int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                GameDatabaseContract.GameEntry.TABLE_NAME,
                null,
                GameDatabaseContract.GameEntry.COLUMN_ID + " = ? AND user_id = ?",
                new String[]{String.valueOf(gameId), String.valueOf(userId)},
                null,
                null,
                null
        );
        boolean isFavorite = cursor.getCount() > 0;
        cursor.close();
        return isFavorite;
    }

    /**
     * Check if game is favorite for default user (backward compatibility)
     *
     * @param gameId Game ID to check
     * @return true if favorited, false otherwise
     */
    public boolean isGameFavorite(int gameId) {
        return isGameFavorite(gameId, 1); // default user_id
    }

    /**
     * Get all favorite games for a specific user
     *
     * @param userId User ID
     * @return List of favorite games
     */
    public List<Game> getAllFavoriteGames(int userId) {
        List<Game> favoriteGames = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                GameDatabaseContract.GameEntry.TABLE_NAME,
                null,
                "user_id = ?",
                new String[]{String.valueOf(userId)},
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

    /**
     * Get favorite games for default user (backward compatibility)
     *
     * @return List of favorite games
     */
    public List<Game> getAllFavoriteGames() {
        return getAllFavoriteGames(1); // default user_id
    }

    // --- CACHING METHODS FOR OFFLINE MODE ---

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

    // Add stronger password validation


    private boolean isPasswordStrong(String password) {
        // At least 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special char
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        return password.matches(passwordPattern);
    }
}