package com.example.gamemology.database;


import android.provider.BaseColumns;

public final class GameDatabaseContract {

    // Untuk mencegah instantiasi
    private GameDatabaseContract() {}

    // Inner class yang mendefinisikan isi tabel
    public static class GameEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorite_games";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_RELEASED = "released";
        public static final String COLUMN_BACKGROUND_IMAGE = "background_image";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_DESCRIPTION = "description";
    }
}