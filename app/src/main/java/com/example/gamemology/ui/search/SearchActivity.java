package com.example.gamemology.ui.search;


import android.content.Intent;
import android.os.Bundle;
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
import com.example.gamemology.utils.Constants;
import com.example.gamemology.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private GameAdapter gameAdapter;
    private ApiService apiService;
    private DatabaseHelper dbHelper;
    private String currentQuery = "";
    private Call<GameListResponse> currentCall;

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

        // Setup RecyclerView
        setupRecyclerView();

        // Setup search view
        setupSearchView();
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
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchGames(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Only search if text is not empty and at least 3 chars
                if (newText.length() >= 3) {
                    searchGames(newText);
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

    private void searchGames(String query) {
        if (query.equals(currentQuery)) {
            return; // Avoid duplicate searches
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
            return;
        }

        currentQuery = query;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvNoResults.setVisibility(View.GONE);

        // Cancel previous call if exists
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }

        currentCall = apiService.getGameList(1, Constants.PAGE_SIZE, query);
        currentCall.enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(@NonNull Call<GameListResponse> call, @NonNull Response<GameListResponse> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Game> games = convertToGameModels(response.body().getResults());
                    gameAdapter.setGames(games);

                    if (games.isEmpty()) {
                        binding.tvNoResults.setVisibility(View.VISIBLE);
                        binding.rvSearchResults.setVisibility(View.GONE);
                    } else {
                        binding.tvNoResults.setVisibility(View.GONE);
                        binding.rvSearchResults.setVisibility(View.VISIBLE);
                    }
                } else {
                    showError(getString(R.string.error_searching_games));
                }
            }

            @Override
            public void onFailure(@NonNull Call<GameListResponse> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    binding.progressBar.setVisibility(View.GONE);
                    showError(t.getMessage());
                }
            }
        });
    }

    private List<Game> convertToGameModels(List<GameResponse> gameResponses) {
        List<Game> games = new ArrayList<>();
        if (gameResponses == null) return games;

        for (GameResponse gameResponse : gameResponses) {
            Game game = new Game();
            game.setId(gameResponse.getId());
            game.setName(gameResponse.getName());
            game.setReleased(gameResponse.getReleased());
            game.setBackgroundImage(gameResponse.getBackgroundImage());
            game.setRating(gameResponse.getRating());

            // Check if the game is in favorites
            game.setFavorite(dbHelper.isGameFavorite(gameResponse.getId()));

            games.add(game);
        }
        return games;
    }

    private void clearResults() {
        gameAdapter.setGames(new ArrayList<>());
        binding.tvNoResults.setVisibility(View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
    }
}