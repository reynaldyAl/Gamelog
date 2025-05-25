package com.example.gamemology.api.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GameListResponse {

    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<GameResponse> results;

    public int getCount() {
        return count;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public List<GameResponse> getResults() {
        return results;
    }
}