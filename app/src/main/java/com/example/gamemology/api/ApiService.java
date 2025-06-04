package com.example.gamemology.api;

import com.example.gamemology.api.responses.*;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiService {

    // Game listings
    @GET("games")
    Call<GameListResponse> getGameList(
            @Query("page") int page,
            @Query("page_size") int pageSize,
            @Query("search") String search
    );

    // Game listings with filters
    @GET("games")
    Call<GameListResponse> getGameList(
            @Query("page") int page,
            @Query("page_size") int pageSize,
            @Query("search") String search,
            @Query("genres") Integer genreId,
            @Query("platforms") Integer platformId,
            @Query("publishers") Integer publisherId
    );

    // Game listings with store filter
    @GET("games")
    Call<GameListResponse> getGameList(
            @Query("page") int page,
            @Query("page_size") int pageSize,
            @Query("search") String search,
            @Query("genres") Integer genreId,
            @Query("platforms") Integer platformId,
            @Query("publishers") Integer publisherId,
            @Query("stores") Integer storeId
    );

    // Game details
    @GET("games/{id}")
    Call<GameResponse> getGameDetail(@Path("id") int gameId);

    // Screenshots
    @GET("games/{game_pk}/screenshots")
    Call<ScreenshotResponse> getGameScreenshots(@Path("game_pk") int gameId);

    // DLC & Add-ons
    @GET("games/{game_pk}/additions")
    Call<AdditionResponse> getGameAdditions(@Path("game_pk") int gameId);

    // Parent Games
    @GET("games/{game_pk}/parent-games")
    Call<ParentGameResponse> getParentGames(@Path("game_pk") int gameId);

    // Game Stores
    @GET("games/{game_pk}/stores")
    Call<StoreResponse> getGameStores(@Path("game_pk") int gameId);

    // Achievements
    @GET("games/{id}/achievements")
    Call<AchievementResponse> getGameAchievements(
            @Path("id") int gameId,
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    // Game Movies/Trailers
    @GET("games/{id}/movies")
    Call<MovieResponse> getGameMovies(@Path("id") int gameId);

    // Genres
    @GET("genres")
    Call<GenreResponse> getGenres(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("genres/{id}")
    Call<GenreResponse.GenreDetail> getGenreDetail(@Path("id") int genreId);

    // Platforms
    @GET("platforms")
    Call<PlatformResponse> getPlatforms(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("platforms/lists/parents")
    Call<PlatformResponse> getParentPlatforms();

    // Publishers
    @GET("publishers")
    Call<PublisherResponse> getPublishers(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("publishers/{id}")
    Call<PublisherResponse.PublisherDetail> getPublisherDetail(@Path("id") int publisherId);

    // Stores
    @GET("stores")
    Call<StoreResponse> getStores();

    // Tags
    @GET("tags")
    Call<TagResponse> getTags(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    // New method for pagination
    @GET
    Call<ScreenshotResponse> getGameScreenshotsNextPage(@Url String url);

}