package com.example.gamemology.ui.favorite;


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

import com.example.gamemology.R;
import com.example.gamemology.adapter.GameAdapter;
import com.example.gamemology.database.DatabaseHelper;
import com.example.gamemology.databinding.FragmentFavoriteBinding;
import com.example.gamemology.models.Game;
import com.example.gamemology.ui.detail.DetailActivity;
import com.example.gamemology.utils.Constants;

import java.util.List;

public class FavoriteFragment extends Fragment {

    private FragmentFavoriteBinding binding;
    private GameAdapter gameAdapter;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());

        // Setup RecyclerView
        setupRecyclerView();

        // Load favorite games
        loadFavoriteGames();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        loadFavoriteGames();
    }

    private void setupRecyclerView() {
        gameAdapter = new GameAdapter(requireContext());
        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFavorites.setAdapter(gameAdapter);

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
                if (!isFavorite) {
                    dbHelper.removeGameFromFavorites(game.getId());
                    Toast.makeText(requireContext(), getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
                    loadFavoriteGames(); // Reload the list
                }
            }
        });
    }

    private void loadFavoriteGames() {
        List<Game> favoriteGames = dbHelper.getAllFavoriteGames();
        gameAdapter.setGames(favoriteGames);

        // Show empty view if no favorite games
        if (favoriteGames.isEmpty()) {
            binding.emptyView.setVisibility(View.VISIBLE);
            binding.rvFavorites.setVisibility(View.GONE);
        } else {
            binding.emptyView.setVisibility(View.GONE);
            binding.rvFavorites.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}