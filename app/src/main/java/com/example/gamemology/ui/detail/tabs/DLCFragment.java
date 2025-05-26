package com.example.gamemology.ui.detail.tabs;

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
import com.example.gamemology.api.ApiClient;
import com.example.gamemology.api.ApiService;
import com.example.gamemology.api.responses.AdditionResponse;
import com.example.gamemology.database.DatabaseHelper;
import com.example.gamemology.databinding.FragmentDlcBinding;
import com.example.gamemology.models.Game;
import com.example.gamemology.ui.detail.DetailActivity;
import com.example.gamemology.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DLCFragment extends Fragment {

    private FragmentDlcBinding binding;
    private ApiService apiService;
    private DatabaseHelper dbHelper;
    private GameAdapter gameAdapter;
    private int gameId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDlcBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getInstance().getApiService();
        dbHelper = DatabaseHelper.getInstance(requireContext());

        // Get game ID from arguments
        if (getArguments() != null) {
            gameId = getArguments().getInt(Constants.EXTRA_GAME_ID, -1);

            if (gameId != -1) {
                setupRecyclerView();
                loadDLC();
            }
        }
    }

    private void setupRecyclerView() {
        gameAdapter = new GameAdapter(requireContext());
        binding.rvDlc.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDlc.setAdapter(gameAdapter);

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
                    Toast.makeText(requireContext(), R.string.added_to_favorites, Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.removeGameFromFavorites(game.getId());
                    Toast.makeText(requireContext(), R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void loadDLC() {
        Call<AdditionResponse> call = apiService.getGameAdditions(gameId);
        call.enqueue(new Callback<AdditionResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdditionResponse> call, @NonNull Response<AdditionResponse> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Game> dlcList = new ArrayList<>();
                    List<AdditionResponse.Addition> additions = response.body().getResults();

                    if (additions != null && !additions.isEmpty()) {
                        for (AdditionResponse.Addition addition : additions) {
                            Game dlc = new Game();
                            dlc.setId(addition.getId());
                            dlc.setName(addition.getName());
                            dlc.setBackgroundImage(addition.getBackgroundImage());
                            dlc.setReleased(addition.getReleased());
                            dlc.setRating(addition.getRating());

                            // Check if DLC is in favorites
                            dlc.setFavorite(dbHelper.isGameFavorite(addition.getId()));

                            dlcList.add(dlc);
                        }
                    }

                    gameAdapter.setGames(dlcList);

                    if (dlcList.isEmpty()) {
                        binding.tvEmptyDlc.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvEmptyDlc.setVisibility(View.GONE);
                    }
                } else {
                    binding.tvEmptyDlc.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdditionResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmptyDlc.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}