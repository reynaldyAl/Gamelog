package com.example.gamemology.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gamemology.R;
import com.example.gamemology.adapter.GameAdapter;
import com.example.gamemology.api.ApiClient;
import com.example.gamemology.api.ApiService;
import com.example.gamemology.api.responses.GameListResponse;
import com.example.gamemology.api.responses.GameResponse;
import com.example.gamemology.database.DatabaseHelper;
import com.example.gamemology.databinding.ActivitySearchBinding;
import com.example.gamemology.models.Game;
import com.example.gamemology.ui.detail.DetailActivity;
import com.example.gamemology.ui.search.FilterBottomSheet.FilterOptions;
import com.example.gamemology.utils.Constants;
import com.example.gamemology.utils.NetworkUtils;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity implements FilterBottomSheet.FilterListener {

    private ActivitySearchBinding binding;
    private GameAdapter gameAdapter;
    private ApiService apiService;
    private DatabaseHelper dbHelper;

    // Search and filter state
    private String currentQuery = "";
    private FilterOptions filterOptions = new FilterOptions();
    private String currentSorting = "popularity"; // Sorting option name
    private Call<GameListResponse> currentCall;

    // Debounce handler for search
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private static final long SEARCH_DELAY_MS = 300;
    private Runnable searchRunnable;

    // Pagination
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMorePages = true;
    private final int PAGE_SIZE = 20;
    private final String TAG = "SearchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.search_games);
        }

        // Initialize API service and database helper
        apiService = ApiClient.getInstance().getApiService();
        dbHelper = DatabaseHelper.getInstance(this);

        // Setup components
        setupRecyclerView();
        setupSearchView();
        setupFilterButton();
        setupSortChips();
        setupSwipeToRefresh();
    }

    private void setupRecyclerView() {
        gameAdapter = new GameAdapter(this);
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSearchResults.setAdapter(gameAdapter);

        // Set up item click listener
        gameAdapter.setOnItemClickListener(new GameAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Game game) {
                Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
                intent.putExtra(Constants.EXTRA_GAME_ID, game.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Game game, boolean isFavorite) {
                if (isFavorite) {
                    dbHelper.addGameToFavorites(game);
                    Toast.makeText(SearchActivity.this, R.string.added_to_favorites, Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.removeGameFromFavorites(game.getId());
                    Toast.makeText(SearchActivity.this, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Setup pagination scroll listener
        binding.rvSearchResults.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!isLoading && hasMorePages && dy > 0) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                                && firstVisibleItemPosition >= 0) {
                            loadNextPage();
                        }
                    }
                }
            }
        });
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchGames(query, true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Debounce search
                searchHandler.removeCallbacks(searchRunnable);

                if (newText.length() >= 2) {
                    searchRunnable = () -> searchGames(newText, true);
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                } else if (newText.isEmpty()) {
                    // Clear results if search box is cleared
                    clearResults();
                }
                return true;
            }
        });

        // Set initial focus
        binding.searchView.setIconified(false);
    }

    private void setupFilterButton() {
        binding.btnFilter.setOnClickListener(v -> {
            FilterBottomSheet filterSheet = FilterBottomSheet.newInstance(filterOptions);
            filterSheet.setListener(this);
            filterSheet.show(getSupportFragmentManager(), "FilterBottomSheet");
        });
    }

    private void setupSortChips() {
        binding.chipPopular.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentSorting = "popularity"; // Client-side sorting key
                refreshSearch();
            }
        });

        binding.chipNewest.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentSorting = "released"; // Client-side sorting key
                refreshSearch();
            }
        });

        binding.chipHighestRated.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentSorting = "rating"; // Client-side sorting key
                refreshSearch();
            }
        });

        binding.chipNameAsc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentSorting = "name_asc"; // Client-side sorting key
                refreshSearch();
            }
        });

        binding.chipNameDesc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentSorting = "name_desc"; // Client-side sorting key
                refreshSearch();
            }
        });
    }

    private void setupSwipeToRefresh() {
        binding.swipeRefresh.setOnRefreshListener(this::refreshSearch);
    }

    private void refreshSearch() {
        currentPage = 1;
        searchGames(currentQuery, true);
    }

    private void searchGames(String query, boolean resetResults) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
            binding.swipeRefresh.setRefreshing(false);
            return;
        }

        currentQuery = query;
        if (resetResults) {
            currentPage = 1;
        }

        isLoading = true;

        if (resetResults) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.rvSearchResults.setVisibility(View.GONE);
        }

        binding.tvNoResults.setVisibility(View.GONE);

        // Cancel previous call if exists
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }

        // Create API call based on filters
        if (filterOptions != null && filterOptions.getStoreId() != null) {
            currentCall = apiService.getGameList(
                    currentPage,
                    PAGE_SIZE,
                    currentQuery,
                    filterOptions.getGenreId(),
                    filterOptions.getPlatformId(),
                    null, // publisherId
                    filterOptions.getStoreId()
            );
        } else {
            currentCall = apiService.getGameList(
                    currentPage,
                    PAGE_SIZE,
                    currentQuery,
                    filterOptions.getGenreId(),
                    filterOptions.getPlatformId(),
                    null // publisherId
            );
        }

        currentCall.enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(@NonNull Call<GameListResponse> call, @NonNull Response<GameListResponse> response) {
                isLoading = false;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Game> games = parseGames(response.body());

                    // Apply client-side sorting since API doesn't support it
                    sortGames(games);

                    if (resetResults) {
                        gameAdapter.setGames(games);
                    } else {
                        gameAdapter.addGames(games);
                    }

                    hasMorePages = response.body().getNext() != null;

                    updateEmptyState();
                } else {
                    showError(getString(R.string.error_searching_games));
                }

                if (resetResults) {
                    binding.rvSearchResults.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GameListResponse> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    isLoading = false;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    showError(t.getMessage());
                    Log.e(TAG, "Network error", t);

                    if (resetResults) {
                        binding.rvSearchResults.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void sortGames(List<Game> games) {
        switch(currentSorting) {
            case "popularity":
                // Default sort is by popularity (metacritic), no need to sort
                break;
            case "released":
                Collections.sort(games, (g1, g2) -> {
                    if (g1.getReleased() == null) return 1;
                    if (g2.getReleased() == null) return -1;
                    return g2.getReleased().compareTo(g1.getReleased()); // Newest first
                });
                break;
            case "rating":
                Collections.sort(games, (g1, g2) -> Double.compare(g2.getRating(), g1.getRating())); // Highest first
                break;
            case "name_asc":
                Collections.sort(games, (g1, g2) -> {
                    if (g1.getName() == null) return 1;
                    if (g2.getName() == null) return -1;
                    return g1.getName().compareToIgnoreCase(g2.getName());
                });
                break;
            case "name_desc":
                Collections.sort(games, (g1, g2) -> {
                    if (g1.getName() == null) return 1;
                    if (g2.getName() == null) return -1;
                    return g2.getName().compareToIgnoreCase(g1.getName());
                });
                break;
        }
    }

    private void loadNextPage() {
        if (!isLoading && hasMorePages) {
            currentPage++;
            searchGames(currentQuery, false);
        }
    }

    private List<Game> parseGames(GameListResponse response) {
        List<Game> games = new ArrayList<>();

        if (response.getResults() != null) {
            // Use the correct class from your response
            for (GameResponse gameResponse : response.getResults()) {
                // Apply client-side filters that can't be done via API
                if (filterOptions.getMinRating() > 0 && gameResponse.getRating() < filterOptions.getMinRating()) {
                    continue; // Skip games below minimum rating
                }

                // Apply year filter if set
                if ((filterOptions.getFromYear() != null && !filterOptions.getFromYear().isEmpty())
                        || (filterOptions.getToYear() != null && !filterOptions.getToYear().isEmpty())) {

                    String releaseDate = gameResponse.getReleased();
                    if (releaseDate != null && releaseDate.length() >= 4) {
                        try {
                            int year = Integer.parseInt(releaseDate.substring(0, 4));

                            if (filterOptions.getFromYear() != null && !filterOptions.getFromYear().isEmpty()
                                    && year < Integer.parseInt(filterOptions.getFromYear())) {
                                continue; // Skip if released before fromYear
                            }

                            if (filterOptions.getToYear() != null && !filterOptions.getToYear().isEmpty()
                                    && year > Integer.parseInt(filterOptions.getToYear())) {
                                continue; // Skip if released after toYear
                            }
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Error parsing year from: " + releaseDate, e);
                        }
                    }
                }

                // Convert from GameResponse to Game model
                Game game = new Game();
                game.setId(gameResponse.getId());
                game.setName(gameResponse.getName());
                game.setReleased(gameResponse.getReleased());
                game.setBackgroundImage(gameResponse.getBackgroundImage());
                game.setRating(gameResponse.getRating());
                game.setFavorite(dbHelper.isGameFavorite(gameResponse.getId()));

                games.add(game);
            }
        }

        return games;
    }

    private void clearResults() {
        gameAdapter.setGames(new ArrayList<>());
        binding.tvNoResults.setVisibility(View.GONE);
        currentQuery = "";
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateEmptyState() {
        if (gameAdapter.getItemCount() == 0) {
            binding.tvNoResults.setVisibility(View.VISIBLE);
            binding.rvSearchResults.setVisibility(View.GONE);
        } else {
            binding.tvNoResults.setVisibility(View.GONE);
            binding.rvSearchResults.setVisibility(View.VISIBLE);
        }
    }

    private void updateActiveFiltersChips() {
        binding.chipGroupActiveFilters.removeAllViews();

        Map<String, String> activeFilters = filterOptions.getActiveFiltersMap();

        if (activeFilters.isEmpty()) {
            binding.chipGroupActiveFilters.setVisibility(View.GONE);
            return;
        }

        binding.chipGroupActiveFilters.setVisibility(View.VISIBLE);

        for (Map.Entry<String, String> entry : activeFilters.entrySet()) {
            Chip chip = new Chip(this);
            chip.setText(entry.getKey() + ": " + entry.getValue());
            chip.setCloseIconVisible(true);

            final String key = entry.getKey();
            chip.setOnCloseIconClickListener(v -> {
                removeFilter(key);
                updateActiveFiltersChips();
                refreshSearch();
            });

            binding.chipGroupActiveFilters.addView(chip);
        }
    }

    private void removeFilter(String filterType) {
        switch (filterType) {
            case "Genre":
                filterOptions.setGenreId(null);
                filterOptions.setGenreName(null);
                break;
            case "Platform":
                filterOptions.setPlatformId(null);
                filterOptions.setPlatformName(null);
                break;
            case "Store":
                filterOptions.setStoreId(null);
                filterOptions.setStoreName(null);
                break;
            case "Years":
                filterOptions.setFromYear(null);
                filterOptions.setToYear(null);
                break;
            case "Min Rating":
                filterOptions.setMinRating(0);
                break;
        }
    }

    @Override
    public void onFiltersApplied(FilterOptions filterOptions) {
        this.filterOptions = filterOptions;
        updateActiveFiltersChips();
        refreshSearch();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentCall != null) {
            currentCall.cancel();
        }
        searchHandler.removeCallbacks(searchRunnable);
    }
}