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

    // Table for storing recently viewed or popular games for home screen
    public static class CachedGamesEntry implements BaseColumns {
        public static final String TABLE_NAME = "cached_games";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_RELEASED = "released";
        public static final String COLUMN_BACKGROUND_IMAGE = "background_image";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_CATEGORY = "category"; // e.g., "trending", "popular", "recent"
        public static final String COLUMN_TIMESTAMP = "timestamp"; // When it was cached
        public static final String COLUMN_PAGE = "page"; // For pagination
    }

    // Table for storing game details
    public static class GameDetailsEntry implements BaseColumns {
        public static final String TABLE_NAME = "game_details";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_FULL_DATA = "full_data"; // Store full JSON response
        public static final String COLUMN_TIMESTAMP = "timestamp"; // When it was cached
    }

    // Table for categories (genres, platforms, publishers, etc)
    public static class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "categories";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TYPE = "type"; // "genre", "platform", "publisher", etc
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_GAMES_COUNT = "games_count";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }

    // Table for tracking last synced timestamps
    public static class SyncInfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "sync_info";
        public static final String COLUMN_TYPE = "type"; // What was synced (e.g., "home", "genres")
        public static final String COLUMN_LAST_SYNC = "last_sync"; // When it was last synced
    }
}