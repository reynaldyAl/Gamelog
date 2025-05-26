package com.example.gamemology.api.responses;

import com.google.gson.annotations.SerializedName;

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

        @SerializedName("url")
        private String url;

        @SerializedName("store")
        private Store store;

        public int getId() {
            return id;
        }

        public String getUrl() {
            return url;
        }

        public Store getStore() {
            return store;
        }
    }

    public static class Store {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("slug")
        private String slug;

        @SerializedName("domain")
        private String domain;

        @SerializedName("image_background")
        private String imageBackground;

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