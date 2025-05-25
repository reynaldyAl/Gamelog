package com.example.gamemology.ui.home;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamemology.R;
import com.example.gamemology.adapter.GameAdapter;
import com.example.gamemology.api.ApiClient;
import com.example.gamemology.api.ApiService;
import com.example.gamemology.api.responses.GameListResponse;
import com.example.gamemology.api.responses.GameResponse;
import com.example.gamemology.database.DatabaseHelper;
import com.example.gamemology.databinding.FragmentHomeBinding;
import com.example.gamemology.models.Game;
import com.example.gamemology.ui.detail.DetailActivity;
import com.example.gamemology.utils.Constants;
import com.example.gamemology.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private GameAdapter gameAdapter;
    private ApiService apiService;
    private DatabaseHelper dbHelper;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private int currentPage = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API service and database helper
        apiService = ApiClient.getInstance().getApiService();
        dbHelper = DatabaseHelper.getInstance(requireContext());

        // Setup RecyclerView
        setupRecyclerView();

        // Setup SwipeRefreshLayout
        binding.swipeRefresh.setOnRefreshListener(this::refreshData);

        // Load initial data
        loadGames(1);

        // Show loading indicator initially
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView() {
        gameAdapter = new GameAdapter(requireContext());
        binding.rvGames.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGames.setAdapter(gameAdapter);

        // Set up item click listener
        gameAdapter.setOnItemClickListener(new GameAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Game game) {
                Intent intent = new Intent(requireContext(), DetailActivity.class);
                intent.putExtra(Constants.EXTRA_GAME_ID, game.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Game game, boolean isFavorite) {
                if (isFavorite) {
                    dbHelper.addGameToFavorites(game);
                    Toast.makeText(requireContext(), getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.removeGameFromFavorites(game.getId());
                    Toast.makeText(requireContext(), getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Setup pagination
        binding.rvGames.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            // Load more data
                            loadMoreGames();
                        }
                    }
                }
            }
        });
    }

    private void loadGames(int page) {
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            showNetworkError();
            return;
        }

        isLoading = true;
        currentPage = page;

        Call<GameListResponse> call = apiService.getGameList(page, Constants.PAGE_SIZE, null);
        call.enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(@NonNull Call<GameListResponse> call, @NonNull Response<GameListResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    GameListResponse gameListResponse = response.body();
                    hasMoreData = gameListResponse.getNext() != null;

                    List<Game> games = convertToGameModels(gameListResponse.getResults());

                    if (page == 1) {
                        // Fresh data
                        gameAdapter.setGames(games);
                        binding.rvGames.scrollToPosition(0);
                    } else {
                        // Pagination data
                        gameAdapter.addGames(games);
                    }

                    // Show empty view if no data
                    if (games.isEmpty() && page == 1) {
                        showEmptyView();
                    } else {
                        hideEmptyView();
                    }
                } else {
                    // Show error
                    showError(getString(R.string.error_loading_games));
                }
            }

            @Override
            public void onFailure(@NonNull Call<GameListResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                isLoading = false;
                showError(t.getMessage());
            }
        });
    }

    private void loadMoreGames() {
        if (isLoading || !hasMoreData) return;

        binding.loadMoreProgress.setVisibility(View.VISIBLE);
        loadGames(currentPage + 1);
    }

    private void refreshData() {
        loadGames(1);
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

    private void showEmptyView() {
        binding.emptyView.setVisibility(View.VISIBLE);
        binding.rvGames.setVisibility(View.GONE);
    }

    private void hideEmptyView() {
        binding.emptyView.setVisibility(View.GONE);
        binding.rvGames.setVisibility(View.VISIBLE);
    }

    private void showNetworkError() {
        binding.progressBar.setVisibility(View.GONE);
        binding.swipeRefresh.setRefreshing(false);
        Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        binding.progressBar.setVisibility(View.GONE);
        binding.swipeRefresh.setRefreshing(false);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}