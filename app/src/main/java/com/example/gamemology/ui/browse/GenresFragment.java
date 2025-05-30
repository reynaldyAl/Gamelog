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
import com.example.gamemology.api.responses.GenreResponse;
import com.example.gamemology.databinding.FragmentCategoryBinding;
import com.example.gamemology.models.Category;
import com.example.gamemology.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenresFragment extends Fragment {

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
        loadGenres(1);
    }

    private void setupRecyclerView() {
        adapter = new CategoryAdapter(requireContext());
        binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(category -> {
            Intent intent = new Intent(requireContext(), CategoryGamesActivity.class);
            intent.putExtra(Constants.EXTRA_CATEGORY_ID, category.getId());
            intent.putExtra(Constants.EXTRA_CATEGORY_NAME, category.getName());
            intent.putExtra(Constants.EXTRA_CATEGORY_TYPE, Constants.CATEGORY_TYPE_GENRE);
            startActivity(intent);
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 1;
            loadGenres(currentPage);
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
                            loadMoreGenres();
                        }
                    }
                }
            }
        });
    }

    private void loadGenres(int page) {
        isLoading = true;
        binding.progressBar.setVisibility(View.VISIBLE);

        Call<GenreResponse> call = apiService.getGenres(page, Constants.PAGE_SIZE);
        call.enqueue(new Callback<GenreResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenreResponse> call, @NonNull Response<GenreResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    GenreResponse genreResponse = response.body();
                    List<Category> genres = convertToCategories(genreResponse.getResults());

                    hasMoreData = genreResponse.getNext() != null;

                    if (page == 1) {
                        adapter.setCategories(genres);
                    } else {
                        adapter.addCategories(genres);
                    }

                    if (adapter.getItemCount() == 0) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                    } else {
                        binding.emptyView.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.error_loading_genres, Toast.LENGTH_SHORT).show();
                    if (adapter.getItemCount() == 0) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenreResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                isLoading = false;

                Toast.makeText(requireContext(), R.string.error_loading_genres, Toast.LENGTH_SHORT).show();
                if (adapter.getItemCount() == 0) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadMoreGenres() {
        currentPage++;
        loadGenres(currentPage);
    }

    private List<Category> convertToCategories(List<GenreResponse.Genre> genres) {
        List<Category> categories = new ArrayList<>();
        if (genres == null) return categories;

        for (GenreResponse.Genre genre : genres) {
            Category category = new Category();
            category.setId(genre.getId());
            category.setName(genre.getName());
            category.setImageUrl(genre.getImageBackground());
            category.setGamesCount(genre.getGamesCount());
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