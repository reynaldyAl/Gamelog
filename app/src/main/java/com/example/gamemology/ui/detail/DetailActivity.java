package com.example.gamemology.ui.detail;

import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gamemology.R;
import com.example.gamemology.adapter.ScreenshotAdapter;
import com.example.gamemology.api.ApiClient;
import com.example.gamemology.api.ApiService;
import com.example.gamemology.api.responses.GameListResponse;
import com.example.gamemology.api.responses.GameResponse;
import com.example.gamemology.database.DatabaseHelper;
import com.example.gamemology.databinding.ActivityDetailBinding;
import com.example.gamemology.models.Game;
import com.example.gamemology.models.Screenshot;
import com.example.gamemology.utils.Constants;
import com.example.gamemology.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private ApiService apiService;
    private DatabaseHelper dbHelper;
    private ScreenshotAdapter screenshotAdapter;
    private int gameId;
    private Game currentGame;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize API service and database helper
        apiService = ApiClient.getInstance().getApiService();
        dbHelper = DatabaseHelper.getInstance(this);

        // Setup screenshot RecyclerView
        setupScreenshotRecyclerView();

        // Get game ID from intent
        if (getIntent().hasExtra(Constants.EXTRA_GAME_ID)) {
            gameId = getIntent().getIntExtra(Constants.EXTRA_GAME_ID, -1);
            if (gameId != -1) {
                // Check if game is in favorites
                isFavorite = dbHelper.isGameFavorite(gameId);
                updateFavoriteButton();

                // Load game details
                loadGameDetails(gameId);

                // Load screenshots
                loadGameScreenshots(gameId);
            } else {
                Toast.makeText(this, R.string.error_game_not_found, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, R.string.error_game_not_found, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Setup favorite button
        binding.fabFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void setupScreenshotRecyclerView() {
        screenshotAdapter = new ScreenshotAdapter(this);
        binding.rvScreenshots.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvScreenshots.setAdapter(screenshotAdapter);
    }

    private void loadGameDetails(int gameId) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.contentLayout.setVisibility(View.GONE);

        Call<GameResponse> call = apiService.getGameDetail(gameId);
        call.enqueue(new Callback<GameResponse>() {
            @Override
            public void onResponse(@NonNull Call<GameResponse> call, @NonNull Response<GameResponse> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    GameResponse gameResponse = response.body();
                    displayGameDetails(gameResponse);
                    binding.contentLayout.setVisibility(View.VISIBLE);
                } else {
                    showError(getString(R.string.error_loading_game_details));
                }
            }

            @Override
            public void onFailure(@NonNull Call<GameResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                showError(t.getMessage());
            }
        });
    }

    private void loadGameScreenshots(int gameId) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            return;
        }

        Call<GameListResponse> call = apiService.getGameScreenshots(gameId);
        call.enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(@NonNull Call<GameListResponse> call, @NonNull Response<GameListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Screenshot> screenshots = new ArrayList<>();
                    List<GameResponse> screenshotResults = response.body().getResults();

                    if (screenshotResults != null) {
                        for (GameResponse result : screenshotResults) {
                            if (result.getScreenshots() != null) {
                                for (GameResponse.ScreenshotResponse screenshotResponse : result.getScreenshots()) {
                                    screenshots.add(new Screenshot(
                                            screenshotResponse.getId(),
                                            screenshotResponse.getImage()
                                    ));
                                }
                            } else if (result.getBackgroundImage() != null) {
                                // Fallback: use background image if no screenshots
                                screenshots.add(new Screenshot(
                                        result.getId(),
                                        result.getBackgroundImage()
                                ));
                            }
                        }
                    }

                    screenshotAdapter.setScreenshots(screenshots);

                    // Show or hide screenshots section
                    if (screenshots.isEmpty()) {
                        binding.screenshotsSection.setVisibility(View.GONE);
                    } else {
                        binding.screenshotsSection.setVisibility(View.VISIBLE);
                    }
                }
            }


            @Override
            public void onFailure(@NonNull Call<GameListResponse> call, @NonNull Throwable t) {
                // Hide screenshots section on error
                binding.screenshotsSection.setVisibility(View.GONE);
            }
        });
    }

    private void displayGameDetails(GameResponse gameResponse) {
        // Create game object from response
        currentGame = new Game();
        currentGame.setId(gameResponse.getId());
        currentGame.setName(gameResponse.getName());
        currentGame.setReleased(gameResponse.getReleased());
        currentGame.setBackgroundImage(gameResponse.getBackgroundImage());
        currentGame.setRating(gameResponse.getRating());
        currentGame.setDescription(gameResponse.getDescription());

        // Set genres
        if (gameResponse.getGenres() != null) {
            List<String> genres = gameResponse.getGenres().stream()
                    .map(GameResponse.GenreResponse::getName)
                    .collect(Collectors.toList());
            currentGame.setGenres(genres);
        }

        // Set platforms
        if (gameResponse.getPlatforms() != null) {
            List<String> platforms = gameResponse.getPlatforms().stream()
                    .map(platformWrapper -> platformWrapper.getPlatform().getName())
                    .collect(Collectors.toList());
            currentGame.setPlatforms(platforms);
        }

        // Update UI with game data
        setTitle(currentGame.getName());

        binding.tvTitle.setText(currentGame.getName());
        binding.tvReleaseDate.setText(getString(R.string.released_date, currentGame.getReleased()));
        binding.tvRating.setText(getString(R.string.rating, currentGame.getRating()));

        // Load image
        if (currentGame.getBackgroundImage() != null && !currentGame.getBackgroundImage().isEmpty()) {
            Glide.with(this)
                    .load(currentGame.getBackgroundImage())
                    .placeholder(R.drawable.placeholder_game)
                    .error(R.drawable.placeholder_game)
                    .into(binding.imgGame);
        }

        // Set description
        if (currentGame.getDescription() != null && !currentGame.getDescription().isEmpty()) {
            binding.tvDescription.setText(Html.fromHtml(currentGame.getDescription(), Html.FROM_HTML_MODE_COMPACT));
            binding.descriptionSection.setVisibility(View.VISIBLE);
        } else {
            binding.descriptionSection.setVisibility(View.GONE);
        }

        // Set genres
        if (currentGame.getGenres() != null && !currentGame.getGenres().isEmpty()) {
            binding.tvGenres.setText(String.join(", ", currentGame.getGenres()));
            binding.genresSection.setVisibility(View.VISIBLE);
        } else {
            binding.genresSection.setVisibility(View.GONE);
        }

        // Set platforms
        if (currentGame.getPlatforms() != null && !currentGame.getPlatforms().isEmpty()) {
            binding.tvPlatforms.setText(String.join(", ", currentGame.getPlatforms()));
            binding.platformsSection.setVisibility(View.VISIBLE);
        } else {
            binding.platformsSection.setVisibility(View.GONE);
        }
    }

    private void toggleFavorite() {
        if (currentGame == null) return;

        isFavorite = !isFavorite;

        if (isFavorite) {
            dbHelper.addGameToFavorites(currentGame);
            Toast.makeText(this, R.string.added_to_favorites, Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.removeGameFromFavorites(currentGame.getId());
            Toast.makeText(this, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
        }

        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        binding.fabFavorite.setImageResource(isFavorite ?
                R.drawable.ic_favorite : R.drawable.ic_favorite_border);
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
}