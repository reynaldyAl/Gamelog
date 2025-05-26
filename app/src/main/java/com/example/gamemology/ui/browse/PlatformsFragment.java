package com.example.gamemology.ui.browse;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamemology.R;
import com.example.gamemology.adapter.CategoryAdapter;
import com.example.gamemology.api.ApiClient;
import com.example.gamemology.api.ApiService;
import com.example.gamemology.api.responses.PlatformResponse;
import com.example.gamemology.databinding.FragmentCategoryBinding;
import com.example.gamemology.models.Category;
import com.example.gamemology.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlatformsFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private ApiService apiService;
    private CategoryAdapter adapter;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getInstance().getApiService();

        setupRecyclerView();
        loadPlatforms(1);
    }

    private void setupRecyclerView() {
        adapter = new CategoryAdapter(requireContext());
        binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(category -> {
            Intent intent = new Intent(requireContext(), CategoryGamesActivity.class);
            intent.putExtra(Constants.EXTRA_CATEGORY_ID, category.getId());
            intent.putExtra(Constants.EXTRA_CATEGORY_NAME, category.getName());
            intent.putExtra(Constants.EXTRA_CATEGORY_TYPE, Constants.CATEGORY_TYPE_PLATFORM);
            startActivity(intent);
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 1;
            loadPlatforms(currentPage);
        });

        // Pagination
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && hasMoreData) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0) {
                            loadMorePlatforms();
                        }
                    }
                }
            }
        });
    }

    private void loadPlatforms(int page) {
        isLoading = true;
        binding.progressBar.setVisibility(View.VISIBLE);

        Call<PlatformResponse> call = apiService.getPlatforms(page, Constants.PAGE_SIZE);
        call.enqueue(new Callback<PlatformResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlatformResponse> call, @NonNull Response<PlatformResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    PlatformResponse platformResponse = response.body();
                    List<Category> platforms = convertToCategories(platformResponse.getResults());

                    hasMoreData = platformResponse.getNext() != null;

                    if (page == 1) {
                        adapter.setCategories(platforms);
                    } else {
                        adapter.addCategories(platforms);
                    }

                    if (adapter.getItemCount() == 0) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                    } else {
                        binding.emptyView.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.error_loading_platforms, Toast.LENGTH_SHORT).show();
                    if (adapter.getItemCount() == 0) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlatformResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                isLoading = false;

                Toast.makeText(requireContext(), R.string.error_loading_platforms, Toast.LENGTH_SHORT).show();
                if (adapter.getItemCount() == 0) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadMorePlatforms() {
        currentPage++;
        loadPlatforms(currentPage);
    }

    private List<Category> convertToCategories(List<PlatformResponse.Platform> platforms) {
        List<Category> categories = new ArrayList<>();
        if (platforms == null) return categories;

        for (PlatformResponse.Platform platform : platforms) {
            Category category = new Category();
            category.setId(platform.getId());
            category.setName(platform.getName());
            category.setImageUrl(platform.getImageBackground());
            category.setGamesCount(platform.getGamesCount());
            categories.add(category);
        }

        return categories;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}