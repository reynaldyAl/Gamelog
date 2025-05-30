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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamemology.R;
import com.example.gamemology.adapter.GameAdapter;
import com.example.gamemology.database.DatabaseHelper;
import com.example.gamemology.databinding.FragmentHomeBinding;
import com.example.gamemology.models.Game;
import com.example.gamemology.ui.detail.DetailActivity;
import com.example.gamemology.utils.Constants;
import com.example.gamemology.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private GameAdapter gameAdapter;
    private HomeViewModel viewModel;
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

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Initialize database helper
        dbHelper = DatabaseHelper.getInstance(requireContext());

        // Setup RecyclerView
        setupRecyclerView();

        // Setup SwipeRefreshLayout
        binding.swipeRefresh.setOnRefreshListener(this::refreshData);

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            isLoading = loading;
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        // Load initial data
        loadGamesPage(1);

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
                // Update favorite status in adapter
                gameAdapter.notifyDataSetChanged();
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

    private void loadGamesPage(int page) {
        currentPage = page;

        // Start with loading indicator for page 1
        if (page == 1) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.loadMoreProgress.setVisibility(View.VISIBLE);
        }

        // Get page from repository through ViewModel
        String category = "home_page_" + page;
        viewModel.loadGamesForPage(page, category).observe(getViewLifecycleOwner(), result -> {
            // Hide loading indicators
            binding.progressBar.setVisibility(View.GONE);
            binding.loadMoreProgress.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);

            if (result != null) {
                List<Game> games = result.first;
                hasMoreData = result.second;

                if (games != null && !games.isEmpty()) {
                    if (page == 1) {
                        // Fresh data
                        gameAdapter.setGames(games);
                        binding.rvGames.scrollToPosition(0);
                        hideEmptyView();
                    } else {
                        // Pagination data
                        gameAdapter.addGames(games);
                    }
                } else if (page == 1) {
                    showEmptyView();
                }
            } else {
                // No result from repository
                if (page == 1) {
                    // Only show empty view for first page failure
                    showEmptyView();
                }

                // If we're offline with no cache, show a friendly message
                if (!NetworkUtils.isNetworkAvailable(requireContext())) {
                    Toast.makeText(requireContext(), R.string.offline_no_cache, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadMoreGames() {
        if (isLoading || !hasMoreData) return;
        loadGamesPage(currentPage + 1);
    }

    private void refreshData() {
        binding.swipeRefresh.setRefreshing(true);
        // Force refresh from network
        viewModel.refreshGames();
        loadGamesPage(1);
    }

    private void showEmptyView() {
        binding.emptyView.setVisibility(View.VISIBLE);
        binding.rvGames.setVisibility(View.GONE);
    }

    private void hideEmptyView() {
        binding.emptyView.setVisibility(View.GONE);
        binding.rvGames.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}