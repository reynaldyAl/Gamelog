package com.example.gamemology.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.gamemology.models.Game;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Informasi database
    private static final String DATABASE_NAME = "gamemology.db";
    private static final int DATABASE_VERSION = 1;

    // Singleton instance
    private static DatabaseHelper instance;

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
        // Membuat tabel game
        final String SQL_CREATE_GAME_TABLE = "CREATE TABLE " +
                GameDatabaseContract.GameEntry.TABLE_NAME + " (" +
                GameDatabaseContract.GameEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                GameDatabaseContract.GameEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                GameDatabaseContract.GameEntry.COLUMN_RELEASED + " TEXT, " +
                GameDatabaseContract.GameEntry.COLUMN_BACKGROUND_IMAGE + " TEXT, " +
                GameDatabaseContract.GameEntry.COLUMN_RATING + " REAL, " +
                GameDatabaseContract.GameEntry.COLUMN_DESCRIPTION + " TEXT" +
                ");";

        db.execSQL(SQL_CREATE_GAME_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Hapus tabel lama dan buat ulang
        db.execSQL("DROP TABLE IF EXISTS " + GameDatabaseContract.GameEntry.TABLE_NAME);
        onCreate(db);
    }

    // CRUD Operations

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
}