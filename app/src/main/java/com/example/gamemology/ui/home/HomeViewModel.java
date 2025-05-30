package com.example.gamemology.ui.home;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.gamemology.api.responses.GameListResponse;
import com.example.gamemology.models.Game;
import com.example.gamemology.repository.GameRepository;
import com.example.gamemology.utils.Constants;
import com.example.gamemology.utils.NetworkUtils;
import com.example.gamemology.utils.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeViewModel extends AndroidViewModel {

    private final GameRepository repository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final Map<Integer, MutableLiveData<Pair<List<Game>, Boolean>>> pageCache = new HashMap<>();
    private boolean refreshRequested = false;

    public HomeViewModel(Application application) {
        super(application);
        repository = new GameRepository(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Pair<List<Game>, Boolean>> loadGamesForPage(int page, String category) {
        // If refresh was requested or this is a new page, create a new LiveData
        if (refreshRequested || !pageCache.containsKey(page)) {
            MutableLiveData<Pair<List<Game>, Boolean>> pageData = new MutableLiveData<>();
            pageCache.put(page, pageData);

            // Set loading state
            isLoading.setValue(true);

            // If it's page 1, we can use the trending games method
            if (page == 1) {
                repository.getTrendingGames().observeForever(games -> {
                    if (games != null) {
                        // We have data, determine if there might be more pages
                        boolean hasMore = games.size() >= Constants.PAGE_SIZE;
                        pageData.setValue(new Pair<>(games, hasMore));
                    } else {
                        // No data available
                        pageData.setValue(null);
                    }
                    isLoading.setValue(false);
                });
            } else {
                // For other pages, use a custom page loading approach
                loadPageFromRepository(page, category, pageData);
            }
        }

        // Reset refresh flag
        if (page == 1) {
            refreshRequested = false;
        }

        return pageCache.get(page);
    }

    private void loadPageFromRepository(int page, String category, MutableLiveData<Pair<List<Game>, Boolean>> pageData) {
        // Create a custom method in repository to get paginated data
        // For now, we'll use a LiveData transformation on the trending games

        // This is a simplified implementation - in a real app, the repository should have
        // proper pagination support with separate cache per page
        repository.getGamesPage(page, category).observeForever(games -> {
            if (games != null) {
                // We have data, determine if there might be more pages
                boolean hasMore = games.size() >= Constants.PAGE_SIZE;
                pageData.setValue(new Pair<>(games, hasMore));
            } else {
                // No data available
                pageData.setValue(null);
            }
            isLoading.setValue(false);
        });
    }

    public void refreshGames() {
        refreshRequested = true;
        pageCache.clear(); // Clear the cache to force reload
    }
}