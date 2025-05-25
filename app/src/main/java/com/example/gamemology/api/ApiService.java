package com.example.gamemology.api;


import com.example.gamemology.api.responses.GameListResponse;
import com.example.gamemology.api.responses.GameResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("games")
    Call<GameListResponse> getGameList(
            @Query("page") int page,
            @Query("page_size") int pageSize,
            @Query("search") String search
    );

    @GET("games/{id}")
    Call<GameResponse> getGameDetail(@Path("id") int gameId);

    @GET("games/{game_pk}/screenshots")
    Call<GameListResponse> getGameScreenshots(@Path("game_pk") int gameId);
}