package com.example.gamemology.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gamemology.api.ApiClient;
import com.example.gamemology.api.ApiService;
import com.example.gamemology.api.responses.GameListResponse;
import com.example.gamemology.api.responses.GameResponse;
import com.example.gamemology.api.responses.GenreResponse;
import com.example.gamemology.api.responses.PlatformResponse;
import com.example.gamemology.database.DatabaseHelper;
import com.example.gamemology.models.Game;
import com.example.gamemology.utils.Constants;
import com.example.gamemology.utils.NetworkUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameRepository {
    private static final String TAG = "GameRepository";

    private final ApiService apiService;
    private final DatabaseHelper dbHelper;
    private final Context context;
    private final Gson gson = new Gson();

    private boolean isLoading = false; // Track if a network request is in progress

    public GameRepository(Context context) {
        this.context = context;
        apiService = ApiClient.getInstance().getApiService();
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // Fetch trending games with offline support
    public LiveData<List<Game>> getTrendingGames() {
        MutableLiveData<List<Game>> gamesLiveData = new MutableLiveData<>();

        // Check if we have connectivity
        boolean isOnline = NetworkUtils.isNetworkAvailable(context);
        String category = "trending";

        // If online or cache expired, try to fetch from network
        if (isOnline && dbHelper.isCacheExpired("home_" + category)) {
            // Fetch from network
            apiService.getGameList(1, 10, null).enqueue(new Callback<GameListResponse>() {
                @Override
                public void onResponse(@NonNull Call<GameListResponse> call, @NonNull Response<GameListResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                        // Save to cache
                        dbHelper.cacheGames(response.body().getResults(), category, 1);

                        // Convert to Game model and deliver
                        List<Game> games = convertResponseToGames(response.body().getResults());
                        gamesLiveData.setValue(games);
                    } else {
                        // If API call fails, try to use cached data
                        List<Game> cachedGames = dbHelper.getCachedGames(category, 1);
                        if (!cachedGames.isEmpty()) {
                            gamesLiveData.setValue(cachedGames);
                        } else {
                            gamesLiveData.setValue(null); // No data available
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GameListResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, "Network error fetching trending games", t);

                    // Use cached data on failure
                    List<Game> cachedGames = dbHelper.getCachedGames(category, 1);
                    if (!cachedGames.isEmpty()) {
                        gamesLiveData.setValue(cachedGames);
                    } else {
                        gamesLiveData.setValue(null); // No data available
                    }
                }
            });
        } else {
            // Offline or cache still valid - use cached data
            List<Game> cachedGames = dbHelper.getCachedGames(category, 1);
            if (!cachedGames.isEmpty()) {
                gamesLiveData.setValue(cachedGames);
            } else if (isOnline) {
                // Cache is empty but we're online, so try to fetch from network anyway
                apiService.getGameList(1, 10, null).enqueue(new Callback<GameListResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GameListResponse> call, @NonNull Response<GameListResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                            dbHelper.cacheGames(response.body().getResults(), category, 1);
                            List<Game> games = convertResponseToGames(response.body().getResults());
                            gamesLiveData.setValue(games);
                        } else {
                            gamesLiveData.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GameListResponse> call, @NonNull Throwable t) {
                        gamesLiveData.setValue(null);
                    }
                });
            } else {
                gamesLiveData.setValue(null); // No cached data and offline
            }
        }

        return gamesLiveData;
    }

    // Fetch game details with offline support
    public LiveData<GameResponse> getGameDetails(int gameId) {
        MutableLiveData<GameResponse> gameLiveData = new MutableLiveData<>();

        boolean isOnline = NetworkUtils.isNetworkAvailable(context);

        // Check if we have cached data
        String cachedDetails = dbHelper.getCachedGameDetails(gameId);
        boolean hasCachedData = cachedDetails != null;

        if (isOnline) {
            // Fetch from network
            apiService.getGameDetail(gameId).enqueue(new Callback<GameResponse>() {
                @Override
                public void onResponse(@NonNull Call<GameResponse> call, @NonNull Response<GameResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        GameResponse game = response.body();

                        // Cache the full game details JSON
                        String gameJson = gson.toJson(game);
                        dbHelper.cacheGameDetails(gameId, gameJson);

                        gameLiveData.setValue(game);
                    } else if (hasCachedData) {
                        // Use cached data if API call fails
                        try {
                            GameResponse game = gson.fromJson(cachedDetails, GameResponse.class);
                            gameLiveData.setValue(game);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing cached game details", e);
                            gameLiveData.setValue(null);
                        }
                    } else {
                        gameLiveData.setValue(null);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GameResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, "Network error fetching game details", t);

                    // Use cached data on failure
                    if (hasCachedData) {
                        try {
                            GameResponse game = gson.fromJson(cachedDetails, GameResponse.class);
                            gameLiveData.setValue(game);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing cached game details", e);
                            gameLiveData.setValue(null);
                        }
                    } else {
                        gameLiveData.setValue(null);
                    }
                }
            });
        } else if (hasCachedData) {
            // Offline - use cached data
            try {
                GameResponse game = gson.fromJson(cachedDetails, GameResponse.class);
                gameLiveData.setValue(game);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing cached game details", e);
                gameLiveData.setValue(null);
            }
        } else {
            gameLiveData.setValue(null); // No cached data and offline
        }

        return gameLiveData;
    }

    // Fetch genres with offline support
    public LiveData<List<GenreResponse.Genre>> getGenres() {
        MutableLiveData<List<GenreResponse.Genre>> genresLiveData = new MutableLiveData<>();

        boolean isOnline = NetworkUtils.isNetworkAvailable(context);
        String categoryType = "genre";

        if (isOnline && dbHelper.isCacheExpired(categoryType)) {
            // Fetch from network
            apiService.getGenres(1, 20).enqueue(new Callback<GenreResponse>() {
                @Override
                public void onResponse(@NonNull Call<GenreResponse> call, @NonNull Response<GenreResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                        List<GenreResponse.Genre> genres = response.body().getResults();

                        // Cache genres
                        dbHelper.cacheCategories(genres, categoryType);

                        genresLiveData.setValue(genres);
                    } else {
                        // If API call fails, try to use cached data
                        List<GenreResponse.Genre> cachedGenres = dbHelper.getCachedCategories(categoryType, GenreResponse.Genre.class);
                        if (!cachedGenres.isEmpty()) {
                            genresLiveData.setValue(cachedGenres);
                        } else {
                            genresLiveData.setValue(null);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GenreResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, "Network error fetching genres", t);

                    // Use cached data on failure
                    List<GenreResponse.Genre> cachedGenres = dbHelper.getCachedCategories(categoryType, GenreResponse.Genre.class);
                    if (!cachedGenres.isEmpty()) {
                        genresLiveData.setValue(cachedGenres);
                    } else {
                        genresLiveData.setValue(null);
                    }
                }
            });
        } else {
            // Offline or cache still valid - use cached data
            List<GenreResponse.Genre> cachedGenres = dbHelper.getCachedCategories(categoryType, GenreResponse.Genre.class);
            if (!cachedGenres.isEmpty()) {
                genresLiveData.setValue(cachedGenres);
            } else if (isOnline) {
                // Empty cache but online - try network
                apiService.getGenres(1, 20).enqueue(new Callback<GenreResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GenreResponse> call, @NonNull Response<GenreResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                            List<GenreResponse.Genre> genres = response.body().getResults();
                            dbHelper.cacheCategories(genres, categoryType);
                            genresLiveData.setValue(genres);
                        } else {
                            genresLiveData.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GenreResponse> call, @NonNull Throwable t) {
                        genresLiveData.setValue(null);
                    }
                });
            } else {
                genresLiveData.setValue(null); // No cache and offline
            }
        }

        return genresLiveData;
    }

    // Helper method to convert API response to app model
    private List<Game> convertResponseToGames(List<GameResponse> gameResponses) {
        List<Game> games = new ArrayList<>();

        for (GameResponse response : gameResponses) {
            Game game = new Game();
            game.setId(response.getId());
            game.setName(response.getName());
            game.setReleased(response.getReleased());
            game.setBackgroundImage(response.getBackgroundImage());
            game.setRating(response.getRating());
            game.setFavorite(dbHelper.isGameFavorite(response.getId()));
            games.add(game);
        }

        return games;
    }

    // Add this method to your GameRepository.java
    public LiveData<List<Game>> getGamesPage(int page, String category) {
        MutableLiveData<List<Game>> gamesLiveData = new MutableLiveData<>();

        // Check if we have connectivity
        boolean isOnline = NetworkUtils.isNetworkAvailable(context);

        // First check for cached data
        List<Game> cachedGames = dbHelper.getCachedGames(category, page);
        if (!cachedGames.isEmpty()) {
            gamesLiveData.setValue(cachedGames);

            // If we're online, refresh in background
            if (isOnline) {
                refreshPageFromNetwork(page, category, gamesLiveData);
            }
            return gamesLiveData;
        }

        // No cached data, try network if online
        if (isOnline) {
            isLoading = true;
            apiService.getGameList(page, Constants.PAGE_SIZE, null).enqueue(new Callback<GameListResponse>() {
                @Override
                public void onResponse(@NonNull Call<GameListResponse> call, @NonNull Response<GameListResponse> response) {
                    isLoading = false;
                    if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                        // Save to cache
                        dbHelper.cacheGames(response.body().getResults(), category, page);

                        // Convert to Game model and deliver
                        List<Game> games = convertResponseToGames(response.body().getResults());
                        gamesLiveData.setValue(games);
                    } else {
                        gamesLiveData.setValue(null);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GameListResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, "Network error fetching games page " + page, t);
                    isLoading = false;
                    gamesLiveData.setValue(null);
                }
            });
        } else {
            // Offline with no cache
            gamesLiveData.setValue(null);
        }

        return gamesLiveData;
    }

    private void refreshPageFromNetwork(int page, String category, MutableLiveData<List<Game>> liveData) {
        apiService.getGameList(page, Constants.PAGE_SIZE, null).enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(@NonNull Call<GameListResponse> call, @NonNull Response<GameListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    // Save to cache
                    dbHelper.cacheGames(response.body().getResults(), category, page);

                    // If this is not the first data delivery, update the LiveData
                    if (liveData.getValue() == null) {
                        List<Game> games = convertResponseToGames(response.body().getResults());
                        liveData.setValue(games);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GameListResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Background refresh failed for page " + page, t);
                // We already have data from cache, so no need to update LiveData
            }
        });
    }

    // Perform database maintenance (call this occasionally, like on app startup)
    public void cleanupOldCache() {
        dbHelper.cleanupOldCache();
    }
}