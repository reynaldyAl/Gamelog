package com.example.gamemology.ui.detail.tabs;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gamemology.R;
import com.example.gamemology.adapter.StoreAdapter;
import com.example.gamemology.api.ApiClient;
import com.example.gamemology.api.ApiService;
import com.example.gamemology.api.responses.StoreResponse;
import com.example.gamemology.databinding.FragmentAboutBinding;
import com.example.gamemology.models.Game;
import com.example.gamemology.utils.Constants;
import com.google.android.material.chip.Chip;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AboutFragment extends Fragment {

    private FragmentAboutBinding binding;
    private ApiService apiService;
    private Game game;
    private int gameId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getInstance().getApiService();

        // Get arguments
        if (getArguments() != null) {
            gameId = getArguments().getInt(Constants.EXTRA_GAME_ID, -1);
            game = getArguments().getParcelable(Constants.EXTRA_GAME);

            if (game != null) {
                // Set description
                if (game.getDescription() != null && !game.getDescription().isEmpty()) {
                    binding.tvDescription.setText(Html.fromHtml(game.getDescription(), Html.FROM_HTML_MODE_COMPACT));
                } else {
                    binding.tvDescriptionTitle.setVisibility(View.GONE);
                    binding.tvDescription.setVisibility(View.GONE);
                }

                // Set genres
                if (game.getGenres() != null && !game.getGenres().isEmpty()) {
                    for (String genre : game.getGenres()) {
                        Chip chip = new Chip(requireContext());
                        chip.setText(genre);
                        binding.chipGroupGenres.addView(chip);
                    }
                } else {
                    binding.tvGenresTitle.setVisibility(View.GONE);
                    binding.chipGroupGenres.setVisibility(View.GONE);
                }

                // Set platforms
                if (game.getPlatforms() != null && !game.getPlatforms().isEmpty()) {
                    for (String platform : game.getPlatforms()) {
                        Chip chip = new Chip(requireContext());
                        chip.setText(platform);
                        binding.chipGroupPlatforms.addView(chip);
                    }
                } else {
                    binding.tvPlatformsTitle.setVisibility(View.GONE);
                    binding.chipGroupPlatforms.setVisibility(View.GONE);
                }

                // Load stores
                if (gameId != -1) {
                    loadGameStores(gameId);
                }
            }
        }
    }

    // Replace only the loadGameStores method
    private void loadGameStores(int gameId) {
        Log.d("AboutFragment", "Loading stores for game ID: " + gameId);

        Call<StoreResponse> call = apiService.getGameStores(gameId);
        call.enqueue(new Callback<StoreResponse>() {
            @Override
            public void onResponse(@NonNull Call<StoreResponse> call, @NonNull Response<StoreResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<StoreResponse.StoreItem> results = response.body().getResults();

                    if (results != null && !results.isEmpty()) {
                        Log.d("AboutFragment", "Received " + results.size() + " stores");

                        // Debug the received store data
                        for (int i = 0; i < results.size(); i++) {
                            StoreResponse.StoreItem item = results.get(i);
                            if (item != null && item.getStore() != null) {
                                Log.d("AboutFragment", "Store " + i + ": " +
                                        "id=" + item.getId() +
                                        ", name=" + item.getStore().getName() +
                                        ", slug=" + item.getStore().getSlug() +
                                        ", url=" + item.getUrl());
                            } else {
                                Log.e("AboutFragment", "Store " + i + " is null or invalid");
                            }
                        }

                        // Create and set adapter
                        StoreAdapter adapter = new StoreAdapter(requireContext(), results);
                        binding.rvStores.setLayoutManager(new LinearLayoutManager(requireContext()));
                        binding.rvStores.setAdapter(adapter);

                        // Make UI elements visible
                        binding.tvStoresTitle.setVisibility(View.VISIBLE);
                        binding.rvStores.setVisibility(View.VISIBLE);
                    } else {
                        Log.d("AboutFragment", "No stores available for this game");
                        binding.tvStoresTitle.setVisibility(View.GONE);
                        binding.rvStores.setVisibility(View.GONE);
                    }
                } else {
                    int errorCode = response.code();
                    String errorBody = null;
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("AboutFragment", "Error reading error body: " + e.getMessage());
                    }

                    Log.e("AboutFragment", "Failed to load stores. Code: " + errorCode +
                            ", Error: " + errorBody);
                    binding.tvStoresTitle.setVisibility(View.GONE);
                    binding.rvStores.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<StoreResponse> call, @NonNull Throwable t) {
                Log.e("AboutFragment", "Network error loading stores: " + t.getMessage(), t);
                binding.tvStoresTitle.setVisibility(View.GONE);
                binding.rvStores.setVisibility(View.GONE);
            }
        });
    }
}