package com.example.gamemology.api.responses;

import com.google.gson.annotations.SerializedName;
import com.example.gamemology.utils.StoreIdMapping;

import java.util.List;

public class StoreResponse {

    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<StoreItem> results;

    public int getCount() {
        return count;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public List<StoreItem> getResults() {
        return results;
    }

    public static class StoreItem {
        @SerializedName("id")
        private int id;

        @SerializedName("game_id")
        private int gameId;

        @SerializedName("store_id")
        private int storeId;

        @SerializedName("url")
        private String url;

        public int getId() {
            return id;
        }

        public int getGameId() {
            return gameId;
        }

        public int getStoreId() {
            return storeId;
        }

        public String getUrl() {
            return url;
        }

        /**
         * Gets store information based on the store_id
         * @return Store information object
         */
        public Store getStore() {
            return StoreIdMapping.getStoreById(storeId);
        }
    }

    public static class Store {
        private int id;
        private String name;
        private String slug;
        private String domain;
        private String imageBackground;

        /**
         * Constructor for creating Store objects in StoreIdMapping
         */
        public Store(int id, String name, String slug) {
            this.id = id;
            this.name = name;
            this.slug = slug;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSlug() {
            return slug;
        }

        public String getDomain() {
            return domain;
        }

        public String getImageBackground() {
            return imageBackground;
        }
    }
}