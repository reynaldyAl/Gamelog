package com.example.gamemology.ui.detail.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamemology.adapter.AchievementAdapter;
import com.example.gamemology.api.ApiClient;
import com.example.gamemology.api.ApiService;
import com.example.gamemology.api.responses.AchievementResponse;
import com.example.gamemology.databinding.FragmentAchievementsBinding;
import com.example.gamemology.models.Achievement;
import com.example.gamemology.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AchievementsFragment extends Fragment {

    private FragmentAchievementsBinding binding;
    private ApiService apiService;
    private AchievementAdapter achievementAdapter;
    private int gameId;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAchievementsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getInstance().getApiService();

        // Get game ID from arguments
        if (getArguments() != null) {
            gameId = getArguments().getInt(Constants.EXTRA_GAME_ID, -1);

            if (gameId != -1) {
                setupRecyclerView();
                loadAchievements(1);
            }
        }
    }

    private void setupRecyclerView() {
        achievementAdapter = new AchievementAdapter(requireContext());
        binding.rvAchievements.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAchievements.setAdapter(achievementAdapter);

        // Add scroll listener for pagination
        binding.rvAchievements.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            // Load more achievements
                            loadMoreAchievements();
                        }
                    }
                }
            }
        });

        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void loadAchievements(int page) {
        isLoading = true;
        currentPage = page;

        Call<AchievementResponse> call = apiService.getGameAchievements(gameId, page, Constants.PAGE_SIZE);
        call.enqueue(new Callback<AchievementResponse>() {
            @Override
            public void onResponse(@NonNull Call<AchievementResponse> call, @NonNull Response<AchievementResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    AchievementResponse achievementResponse = response.body();
                    List<Achievement> achievements = convertToAchievements(achievementResponse.getResults());

                    hasMoreData = achievementResponse.getNext() != null;

                    if (page == 1) {
                        achievementAdapter.setAchievements(achievements);

                        if (achievements.isEmpty()) {
                            binding.tvEmptyAchievements.setVisibility(View.VISIBLE);
                        } else {
                            binding.tvEmptyAchievements.setVisibility(View.GONE);
                        }
                    } else {
                        achievementAdapter.addAchievements(achievements);
                    }
                } else {
                    if (page == 1) {
                        binding.tvEmptyAchievements.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AchievementResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                isLoading = false;

                if (page == 1) {
                    binding.tvEmptyAchievements.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadMoreAchievements() {
        if (isLoading || !hasMoreData) return;

        loadAchievements(currentPage + 1);
    }

    private List<Achievement> convertToAchievements(List<AchievementResponse.Achievement> achievementResponses) {
        List<Achievement> achievements = new ArrayList<>();
        if (achievementResponses == null) return achievements;

        for (AchievementResponse.Achievement response : achievementResponses) {
            Achievement achievement = new Achievement();
            achievement.setId(response.getId());
            achievement.setName(response.getName());
            achievement.setDescription(response.getDescription());
            achievement.setImage(response.getImage());
            achievement.setPercent(response.getPercent());
            achievements.add(achievement);
        }

        return achievements;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}