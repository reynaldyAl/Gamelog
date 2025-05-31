package com.example.gamemology.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.gamemology.models.Game;
import com.example.gamemology.repository.GameRepository;
import com.example.gamemology.utils.Constants;
import com.example.gamemology.utils.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeViewModel extends AndroidViewModel {
    private static final String TAG = "HomeViewModel";

    private final GameRepository repository;

    // Central LiveData object for UI to observe
    private final MediatorLiveData<Result<List<Game>>> gamesResult = new MediatorLiveData<>();

    // Keep track of observers to avoid memory leaks
    private final Map<Integer, Observer<List<Game>>> observers = new HashMap<>();

    // Internal state management
    private final List<Game> allGames = new ArrayList<>();
    private int currentPage = 1;
    private boolean hasMoreData = true;
    private boolean isRefreshing = false;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new GameRepository(application);
        gamesResult.setValue(Result.initial());
    }

    /**
     * Get the LiveData object for UI to observe.
     */
    public LiveData<Result<List<Game>>> getGamesResult() {
        return gamesResult;
    }

    /**
     * Load first page of games
     */
    public void loadFirstPage() {
        Log.d(TAG, "Loading first page");

        // Reset state
        currentPage = 1;
        allGames.clear();
        isRefreshing = true;

        // Show loading state
        gamesResult.setValue(Result.loading());

        // Load first page
        loadPage(1);
    }

    /**
     * Load next page for pagination
     */
    public void loadNextPage() {
        if (!hasMoreData || isLoading()) {
            return;
        }

        int nextPage = currentPage + 1;
        Log.d(TAG, "Loading next page: " + nextPage);

        // Show loading more state
        if (gamesResult.getValue() != null && gamesResult.getValue().isSuccess()) {
            gamesResult.setValue(Result.loadingMore());
        }

        loadPage(nextPage);
    }

    /**
     * Refresh games data
     */
    public void refreshGames() {
        Log.d(TAG, "Refreshing games");
        loadFirstPage();
    }

    /**
     * Check if we're currently loading
     */
    private boolean isLoading() {
        Result<List<Game>> value = gamesResult.getValue();
        return value != null && value.isLoading();
    }

    /**
     * Internal method to load a specific page
     */
    private void loadPage(int page) {
        String category = "home_page_" + page;

        // Clean up old observer if refreshing
        if (observers.containsKey(page)) {
            gamesResult.removeSource(repository.getGamesPage(page, category));
            observers.remove(page);
        }

        // Create new observer
        Observer<List<Game>> observer = games -> {
            Log.d(TAG, "Received data for page " + page + ": " +
                    (games != null ? games.size() : 0) + " games");

            if (games != null && !games.isEmpty()) {
                if (page == 1) {
                    // First page - replace all data
                    allGames.clear();
                    allGames.addAll(games);
                } else {
                    // Subsequent pages - add to existing data
                    allGames.addAll(games);
                }

                // Check if there might be more data
                hasMoreData = games.size() >= Constants.PAGE_SIZE;
                currentPage = Math.max(currentPage, page);

                // Update UI - create new list to trigger observers
                gamesResult.setValue(Result.success(new ArrayList<>(allGames), hasMoreData));
            } else if (isLoading()) {
                // No data returned while in loading state
                if (page == 1 || allGames.isEmpty()) {
                    gamesResult.setValue(Result.error("Failed to load games"));
                } else {
                    // For pagination, keep existing data
                    gamesResult.setValue(Result.success(new ArrayList<>(allGames), false));
                }
            }

            // Reset refreshing flag after first page is loaded
            if (page == 1) {
                isRefreshing = false;
            }
        };

        // Store observer for cleanup
        observers.put(page, observer);

        // Start observing repository data
        gamesResult.addSource(repository.getGamesPage(page, category), observer);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        // Clean up all observers to avoid memory leaks
        for (int page : observers.keySet()) {
            String category = "home_page_" + page;
            gamesResult.removeSource(repository.getGamesPage(page, category));
        }
        observers.clear();
    }
}