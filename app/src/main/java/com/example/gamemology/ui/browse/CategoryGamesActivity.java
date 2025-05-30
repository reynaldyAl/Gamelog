package com.example.gamemology.ui.browse;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamemology.R;
import com.example.gamemology.adapter.GameAdapter;
import com.example.gamemology.api.ApiClient;
import com.example.gamemology.api.ApiService;
import com.example.gamemology.api.responses.GameListResponse;
import com.example.gamemology.api.responses.GameResponse;
import com.example.gamemology.database.DatabaseHelper;
import com.example.gamemology.databinding.ActivityCategoryGamesBinding;
import com.example.gamemology.models.Game;
import com.example.gamemology.ui.detail.DetailActivity;
import com.example.gamemology.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryGamesActivity extends AppCompatActivity {

    private ActivityCategoryGamesBinding binding;
    private ApiService apiService;
    private DatabaseHelper dbHelper;
    private GameAdapter adapter;

    private int categoryId;
    private String categoryName;
    private String categoryType;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryGamesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance().getApiService();
        dbHelper = DatabaseHelper.getInstance(this);

        // Get category details from intent
        if (getIntent().hasExtra(Constants.EXTRA_CATEGORY_ID) &&
                getIntent().hasExtra(Constants.EXTRA_CATEGORY_NAME) &&
                getIntent().hasExtra(Constants.EXTRA_CATEGORY_TYPE)) {

            categoryId = getIntent().getIntExtra(Constants.EXTRA_CATEGORY_ID, 0);
            categoryName = getIntent().getStringExtra(Constants.EXTRA_CATEGORY_NAME);
            categoryType = getIntent().getStringExtra(Constants.EXTRA_CATEGORY_TYPE);

            // Setup toolbar
            setSupportActionBar(binding.toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(categoryName);
            }

            setupRecyclerView();
            loadGames(1);
        } else {
            Toast.makeText(this, R.string.error_loading_category, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRecyclerView() {
        adapter = new GameAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new GameAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Game game) {
                Intent intent = new Intent(CategoryGamesActivity.this, DetailActivity.class);
                intent.putExtra(Constants.EXTRA_GAME_ID, game.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Game game, boolean isFavorite) {
                if (isFavorite) {
                    dbHelper.addGameToFavorites(game);
                    Toast.makeText(CategoryGamesActivity.this, R.string.added_to_favorites, Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.removeGameFromFavorites(game.getId());
                    Toast.makeText(CategoryGamesActivity.this, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 1;
            loadGames(currentPage);
        });

        // Add scroll listener for pagination
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && hasMoreData) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0) {
                            loadMoreGames();
                        }
                    }
                }
            }
        });
    }

    private void loadGames(int page) {
        isLoading = true;
        binding.progressBar.setVisibility(View.VISIBLE);

        Call<GameListResponse> call;

        switch (categoryType) {
            case Constants.CATEGORY_TYPE_GENRE:
                call = apiService.getGameList(page, Constants.PAGE_SIZE, null, categoryId, null, null);
                break;
            case Constants.CATEGORY_TYPE_PLATFORM:
                call = apiService.getGameList(page, Constants.PAGE_SIZE, null, null, categoryId, null);
                break;
            case Constants.CATEGORY_TYPE_PUBLISHER:
                call = apiService.getGameList(page, Constants.PAGE_SIZE, null, null, null, categoryId);
                break;
            case Constants.CATEGORY_TYPE_STORE:
                call = apiService.getGameList(page, Constants.PAGE_SIZE, null, null, null, null, categoryId);
                break;
            default:
                call = apiService.getGameList(page, Constants.PAGE_SIZE, null);
                break;
        }

        call.enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(@NonNull Call<GameListResponse> call, @NonNull Response<GameListResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    GameListResponse gameListResponse = response.body();
                    List<Game> games = convertToGames(gameListResponse.getResults());

                    hasMoreData = gameListResponse.getNext() != null;

                    if (page == 1) {
                        adapter.setGames(games);
                    } else {
                        adapter.addGames(games);
                    }

                    if (adapter.getItemCount() == 0) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                    } else {
                        binding.emptyView.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(CategoryGamesActivity.this, R.string.error_loading_games, Toast.LENGTH_SHORT).show();
                    if (adapter.getItemCount() == 0) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GameListResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                isLoading = false;

                Toast.makeText(CategoryGamesActivity.this, R.string.error_loading_games, Toast.LENGTH_SHORT).show();
                if (adapter.getItemCount() == 0) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadMoreGames() {
        if (!isLoading && hasMoreData) {
            currentPage++;
            loadGames(currentPage);
        }
    }

    private List<Game> convertToGames(List<GameResponse> gameResponses) {
        List<Game> games = new ArrayList<>();
        if (gameResponses == null) return games;

        for (GameResponse response : gameResponses) {
            Game game = new Game();
            game.setId(response.getId());
            game.setName(response.getName());
            game.setReleased(response.getReleased());
            game.setBackgroundImage(response.getBackgroundImage());
            game.setRating(response.getRating());

            // Check if game is in favorites
            game.setFavorite(dbHelper.isGameFavorite(response.getId()));

            games.add(game);
        }

        return games;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}