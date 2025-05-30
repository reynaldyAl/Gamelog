package com.example.gamemology.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gamemology.R;
import com.example.gamemology.api.ApiClient;
import com.example.gamemology.api.ApiService;
import com.example.gamemology.api.responses.GenreResponse;
import com.example.gamemology.api.responses.PlatformResponse;
import com.example.gamemology.databinding.BottomSheetFilterBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    public interface FilterListener {
        void onFiltersApplied(FilterOptions filterOptions);
    }

    public static class FilterOptions {
        private Integer genreId;
        private String genreName;
        private Integer platformId;
        private String platformName;
        private Integer storeId;
        private String storeName;
        private String fromYear;
        private String toYear;
        private int minRating;
        private String sortBy;

        // Getters and setters
        public Integer getGenreId() { return genreId; }
        public void setGenreId(Integer genreId) { this.genreId = genreId; }
        public String getGenreName() { return genreName; }
        public void setGenreName(String genreName) { this.genreName = genreName; }
        public Integer getPlatformId() { return platformId; }
        public void setPlatformId(Integer platformId) { this.platformId = platformId; }
        public String getPlatformName() { return platformName; }
        public void setPlatformName(String platformName) { this.platformName = platformName; }
        public Integer getStoreId() { return storeId; }
        public void setStoreId(Integer storeId) { this.storeId = storeId; }
        public String getStoreName() { return storeName; }
        public void setStoreName(String storeName) { this.storeName = storeName; }
        public String getFromYear() { return fromYear; }
        public void setFromYear(String fromYear) { this.fromYear = fromYear; }
        public String getToYear() { return toYear; }
        public void setToYear(String toYear) { this.toYear = toYear; }
        public int getMinRating() { return minRating; }
        public void setMinRating(int minRating) { this.minRating = minRating; }
        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }

        public boolean hasFilters() {
            return genreId != null || platformId != null || storeId != null ||
                    (fromYear != null && !fromYear.isEmpty()) ||
                    (toYear != null && !toYear.isEmpty()) ||
                    minRating > 0;
        }

        public Map<String, String> getActiveFiltersMap() {
            Map<String, String> filters = new HashMap<>();
            if (genreName != null) filters.put("Genre", genreName);
            if (platformName != null) filters.put("Platform", platformName);
            if (storeName != null) filters.put("Store", storeName);

            if ((fromYear != null && !fromYear.isEmpty()) || (toYear != null && !toYear.isEmpty())) {
                String yearRange = "";
                if (fromYear != null && !fromYear.isEmpty()) yearRange += fromYear;
                yearRange += " - ";
                if (toYear != null && !toYear.isEmpty()) yearRange += toYear;
                filters.put("Years", yearRange);
            }

            if (minRating > 0) filters.put("Min Rating", minRating + "+");

            return filters;
        }
    }

    private BottomSheetFilterBinding binding;
    private FilterListener listener;
    private ApiService apiService;
    private FilterOptions filterOptions;

    // Maps for storing names
    private final Map<Integer, String> genreNames = new HashMap<>();
    private final Map<Integer, String> platformNames = new HashMap<>();
    private final Map<Integer, String> storeNames = new HashMap<>();

    public static FilterBottomSheet newInstance(FilterOptions filters) {
        FilterBottomSheet fragment = new FilterBottomSheet();
        Bundle args = new Bundle();
        if (filters != null) {
            // We could serialize the FilterOptions if needed
            // For now we'll just pass the individual values
            if (filters.getGenreId() != null) args.putInt("genreId", filters.getGenreId());
            if (filters.getPlatformId() != null) args.putInt("platformId", filters.getPlatformId());
            if (filters.getStoreId() != null) args.putInt("storeId", filters.getStoreId());
            if (filters.getFromYear() != null) args.putString("fromYear", filters.getFromYear());
            if (filters.getToYear() != null) args.putString("toYear", filters.getToYear());
            args.putInt("minRating", filters.getMinRating());
            if (filters.getSortBy() != null) args.putString("sortBy", filters.getSortBy());
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setListener(FilterListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getInstance().getApiService();
        filterOptions = new FilterOptions();

        // Restore previous filter settings if they exist
        if (getArguments() != null) {
            if (getArguments().containsKey("genreId")) {
                filterOptions.setGenreId(getArguments().getInt("genreId"));
            }
            if (getArguments().containsKey("platformId")) {
                filterOptions.setPlatformId(getArguments().getInt("platformId"));
            }
            if (getArguments().containsKey("storeId")) {
                filterOptions.setStoreId(getArguments().getInt("storeId"));
            }
            if (getArguments().containsKey("fromYear")) {
                filterOptions.setFromYear(getArguments().getString("fromYear"));
                binding.etFromYear.setText(filterOptions.getFromYear());
            }
            if (getArguments().containsKey("toYear")) {
                filterOptions.setToYear(getArguments().getString("toYear"));
                binding.etToYear.setText(filterOptions.getToYear());
            }
            filterOptions.setMinRating(getArguments().getInt("minRating", 0));
            binding.sliderRating.setValue(filterOptions.getMinRating());
            if (getArguments().containsKey("sortBy")) {
                filterOptions.setSortBy(getArguments().getString("sortBy"));
            }
        }

        // Load filter options
        loadGenres();
        loadPlatforms();
        loadStores();

        // Set up button listeners
        binding.btnReset.setOnClickListener(v -> resetFilters());
        binding.btnApply.setOnClickListener(v -> applyFilters());
    }

    private void loadGenres() {
        binding.chipGroupGenres.removeAllViews();

        // Add loading indicator
        Chip loadingChip = new Chip(requireContext());
        loadingChip.setText(R.string.loading);
        binding.chipGroupGenres.addView(loadingChip);

        apiService.getGenres(1, 20).enqueue(new Callback<GenreResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenreResponse> call, @NonNull Response<GenreResponse> response) {
                binding.chipGroupGenres.removeAllViews();

                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    setupGenreChips(response.body().getResults());
                } else {
                    Toast.makeText(requireContext(), R.string.error_loading_genres, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenreResponse> call, @NonNull Throwable t) {
                binding.chipGroupGenres.removeAllViews();
                Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupGenreChips(List<GenreResponse.Genre> genres) {
        for (GenreResponse.Genre genre : genres) {
            // Store name for filter summary
            genreNames.put(genre.getId(), genre.getName());

            Chip chip = new Chip(requireContext());
            chip.setText(genre.getName());
            chip.setCheckable(true);
            chip.setChecked(filterOptions.getGenreId() != null &&
                    filterOptions.getGenreId() == genre.getId());

            final int genreId = genre.getId();
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filterOptions.setGenreId(genreId);
                    filterOptions.setGenreName(genre.getName());
                    uncheckOtherChips(binding.chipGroupGenres, chip.getId());
                } else if (filterOptions.getGenreId() != null &&
                        filterOptions.getGenreId() == genreId) {
                    filterOptions.setGenreId(null);
                    filterOptions.setGenreName(null);
                }
            });

            binding.chipGroupGenres.addView(chip);
        }
    }

    private void loadPlatforms() {
        binding.chipGroupPlatforms.removeAllViews();

        // Add loading indicator
        Chip loadingChip = new Chip(requireContext());
        loadingChip.setText(R.string.loading);
        binding.chipGroupPlatforms.addView(loadingChip);

        apiService.getParentPlatforms().enqueue(new Callback<PlatformResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlatformResponse> call, @NonNull Response<PlatformResponse> response) {
                binding.chipGroupPlatforms.removeAllViews();

                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    setupPlatformChips(response.body().getResults());
                } else {
                    Toast.makeText(requireContext(), R.string.error_loading_platforms, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlatformResponse> call, @NonNull Throwable t) {
                binding.chipGroupPlatforms.removeAllViews();
                Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupPlatformChips(List<PlatformResponse.Platform> platforms) {
        for (PlatformResponse.Platform platform : platforms) {
            // Store name for filter summary
            platformNames.put(platform.getId(), platform.getName());

            Chip chip = new Chip(requireContext());
            chip.setText(platform.getName());
            chip.setCheckable(true);
            chip.setChecked(filterOptions.getPlatformId() != null &&
                    filterOptions.getPlatformId() == platform.getId());

            final int platformId = platform.getId();
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filterOptions.setPlatformId(platformId);
                    filterOptions.setPlatformName(platform.getName());
                    uncheckOtherChips(binding.chipGroupPlatforms, chip.getId());
                } else if (filterOptions.getPlatformId() != null &&
                        filterOptions.getPlatformId() == platformId) {
                    filterOptions.setPlatformId(null);
                    filterOptions.setPlatformName(null);
                }
            });

            binding.chipGroupPlatforms.addView(chip);
        }
    }

    private void loadStores() {
        // Add major stores manually for predictable results
        binding.chipGroupStores.removeAllViews();

        Map<Integer, String> majorStores = new HashMap<>();
        majorStores.put(1, "Steam");
        majorStores.put(2, "Xbox Store");
        majorStores.put(3, "PlayStation Store");
        majorStores.put(4, "App Store");
        majorStores.put(5, "GOG");
        majorStores.put(6, "Nintendo Store");
        majorStores.put(7, "Xbox 360");
        majorStores.put(8, "Google Play");
        majorStores.put(9, "itch.io");
        majorStores.put(11, "Epic Games Store");

        for (Map.Entry<Integer, String> store : majorStores.entrySet()) {
            // Store name for filter summary
            storeNames.put(store.getKey(), store.getValue());

            Chip chip = new Chip(requireContext());
            chip.setText(store.getValue());
            chip.setCheckable(true);
            chip.setChecked(filterOptions.getStoreId() != null &&
                    filterOptions.getStoreId() == store.getKey());

            final int storeId = store.getKey();
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filterOptions.setStoreId(storeId);
                    filterOptions.setStoreName(store.getValue());
                    uncheckOtherChips(binding.chipGroupStores, chip.getId());
                } else if (filterOptions.getStoreId() != null &&
                        filterOptions.getStoreId() == storeId) {
                    filterOptions.setStoreId(null);
                    filterOptions.setStoreName(null);
                }
            });

            binding.chipGroupStores.addView(chip);
        }
    }

    private void uncheckOtherChips(ViewGroup chipGroup, int checkedId) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip && child.getId() != checkedId) {
                ((Chip) child).setChecked(false);
            }
        }
    }

    private void resetFilters() {
        filterOptions = new FilterOptions();

        // Reset UI
        binding.etFromYear.setText("");
        binding.etToYear.setText("");
        binding.sliderRating.setValue(0);

        // Uncheck all chips
        uncheckAllChips(binding.chipGroupGenres);
        uncheckAllChips(binding.chipGroupPlatforms);
        uncheckAllChips(binding.chipGroupStores);
    }

    private void uncheckAllChips(ViewGroup chipGroup) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setChecked(false);
            }
        }
    }

    private void applyFilters() {
        // Update filters with UI values
        filterOptions.setFromYear(binding.etFromYear.getText().toString().trim());
        filterOptions.setToYear(binding.etToYear.getText().toString().trim());
        filterOptions.setMinRating((int) binding.sliderRating.getValue());

        // Pass back to listener
        if (listener != null) {
            listener.onFiltersApplied(filterOptions);
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}