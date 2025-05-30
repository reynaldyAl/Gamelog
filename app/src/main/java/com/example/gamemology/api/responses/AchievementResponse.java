package com.example.gamemology.api.responses;


import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AchievementResponse {

    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<Achievement> results;

    public int getCount() {
        return count;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public List<Achievement> getResults() {
        return results;
    }

    public static class Achievement {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("description")
        private String description;

        @SerializedName("image")
        private String image;

        @SerializedName("percent")
        private float percent;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getImage() {
            return image;
        }

        public float getPercent() {
            return percent;
        }
    }
}