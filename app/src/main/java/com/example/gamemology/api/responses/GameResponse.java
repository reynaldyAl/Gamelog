package com.example.gamemology.api.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GameResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("released")
    private String released;

    @SerializedName("background_image")
    private String backgroundImage;

    @SerializedName("rating")
    private double rating;

    @SerializedName("description_raw")
    private String description;

    @SerializedName("genres")
    private List<GenreResponse> genres;

    @SerializedName("platforms")
    private List<PlatformWrapper> platforms;

    @SerializedName("short_screenshots")
    private List<ScreenshotResponse> screenshots;

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

    public double getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public List<GenreResponse> getGenres() {
        return genres;
    }

    public List<PlatformWrapper> getPlatforms() {
        return platforms;
    }

    public List<ScreenshotResponse> getScreenshots() {
        return screenshots;
    }

    // Inner classes for nested objects
    public static class GenreResponse {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public static class PlatformWrapper {
        @SerializedName("platform")
        private PlatformResponse platform;

        public PlatformResponse getPlatform() {
            return platform;
        }
    }

    public static class PlatformResponse {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public static class ScreenshotResponse {
        @SerializedName("id")
        private int id;

        @SerializedName("image")
        private String image;

        public int getId() {
            return id;
        }

        public String getImage() {
            return image;
        }
    }
}