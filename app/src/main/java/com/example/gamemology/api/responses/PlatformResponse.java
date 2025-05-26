package com.example.gamemology.api.responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlatformResponse {

    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<Platform> results;

    public int getCount() {
        return count;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public List<Platform> getResults() {
        return results;
    }

    public static class Platform {
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

        @SerializedName("year_start")
        private Integer yearStart;

        @SerializedName("year_end")
        private Integer yearEnd;

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

        public Integer getYearStart() {
            return yearStart;
        }

        public Integer getYearEnd() {
            return yearEnd;
        }
    }
}