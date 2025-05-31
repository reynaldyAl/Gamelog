package com.example.gamemology.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.gamemology.utils.LoadState;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;
    private GameAdapter gameAdapter;
    private HomeViewModel viewModel;
    private DatabaseHelper dbHelper;

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

        // Observe games result from ViewModel
        viewModel.getGamesResult().observe(getViewLifecycleOwner(), result -> {
            Log.d(TAG, "Games result updated: state=" + result.getState());

            // Handle different states
            if (result.getState() == LoadState.LOADING) {
                showLoading(true);
                hideLoadMoreProgress();
            } else if (result.getState() == LoadState.LOADING_MORE) {
                showLoadMoreProgress();
            } else {
                // Hide all loading indicators
                showLoading(false);
                hideLoadMoreProgress();
                binding.swipeRefresh.setRefreshing(false);

                if (result.isSuccess()) {
                    // Data loaded successfully
                    if (result.getData() != null && !result.getData().isEmpty()) {
                        Log.d(TAG, "Setting " + result.getData().size() + " games to adapter");
                        gameAdapter.setGames(result.getData());
                        showContent();
                    } else {
                        showEmptyView();
                    }
                } else if (result.isError()) {
                    // Show error
                    Toast.makeText(requireContext(),
                            result.getErrorMessage() != null ? result.getErrorMessage() : getString(R.string.error_loading_games),
                            Toast.LENGTH_SHORT).show();

                    // Show empty view if we have no data
                    if (gameAdapter.getItemCount() == 0) {
                        showEmptyView();
                    }
                }
            }
        });

        // Load initial data
        viewModel.loadFirstPage();
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");
        gameAdapter = new GameAdapter(requireContext());

        // Initialize with empty list
        gameAdapter.setGames(new ArrayList<>());

        binding.rvGames.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGames.setAdapter(gameAdapter);

        // Setup item click listeners
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

        // Setup pagination with scroll listener
        binding.rvGames.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Skip when scrolling up
                if (dy <= 0) return;

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // Check if near the end of the list
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                            && firstVisibleItemPosition >= 0) {
                        // Load more data
                        viewModel.loadNextPage();
                    }
                }
            }
        });
    }

    private void refreshData() {
        Log.d(TAG, "Refreshing data");
        binding.swipeRefresh.setRefreshing(true);
        viewModel.refreshGames();
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showLoadMoreProgress() {
        binding.loadMoreProgress.setVisibility(View.VISIBLE);
    }

    private void hideLoadMoreProgress() {
        binding.loadMoreProgress.setVisibility(View.GONE);
    }

    private void showEmptyView() {
        binding.emptyView.setVisibility(View.VISIBLE);
        binding.rvGames.setVisibility(View.GONE);
    }

    private void showContent() {
        binding.emptyView.setVisibility(View.GONE);
        binding.rvGames.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}