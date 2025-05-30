package com.example.gamemology.api.responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TagResponse {

    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<Tag> results;

    public int getCount() {
        return count;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public List<Tag> getResults() {
        return results;
    }

    public static class Tag {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("slug")
        private String slug;

        @SerializedName("games_count")
        private int gamesCount;

        @SerializedName("image_background")
        private String imageBackground;

        @SerializedName("language")
        private String language;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSlug() {
            return slug;
        }

        public int getGamesCount() {
            return gamesCount;
        }

        public String getImageBackground() {
            return imageBackground;
        }

        public String getLanguage() {
            return language;
        }
    }
}