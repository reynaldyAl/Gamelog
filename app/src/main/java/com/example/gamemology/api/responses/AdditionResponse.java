package com.example.gamemology.api.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdditionResponse {

    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<Addition> results;

    public int getCount() {
        return count;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public List<Addition> getResults() {
        return results;
    }

    public static class Addition {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("released")
        private String released;

        @SerializedName("background_image")
        private String backgroundImage;

        @SerializedName("rating")
        private float rating;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getReleased() {
            return released;
        }

        public String getBackgroundImage() {
            return backgroundImage;
        }

        public float getRating() {
            return rating;
        }
    }
}