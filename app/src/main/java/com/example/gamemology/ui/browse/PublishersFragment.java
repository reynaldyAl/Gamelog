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
import com.example.gamemology.api.responses.PublisherResponse;
import com.example.gamemology.databinding.FragmentCategoryBinding;
import com.example.gamemology.models.Category;
import com.example.gamemology.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublishersFragment extends Fragment {

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
        loadPublishers(1);
    }

    private void setupRecyclerView() {
        adapter = new CategoryAdapter(requireContext());
        binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(category -> {
            Intent intent = new Intent(requireContext(), CategoryGamesActivity.class);
            intent.putExtra(Constants.EXTRA_CATEGORY_ID, category.getId());
            intent.putExtra(Constants.EXTRA_CATEGORY_NAME, category.getName());
            intent.putExtra(Constants.EXTRA_CATEGORY_TYPE, Constants.CATEGORY_TYPE_PUBLISHER);
            startActivity(intent);
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 1;
            loadPublishers(currentPage);
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
                            loadMorePublishers();
                        }
                    }
                }
            }
        });
    }

    private void loadPublishers(int page) {
        isLoading = true;
        binding.progressBar.setVisibility(View.VISIBLE);

        Call<PublisherResponse> call = apiService.getPublishers(page, Constants.PAGE_SIZE);
        call.enqueue(new Callback<PublisherResponse>() {
            @Override
            public void onResponse(@NonNull Call<PublisherResponse> call, @NonNull Response<PublisherResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    PublisherResponse publisherResponse = response.body();
                    List<Category> publishers = convertToCategories(publisherResponse.getResults());

                    hasMoreData = publisherResponse.getNext() != null;

                    if (page == 1) {
                        adapter.setCategories(publishers);
                    } else {
                        adapter.addCategories(publishers);
                    }

                    if (adapter.getItemCount() == 0) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                    } else {
                        binding.emptyView.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.error_loading_publishers, Toast.LENGTH_SHORT).show();
                    if (adapter.getItemCount() == 0) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PublisherResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                isLoading = false;

                Toast.makeText(requireContext(), R.string.error_loading_publishers, Toast.LENGTH_SHORT).show();
                if (adapter.getItemCount() == 0) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadMorePublishers() {
        currentPage++;
        loadPublishers(currentPage);
    }

    private List<Category> convertToCategories(List<PublisherResponse.Publisher> publishers) {
        List<Category> categories = new ArrayList<>();
        if (publishers == null) return categories;

        for (PublisherResponse.Publisher publisher : publishers) {
            Category category = new Category();
            category.setId(publisher.getId());
            category.setName(publisher.getName());
            category.setImageUrl(publisher.getImageBackground());
            category.setGamesCount(publisher.getGamesCount());
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