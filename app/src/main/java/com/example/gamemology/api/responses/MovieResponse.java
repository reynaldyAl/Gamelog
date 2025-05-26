package com.example.gamemology.api.responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieResponse {

    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<Movie> results;

    public int getCount() {
        return count;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public List<Movie> getResults() {
        return results;
    }

    public static class Movie {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("preview")
        private String preview;

        @SerializedName("data")
        private MovieData data;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPreview() {
            return preview;
        }

        public MovieData getData() {
            return data;
        }

        public static class MovieData {
            @SerializedName("480")
            private String quality480;

            @SerializedName("max")
            private String qualityMax;

            public String getQuality480() {
                return quality480;
            }

            public String getQualityMax() {
                return qualityMax;
            }
        }
    }
}