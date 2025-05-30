package com.example.gamemology.api.responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PublisherResponse {

    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<Publisher> results;

    public int getCount() {
        return count;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public List<Publisher> getResults() {
        return results;
    }

    public static class Publisher {
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
    }

    public static class PublisherDetail extends Publisher {
        @SerializedName("description")
        private String description;

        @SerializedName("games")
        private List<GameResponse> games;

        public String getDescription() {
            return description;
        }

        public List<GameResponse> getGames() {
            return games;
        }
    }
}