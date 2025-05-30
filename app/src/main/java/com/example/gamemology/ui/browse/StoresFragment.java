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

import com.example.gamemology.R;
import com.example.gamemology.adapter.CategoryAdapter;
import com.example.gamemology.api.ApiClient;
import com.example.gamemology.api.ApiService;
import com.example.gamemology.api.responses.StoreResponse;
import com.example.gamemology.databinding.FragmentCategoryBinding;
import com.example.gamemology.models.Category;
import com.example.gamemology.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoresFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private ApiService apiService;
    private CategoryAdapter adapter;

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
        loadStores();
    }

    private void setupRecyclerView() {
        adapter = new CategoryAdapter(requireContext());
        binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(category -> {
            Intent intent = new Intent(requireContext(), CategoryGamesActivity.class);
            intent.putExtra(Constants.EXTRA_CATEGORY_ID, category.getId());
            intent.putExtra(Constants.EXTRA_CATEGORY_NAME, category.getName());
            intent.putExtra(Constants.EXTRA_CATEGORY_TYPE, Constants.CATEGORY_TYPE_STORE);
            startActivity(intent);
        });

        binding.swipeRefresh.setOnRefreshListener(this::loadStores);
    }

    private void loadStores() {
        binding.progressBar.setVisibility(View.VISIBLE);

        Call<StoreResponse> call = apiService.getStores();
        call.enqueue(new Callback<StoreResponse>() {
            @Override
            public void onResponse(@NonNull Call<StoreResponse> call, @NonNull Response<StoreResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Category> stores = convertToCategories(response.body().getResults());
                    adapter.setCategories(stores);

                    if (stores.isEmpty()) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                    } else {
                        binding.emptyView.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.error_loading_stores, Toast.LENGTH_SHORT).show();
                    binding.emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<StoreResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                Toast.makeText(requireContext(), R.string.error_loading_stores, Toast.LENGTH_SHORT).show();
                binding.emptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    private List<Category> convertToCategories(List<StoreResponse.StoreItem> storeItems) {
        List<Category> categories = new ArrayList<>();
        if (storeItems == null) return categories;

        for (StoreResponse.StoreItem storeItem : storeItems) {
            if (storeItem.getStore() != null) {
                Category category = new Category();
                category.setId(storeItem.getStore().getId());
                category.setName(storeItem.getStore().getName());
                category.setImageUrl(storeItem.getStore().getImageBackground());
                categories.add(category);
            }
        }

        return categories;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}