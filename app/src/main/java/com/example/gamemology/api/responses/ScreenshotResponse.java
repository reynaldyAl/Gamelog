package com.example.gamemology.api.responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ScreenshotResponse {
    @SerializedName("count")
    private int count;

    @SerializedName("results")
    private List<Result> results;

    public int getCount() {
        return count;
    }

    public List<Result> getResults() {
        return results;
    }

    public static class Result {
        @SerializedName("id")
        private int id;

        @SerializedName("image")
        private String image;

        @SerializedName("width")
        private int width;

        @SerializedName("height")
        private int height;

        public int getId() {
            return id;
        }

        public String getImage() {
            return image;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}