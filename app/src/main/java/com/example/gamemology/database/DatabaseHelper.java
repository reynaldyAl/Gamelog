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
import com.example.gamemology.api.responses.PublisherResponse;
import com.example.gamemology.api.responses.StoreResponse;
import com.example.gamemology.models.Game;
import com.example.gamemology.models.User;
import com.example.gamemology.utils.SessionManager;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Database information
    private static final String DATABASE_NAME = "gamemology.db";
    private static final int DATABASE_VERSION = 4; // Increased version for user-specific cache tables

    // Cache expiration time (in milliseconds)
    private static final long CACHE_EXPIRY_HOME = 24 * 60 * 60 * 1000; // 24 hours for home data
    private static final long CACHE_EXPIRY_DETAILS = 7 * 24 * 60 * 60 * 1000; // 7 days for game details
    private static final long CACHE_EXPIRY_CATEGORIES = 7 * 24 * 60 * 60 * 1000; // 7 days for categories

    // Singleton instance
    private static DatabaseHelper instance;
    private final Gson gson = new Gson();
    private final Context context;

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
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create user table
        db.execSQL(CREATE_USER_TABLE);

        // Create favorite games table with user_id
        final String SQL_CREATE_GAME_TABLE = "CREATE TABLE " +
                GameDatabaseContract.GameEntry.TABLE_NAME + " (" +
                GameDatabaseContract.GameEntry.COLUMN_ID + " INTEGER, " +
                GameDatabaseContract.GameEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                GameDatabaseContract.GameEntry.COLUMN_RELEASED + " TEXT, " +
                GameDatabaseContract.GameEntry.COLUMN_BACKGROUND_IMAGE + " TEXT, " +
                GameDatabaseContract.GameEntry.COLUMN_RATING + " REAL, " +
                GameDatabaseContract.GameEntry.COLUMN_DESCRIPTION + " TEXT, " +
                "user_id INTEGER, " +
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
                "user_id INTEGER, " +
                "PRIMARY KEY (" + GameDatabaseContract.CachedGamesEntry.COLUMN_ID + ", " +
                GameDatabaseContract.CachedGamesEntry.COLUMN_CATEGORY + ", " +
                "user_id)" +
                ");";

        // Game details table with user_id
        final String SQL_CREATE_GAME_DETAILS_TABLE = "CREATE TABLE " +
                GameDatabaseContract.GameDetailsEntry.TABLE_NAME + " (" +
                GameDatabaseContract.GameDetailsEntry.COLUMN_ID + " INTEGER, " +
                GameDatabaseContract.GameDetailsEntry.COLUMN_FULL_DATA + " TEXT NOT NULL, " +
                GameDatabaseContract.GameDetailsEntry.COLUMN_TIMESTAMP + " INTEGER, " +
                "user_id INTEGER, " +
                "PRIMARY KEY (" + GameDatabaseContract.GameDetailsEntry.COLUMN_ID + ", user_id)" +
                ");";

        // Categories table with user_id
        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " +
                GameDatabaseContract.CategoryEntry.TABLE_NAME + " (" +
                GameDatabaseContract.CategoryEntry.COLUMN_ID + " INTEGER, " +
                GameDatabaseContract.CategoryEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                GameDatabaseContract.CategoryEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                GameDatabaseContract.CategoryEntry.COLUMN_IMAGE + " TEXT, " +
                GameDatabaseContract.CategoryEntry.COLUMN_GAMES_COUNT + " INTEGER, " +
                GameDatabaseContract.CategoryEntry.COLUMN_FULL_DATA + " TEXT, " +
                GameDatabaseContract.CategoryEntry.COLUMN_TIMESTAMP + " INTEGER, " +
                "user_id INTEGER, " +
                "PRIMARY KEY (" + GameDatabaseContract.CategoryEntry.COLUMN_ID + ", " +
                GameDatabaseContract.CategoryEntry.COLUMN_TYPE + ", user_id)" +
                ");";

        // Sync info table with user_id
        final String SQL_CREATE_SYNC_INFO_TABLE = "CREATE TABLE " +
                GameDatabaseContract.SyncInfoEntry.TABLE_NAME + " (" +
                GameDatabaseContract.SyncInfoEntry.COLUMN_TYPE + " TEXT, " +
                GameDatabaseContract.SyncInfoEntry.COLUMN_LAST_SYNC + " INTEGER, " +
                "user_id INTEGER, " +
                "PRIMARY KEY (" + GameDatabaseContract.SyncInfoEntry.COLUMN_TYPE + ", user_id)" +
                ");";

        db.execSQL(SQL_CREATE_GAME_TABLE);
        db.execSQL(SQL_CREATE_CACHED_GAMES_TABLE);
        db.execSQL(SQL_CREATE_GAME_DETAILS_TABLE);
        db.execSQL(SQL_CREATE_CATEGORY_TABLE);
        db.execSQL(SQL_CREATE_SYNC_INFO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            // Add user_id columns to existing tables if upgrading from earlier versions
            try {
                // Check if columns exist before modifying
                if (!isColumnExists(db, GameDatabaseContract.GameEntry.TABLE_NAME, "user_id")) {
                    db.execSQL("ALTER TABLE " + GameDatabaseContract.GameEntry.TABLE_NAME +
                            " ADD COLUMN user_id INTEGER DEFAULT 1");
                }

                if (!isColumnExists(db, GameDatabaseContract.CachedGamesEntry.TABLE_NAME, "user_id")) {
                    db.execSQL("ALTER TABLE " + GameDatabaseContract.CachedGamesEntry.TABLE_NAME +
                            " ADD COLUMN user_id INTEGER DEFAULT 1");
                }

                if (!isColumnExists(db, GameDatabaseContract.GameDetailsEntry.TABLE_NAME, "user_id")) {
                    db.execSQL("ALTER TABLE " + GameDatabaseContract.GameDetailsEntry.TABLE_NAME +
                            " ADD COLUMN user_id INTEGER DEFAULT 1");
                }

                if (!isColumnExists(db, GameDatabaseContract.CategoryEntry.TABLE_NAME, "user_id")) {
                    db.execSQL("ALTER TABLE " + GameDatabaseContract.CategoryEntry.TABLE_NAME +
                            " ADD COLUMN user_id INTEGER DEFAULT 1");
                }

                if (!isColumnExists(db, GameDatabaseContract.SyncInfoEntry.TABLE_NAME, "user_id")) {
                    db.execSQL("ALTER TABLE " + GameDatabaseContract.SyncInfoEntry.TABLE_NAME +
                            " ADD COLUMN user_id INTEGER DEFAULT 1");
                }

                // Recreate tables with new primary keys if needed
                recreateTableWithUserPrimaryKey(db, GameDatabaseContract.GameEntry.TABLE_NAME,
                        GameDatabaseContract.GameEntry.COLUMN_ID);
                recreateTableWithUserPrimaryKey(db, GameDatabaseContract.CachedGamesEntry.TABLE_NAME,
                        GameDatabaseContract.CachedGamesEntry.COLUMN_ID,
                        GameDatabaseContract.CachedGamesEntry.COLUMN_CATEGORY);
                recreateTableWithUserPrimaryKey(db, GameDatabaseContract.GameDetailsEntry.TABLE_NAME,
                        GameDatabaseContract.GameDetailsEntry.COLUMN_ID);
                recreateTableWithUserPrimaryKey(db, GameDatabaseContract.CategoryEntry.TABLE_NAME,
                        GameDatabaseContract.CategoryEntry.COLUMN_ID,
                        GameDatabaseContract.CategoryEntry.COLUMN_TYPE);
                recreateTableWithUserPrimaryKey(db, GameDatabaseContract.SyncInfoEntry.TABLE_NAME,
                        GameDatabaseContract.SyncInfoEntry.COLUMN_TYPE);

            } catch (Exception e) {
                Log.e(TAG, "Error upgrading database", e);
            }
        }
    }

    /**
     * Ensure all required tables exist in the database
     * Call this method at application startup
     */
    public void ensureTablesExist() {
        SQLiteDatabase db = getWritableDatabase();

        // Check if users table exists, create if not
        if (!isTableExists(db, GameDatabaseContract.UserEntry.TABLE_NAME)) {
            Log.w(TAG, "Users table not found, creating it now");
            db.execSQL(CREATE_USER_TABLE);
        }

        // Check for other essential tables and create if needed
        // This is a safety mechanism to ensure app doesn't crash due to missing tables
        if (!isTableExists(db, GameDatabaseContract.GameEntry.TABLE_NAME)) {
            db.execSQL("CREATE TABLE " + GameDatabaseContract.GameEntry.TABLE_NAME + " (" +
                    GameDatabaseContract.GameEntry.COLUMN_ID + " INTEGER, " +
                    GameDatabaseContract.GameEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                    GameDatabaseContract.GameEntry.COLUMN_RELEASED + " TEXT, " +
                    GameDatabaseContract.GameEntry.COLUMN_BACKGROUND_IMAGE + " TEXT, " +
                    GameDatabaseContract.GameEntry.COLUMN_RATING + " REAL, " +
                    GameDatabaseContract.GameEntry.COLUMN_DESCRIPTION + " TEXT, " +
                    "user_id INTEGER DEFAULT 1, " +
                    "PRIMARY KEY (" + GameDatabaseContract.GameEntry.COLUMN_ID + ", user_id)" +
                    ")");
        }
    }

    /**
     * Check if a table exists in the database
     * @param db Database to check
     * @param tableName Name of table to check for
     * @return true if table exists, false otherwise
     */
    private boolean isTableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;
        try {
            // Query the sqlite_master table for the specified table
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{tableName});
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Helper method to recreate a table with user_id in the primary key
    private void recreateTableWithUserPrimaryKey(SQLiteDatabase db, String tableName, String... keyColumns) {
        Cursor cursor = null;
        try {
            // Get table schema using fixed column indices for PRAGMA table_info
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);

            // PRAGMA table_info always returns columns in this order:
            // 0:cid, 1:name, 2:type, 3:notnull, 4:dflt_value, 5:pk
            final int NAME_INDEX = 1;  // 'name' is always at index 1
            final int TYPE_INDEX = 2;  // 'type' is always at index 2

            StringBuilder createTableSql = new StringBuilder("CREATE TABLE temp_" + tableName + " (");
            List<String> columnNames = new ArrayList<>();

            // Build column definitions
            while (cursor.moveToNext()) {
                String columnName = cursor.getString(NAME_INDEX);
                String columnType = cursor.getString(TYPE_INDEX);
                columnNames.add(columnName);

                createTableSql.append(columnName).append(" ").append(columnType);
                createTableSql.append(", ");
            }

            // Add primary key
            createTableSql.append("PRIMARY KEY (");
            for (int i = 0; i < keyColumns.length; i++) {
                createTableSql.append(keyColumns[i]);
                if (i < keyColumns.length - 1) createTableSql.append(", ");
            }
            createTableSql.append(", user_id)");
            createTableSql.append(")");

            // Execute the temporary table creation
            db.execSQL(createTableSql.toString());

            // Copy data to temp table
            StringBuilder columns = new StringBuilder();
            for (int i = 0; i < columnNames.size(); i++) {
                columns.append(columnNames.get(i));
                if (i < columnNames.size() - 1) columns.append(", ");
            }

            db.execSQL("INSERT INTO temp_" + tableName + " SELECT " + columns + " FROM " + tableName);

            // Drop original table and rename temp
            db.execSQL("DROP TABLE " + tableName);
            db.execSQL("ALTER TABLE temp_" + tableName + " RENAME TO " + tableName);

        } catch (Exception e) {
            Log.e(TAG, "Error recreating table " + tableName, e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public void logDatabaseStatus() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            Log.d(TAG, "Database version: " + db.getVersion());

            // List all tables
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            Log.d(TAG, "Database tables:");
            if (cursor.moveToFirst()) {
                do {
                    Log.d(TAG, "- " + cursor.getString(0));
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "No tables found");
            }

            // Check users table specifically
            boolean usersExists = isTableExists(db, GameDatabaseContract.UserEntry.TABLE_NAME);
            Log.d(TAG, "Users table exists: " + usersExists);

        } catch (Exception e) {
            Log.e(TAG, "Error checking database status", e);
        } finally {
            if (cursor != null) cursor.close();
        }
    }


    // Helper method to check if a column exists
    private boolean isColumnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            final int NAME_INDEX = 1; // 'name' is the second column in PRAGMA table_info

            while (cursor != null && cursor.moveToNext()) {
                String name = cursor.getString(NAME_INDEX);
                if (columnName.equalsIgnoreCase(name)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if column exists: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
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
        // Ensure users table exists before registration
        ensureTablesExist();

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
     * Delete a user account and all their data
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

            // Delete user's cached games
            db.delete(
                    GameDatabaseContract.CachedGamesEntry.TABLE_NAME,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );

            // Delete user's cached game details
            db.delete(
                    GameDatabaseContract.GameDetailsEntry.TABLE_NAME,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );

            // Delete user's cached categories
            db.delete(
                    GameDatabaseContract.CategoryEntry.TABLE_NAME,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );

            // Delete user's sync info
            db.delete(
                    GameDatabaseContract.SyncInfoEntry.TABLE_NAME,
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
     * Add game to favorites for current user
     *
     * @param game Game to add
     * @return Database row ID or -1 if failed
     */
    public long addGameToFavorites(Game game) {
        return addGameToFavorites(game, getCurrentUserId());
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
     * Remove game from favorites for current user
     *
     * @param gameId Game ID to remove
     * @return Number of rows affected
     */
    public int removeGameFromFavorites(int gameId) {
        return removeGameFromFavorites(gameId, getCurrentUserId());
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
     * Check if game is favorite for current user
     *
     * @param gameId Game ID to check
     * @return true if favorited, false otherwise
     */
    public boolean isGameFavorite(int gameId) {
        return isGameFavorite(gameId, getCurrentUserId());
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
     * Get favorite games for current user
     *
     * @return List of favorite games
     */
    public List<Game> getAllFavoriteGames() {
        return getAllFavoriteGames(getCurrentUserId());
    }

    // --- CACHING METHODS FOR OFFLINE MODE ---

    /**
     * Cache games for a specific category for a specific user
     *
     * @param games List of games to cache
     * @param category Category name
     * @param page Page number
     * @param userId User ID
     */
    public void cacheGames(List<GameResponse> games, String category, int page, int userId) {
        if (games == null || games.isEmpty()) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();

        try {
            db.beginTransaction();

            // If it's page 1, clear existing cache for this category and user to avoid duplicates
            if (page == 1) {
                db.delete(GameDatabaseContract.CachedGamesEntry.TABLE_NAME,
                        GameDatabaseContract.CachedGamesEntry.COLUMN_CATEGORY + " = ? AND user_id = ?",
                        new String[]{category, String.valueOf(userId)});
            }

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
                values.put("user_id", userId);

                db.insertWithOnConflict(
                        GameDatabaseContract.CachedGamesEntry.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                );
            }

            // Update sync timestamp
            updateSyncTimestamp(category + "_games", now, userId);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error caching games: ", e);
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }
    }

    /**
     * Cache games for current user
     *
     * @param games List of games to cache
     * @param category Category name
     * @param page Page number
     */
    public void cacheGames(List<GameResponse> games, String category, int page) {
        cacheGames(games, category, page, getCurrentUserId());
    }

    /**
     * Get cached games for a specific user
     *
     * @param category Category name
     * @param page Page number
     * @param userId User ID
     * @return List of cached games
     */
    public List<Game> getCachedGames(String category, int page, int userId) {
        List<Game> games = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String selection = GameDatabaseContract.CachedGamesEntry.COLUMN_CATEGORY + " = ? AND " +
                GameDatabaseContract.CachedGamesEntry.COLUMN_PAGE + " = ? AND " +
                "user_id = ?";

        String[] selectionArgs = {category, String.valueOf(page), String.valueOf(userId)};

        Cursor cursor = db.query(
                GameDatabaseContract.CachedGamesEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
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
                game.setFavorite(isGameFavorite(game.getId(), userId));
                games.add(game);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return games;
    }

    /**
     * Get cached games for current user
     *
     * @param category Category name
     * @param page Page number
     * @return List of cached games
     */
    public List<Game> getCachedGames(String category, int page) {
        return getCachedGames(category, page, getCurrentUserId());
    }

    /**
     * Cache game details for a specific user
     *
     * @param gameId Game ID
     * @param gameDetailsJson Game details JSON
     * @param userId User ID
     */
    public void cacheGameDetails(int gameId, String gameDetailsJson, int userId) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(GameDatabaseContract.GameDetailsEntry.COLUMN_ID, gameId);
        values.put(GameDatabaseContract.GameDetailsEntry.COLUMN_FULL_DATA, gameDetailsJson);
        values.put(GameDatabaseContract.GameDetailsEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());
        values.put("user_id", userId);

        db.insertWithOnConflict(
                GameDatabaseContract.GameDetailsEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    /**
     * Cache game details for current user
     *
     * @param gameId Game ID
     * @param gameDetailsJson Game details JSON
     */
    public void cacheGameDetails(int gameId, String gameDetailsJson) {
        cacheGameDetails(gameId, gameDetailsJson, getCurrentUserId());
    }

    /**
     * Get cached game details for a specific user
     *
     * @param gameId Game ID
     * @param userId User ID
     * @return Game details JSON or null if not cached
     */
    public String getCachedGameDetails(int gameId, int userId) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                GameDatabaseContract.GameDetailsEntry.COLUMN_FULL_DATA
        };

        String selection = GameDatabaseContract.GameDetailsEntry.COLUMN_ID + " = ? AND user_id = ?";
        String[] selectionArgs = {String.valueOf(gameId), String.valueOf(userId)};

        Cursor cursor = db.query(
                GameDatabaseContract.GameDetailsEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        String detailsJson = null;

        if (cursor.moveToFirst()) {
            detailsJson = cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseContract.GameDetailsEntry.COLUMN_FULL_DATA));
        }

        cursor.close();
        return detailsJson;
    }

    /**
     * Get cached game details for current user
     *
     * @param gameId Game ID
     * @return Game details JSON or null if not cached
     */
    public String getCachedGameDetails(int gameId) {
        return getCachedGameDetails(gameId, getCurrentUserId());
    }

    /**
     * Check if cache for a specific user is expired
     *
     * @param type Cache type
     * @param userId User ID
     * @return true if cache is expired, false otherwise
     */
    public boolean isCacheExpired(String type, int userId) {
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

        long lastSync = getLastSyncTimestamp(type, userId);
        return (System.currentTimeMillis() - lastSync) > expiryTime;
    }

    /**
     * Check if cache for current user is expired
     *
     * @param type Cache type
     * @return true if cache is expired, false otherwise
     */
    public boolean isCacheExpired(String type) {
        return isCacheExpired(type, getCurrentUserId());
    }

/**
 * Cache categories for a specific user
 *
 * @param items List of items to cache
 * @param categoryType Category type
 * @param userId User ID
 */
public <T> void cacheCategories(List<T> items, String categoryType, int userId) {
    if (items == null || items.isEmpty()) {
        return;
    }

    SQLiteDatabase db = getWritableDatabase();
    long now = System.currentTimeMillis();

    try {
        db.beginTransaction();

        // Clear existing cached items of this type for this user
        db.delete(
                GameDatabaseContract.CategoryEntry.TABLE_NAME,
                GameDatabaseContract.CategoryEntry.COLUMN_TYPE + " = ? AND user_id = ?",
                new String[]{categoryType, String.valueOf(userId)}
        );

        for (T item : items) {
            int id;
            String name;
            String fullData = gson.toJson(item);

            if (item instanceof GenreResponse.Genre) {
                id = ((GenreResponse.Genre) item).getId();
                name = ((GenreResponse.Genre) item).getName();
            } else if (item instanceof PlatformResponse.Platform) {
                id = ((PlatformResponse.Platform) item).getId();
                name = ((PlatformResponse.Platform) item).getName();
            } else if (item instanceof PublisherResponse.Publisher) {
                id = ((PublisherResponse.Publisher) item).getId();
                name = ((PublisherResponse.Publisher) item).getName();
            } else if (item instanceof StoreResponse.Store) {
                id = ((StoreResponse.Store) item).getId();
                name = ((StoreResponse.Store) item).getName();
            } else if (item instanceof StoreResponse.StoreItem) {
                // Handle StoreItem differently since it has different structure
                id = ((StoreResponse.StoreItem) item).getId();
                // Use getStore() to get the name if direct getName() isn't available
                StoreResponse.Store store = ((StoreResponse.StoreItem) item).getStore();
                name = store != null ? store.getName() : "Unknown Store";
            } else {
                continue;
            }

            ContentValues values = new ContentValues();
            values.put(GameDatabaseContract.CategoryEntry.COLUMN_ID, id);
            values.put(GameDatabaseContract.CategoryEntry.COLUMN_NAME, name);
            values.put(GameDatabaseContract.CategoryEntry.COLUMN_TYPE, categoryType);
            values.put(GameDatabaseContract.CategoryEntry.COLUMN_FULL_DATA, fullData);
            values.put(GameDatabaseContract.CategoryEntry.COLUMN_TIMESTAMP, now);
            values.put("user_id", userId);

            db.insertWithOnConflict(
                    GameDatabaseContract.CategoryEntry.TABLE_NAME,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
            );
        }

        // Update sync timestamp
        updateSyncTimestamp(categoryType, now, userId);

        db.setTransactionSuccessful();
    } catch (Exception e) {
        Log.e(TAG, "Error caching categories: ", e);
    } finally {
        if (db.inTransaction()) {
            db.endTransaction();
        }
    }
}

    /**
     * Cache categories for current user
     *
     * @param items List of items to cache
     * @param categoryType Category type
     */
    public <T> void cacheCategories(List<T> items, String categoryType) {
        cacheCategories(items, categoryType, getCurrentUserId());
    }

    /**
     * Get cached categories for a specific user
     *
     * @param categoryType Category type
     * @param categoryClass Category class
     * @param userId User ID
     * @return List of cached categories
     */
    public <T> List<T> getCachedCategories(String categoryType, Class<T> categoryClass, int userId) {
        List<T> categories = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String selection = GameDatabaseContract.CategoryEntry.COLUMN_TYPE + " = ? AND user_id = ?";
        String[] selectionArgs = {categoryType, String.valueOf(userId)};

        Cursor cursor = db.query(
                GameDatabaseContract.CategoryEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                GameDatabaseContract.CategoryEntry.COLUMN_NAME + " ASC"
        );

        if (cursor.moveToFirst()) {
            do {
                String fullData = cursor.getString(
                        cursor.getColumnIndexOrThrow(GameDatabaseContract.CategoryEntry.COLUMN_FULL_DATA));
                try {
                    T category = gson.fromJson(fullData, categoryClass);
                    categories.add(category);
                } catch (Exception e) {
                    Log.e(TAG, "Error deserializing category: ", e);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return categories;
    }

    /**
     * Get cached categories for current user
     *
     * @param categoryType Category type
     * @param categoryClass Category class
     * @return List of cached categories
     */
    public <T> List<T> getCachedCategories(String categoryType, Class<T> categoryClass) {
        return getCachedCategories(categoryType, categoryClass, getCurrentUserId());
    }

    /**
     * Update sync timestamp for a specific user
     *
     * @param type Sync type
     * @param timestamp Timestamp
     * @param userId User ID
     */
    private void updateSyncTimestamp(String type, long timestamp, int userId) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(GameDatabaseContract.SyncInfoEntry.COLUMN_TYPE, type);
        values.put(GameDatabaseContract.SyncInfoEntry.COLUMN_LAST_SYNC, timestamp);
        values.put("user_id", userId);

        db.insertWithOnConflict(
                GameDatabaseContract.SyncInfoEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    /**
     * Update sync timestamp for current user
     *
     * @param type Sync type
     * @param timestamp Timestamp
     */
    private void updateSyncTimestamp(String type, long timestamp) {
        updateSyncTimestamp(type, timestamp, getCurrentUserId());
    }

    /**
     * Get last sync timestamp for a specific user
     *
     * @param type Sync type
     * @param userId User ID
     * @return Last sync timestamp
     */
    private long getLastSyncTimestamp(String type, int userId) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                GameDatabaseContract.SyncInfoEntry.COLUMN_LAST_SYNC
        };

        String selection = GameDatabaseContract.SyncInfoEntry.COLUMN_TYPE + " = ? AND user_id = ?";
        String[] selectionArgs = {type, String.valueOf(userId)};

        Cursor cursor = db.query(
                GameDatabaseContract.SyncInfoEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        long timestamp = 0;

        if (cursor.moveToFirst()) {
            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(GameDatabaseContract.SyncInfoEntry.COLUMN_LAST_SYNC));
        }

        cursor.close();
        return timestamp;
    }

    /**
     * Get last sync timestamp for current user
     *
     * @param type Sync type
     * @return Last sync timestamp
     */
    private long getLastSyncTimestamp(String type) {
        return getLastSyncTimestamp(type, getCurrentUserId());
    }

    /**
     * Clear old cached data for a specific user
     *
     * @param userId User ID
     */
    public void cleanupOldCache(int userId) {
        SQLiteDatabase db = getWritableDatabase();

        // Delete expired home screen data
        long homeCutoff = System.currentTimeMillis() - CACHE_EXPIRY_HOME;
        db.delete(
                GameDatabaseContract.CachedGamesEntry.TABLE_NAME,
                GameDatabaseContract.CachedGamesEntry.COLUMN_TIMESTAMP + " < ? AND user_id = ?",
                new String[]{String.valueOf(homeCutoff), String.valueOf(userId)}
        );

        // Delete expired game details
        long detailsCutoff = System.currentTimeMillis() - CACHE_EXPIRY_DETAILS;
        db.delete(
                GameDatabaseContract.GameDetailsEntry.TABLE_NAME,
                GameDatabaseContract.GameDetailsEntry.COLUMN_TIMESTAMP + " < ? AND user_id = ?",
                new String[]{String.valueOf(detailsCutoff), String.valueOf(userId)}
        );
    }

    /**
     * Clear old cached data for current user
     */
    public void cleanupOldCache() {
        cleanupOldCache(getCurrentUserId());
    }

    /**
     * Clear all cached data for a specific user
     *
     * @param userId User ID
     */
    public void clearUserCache(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(
                    GameDatabaseContract.CachedGamesEntry.TABLE_NAME,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );

            db.delete(
                    GameDatabaseContract.GameDetailsEntry.TABLE_NAME,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );

            db.delete(
                    GameDatabaseContract.CategoryEntry.TABLE_NAME,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );

            db.delete(
                    GameDatabaseContract.SyncInfoEntry.TABLE_NAME,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Helper method to get current user ID from SessionManager
     *
     * @return Current user ID or 1 if no user is logged in
     */
    private int getCurrentUserId() {
        try {
            User user = SessionManager.getInstance(context).getUser();
            return user != null ? user.getId() : 1; // Default to user ID 1 if not logged in
        } catch (Exception e) {
            Log.e(TAG, "Error getting current user ID", e);
            return 1; // Default user ID
        }
    }

    /**
     * Validate password strength
     *
     * @param password Password to validate
     * @return true if password is strong, false otherwise
     */
    public boolean isPasswordStrong(String password) {
        // At least 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special char
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        return password.matches(passwordPattern);
    }
}