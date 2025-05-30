package com.example.gamemology.ui.detail.tabs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gamemology.R;
import com.example.gamemology.adapter.ScreenshotAdapter;
import com.example.gamemology.adapter.TrailerAdapter;
import com.example.gamemology.api.ApiClient;
import com.example.gamemology.api.ApiService;
import com.example.gamemology.api.responses.ScreenshotResponse;
import com.example.gamemology.api.responses.MovieResponse;
import com.example.gamemology.databinding.FragmentMediaBinding;
import com.example.gamemology.models.Screenshot;
import com.example.gamemology.models.Trailer;
import com.example.gamemology.ui.detail.ImageViewerActivity;
import com.example.gamemology.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MediaFragment extends Fragment {
    private static final String TAG = "MediaFragment";
    private FragmentMediaBinding binding;
    private ApiService apiService;
    private int gameId;
    private ScreenshotAdapter screenshotAdapter;
    private TrailerAdapter trailerAdapter;
    private boolean hasScreenshots = false;
    private boolean hasTrailers = false;
    private boolean screenshotSectionProcessed = false;
    private boolean trailerSectionProcessed = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMediaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated started");

        apiService = ApiClient.getInstance().getApiService();

        // Reset processing flags
        screenshotSectionProcessed = false;
        trailerSectionProcessed = false;

        // Clear any previous data
        if (screenshotAdapter != null) {
            screenshotAdapter.clearItems();
        }
        if (trailerAdapter != null) {
            trailerAdapter.clearItems();
        }

        // Get game ID from arguments
        if (getArguments() != null) {
            gameId = getArguments().getInt(Constants.EXTRA_GAME_ID, -1);
            Log.d(TAG, "Media Fragment: Loading media for game ID: " + gameId);

            if (gameId != -1) {
                setupRecyclerViews();
                loadScreenshots();
            } else {
                Log.e(TAG, "Invalid game ID: -1");
                showError(getString(R.string.error_loading_media));
            }
        } else {
            Log.e(TAG, "No arguments provided to fragment");
            showError(getString(R.string.error_loading_media));
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        if (binding != null) {
            binding.progressBar.setVisibility(View.GONE);
            binding.tvEmptyMedia.setText(message);
            binding.tvEmptyMedia.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerViews() {
        Log.d(TAG, "Setting up RecyclerViews");

        // Setup Screenshots RecyclerView
        screenshotAdapter = new ScreenshotAdapter(requireContext());
        GridLayoutManager screenshotLayoutManager = new GridLayoutManager(requireContext(), 2);
        binding.rvScreenshots.setLayoutManager(screenshotLayoutManager);
        binding.rvScreenshots.setHasFixedSize(true);
        binding.rvScreenshots.setAdapter(screenshotAdapter);
        binding.tvScreenshotsTitle.setVisibility(View.VISIBLE);
        binding.rvScreenshots.setVisibility(View.VISIBLE);
        Log.d(TAG, "Screenshot adapter set up");

        // Force layout update for screenshots
        binding.rvScreenshots.post(() -> {
            binding.rvScreenshots.requestLayout();
            Log.d(TAG, "Screenshot layout refreshed");
        });

        // Setup Screenshots click listener
        screenshotAdapter.setOnScreenshotClickListener((screenshot, position) -> {
            Log.d(TAG, "Screenshot clicked: " + position + ", URL: " + screenshot.getImageUrl());
            // Open full screen image viewer
            Intent intent = new Intent(requireContext(), ImageViewerActivity.class);
            intent.putExtra(Constants.EXTRA_IMAGE_URL, screenshot.getImageUrl());
            startActivity(intent);
        });

        // Setup Trailers RecyclerView
        trailerAdapter = new TrailerAdapter(requireContext());
        binding.rvTrailers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTrailers.setHasFixedSize(true);
        binding.rvTrailers.setAdapter(trailerAdapter);
        binding.tvTrailersTitle.setVisibility(View.VISIBLE);
        binding.rvTrailers.setVisibility(View.VISIBLE);
        Log.d(TAG, "Trailer adapter set up");

        // Setup Trailers click listener
        trailerAdapter.setOnTrailerClickListener(trailer -> {
            if (trailer.getVideoUrl() != null && !trailer.getVideoUrl().isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.getVideoUrl()));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Error playing video", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), R.string.trailer_not_available, Toast.LENGTH_SHORT).show();
            }
        });

        // Show loading
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void loadScreenshots() {
        Log.d(TAG, "Loading screenshots for game: " + gameId);

        // Ensure screenshots section is visible
        binding.tvScreenshotsTitle.setVisibility(View.VISIBLE);
        binding.rvScreenshots.setVisibility(View.VISIBLE);

        Call<ScreenshotResponse> call = apiService.getGameScreenshots(gameId);
        call.enqueue(new Callback<ScreenshotResponse>() {
            @Override
            public void onResponse(@NonNull Call<ScreenshotResponse> call, @NonNull Response<ScreenshotResponse> response) {
                Log.d(TAG, "Screenshots response received for game ID: " + gameId);

                // Keep screenshots visible regardless of API response
                binding.tvScreenshotsTitle.setVisibility(View.VISIBLE);
                binding.rvScreenshots.setVisibility(View.VISIBLE);

                List<Screenshot> screenshots = new ArrayList<>();

                if (response.isSuccessful() && response.body() != null) {
                    List<ScreenshotResponse.Result> results = response.body().getResults();

                    if (results != null && !results.isEmpty()) {
                        for (ScreenshotResponse.Result result : results) {
                            if (result.getImage() != null) {
                                screenshots.add(new Screenshot(
                                        result.getId(),
                                        result.getImage()
                                ));
                            }
                        }
                    }
                }

                // Update adapter with game ID
                screenshotAdapter.setScreenshots(screenshots, gameId);
                hasScreenshots = true;

                // Force layout refreshing
                binding.rvScreenshots.post(() -> binding.rvScreenshots.requestLayout());

                // Mark section as processed
                screenshotSectionProcessed = true;

                // Now load trailers, only after screenshots are fully loaded
                loadTrailers();
            }

            @Override
            public void onFailure(@NonNull Call<ScreenshotResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Screenshot loading failed for game ID: " + gameId, t);

                // Keep screenshots visible even on error
                binding.tvScreenshotsTitle.setVisibility(View.VISIBLE);
                binding.rvScreenshots.setVisibility(View.VISIBLE);

                // Set screenshots with game ID even on failure
                screenshotAdapter.setScreenshots(null, gameId);

                // Mark section as processed
                screenshotSectionProcessed = true;
                hasScreenshots = true;

                // Now load trailers
                loadTrailers();
            }
        });
    }

    private void loadTrailers() {
        Log.d(TAG, "Loading trailers for game: " + gameId);

        Call<MovieResponse> call = apiService.getGameMovies(gameId);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Trailers response received for game ID: " + gameId);

                // Make sure screenshots remain visible
                binding.tvScreenshotsTitle.setVisibility(View.VISIBLE);
                binding.rvScreenshots.setVisibility(View.VISIBLE);

                List<Trailer> trailers = new ArrayList<>();

                if (response.isSuccessful() && response.body() != null) {
                    List<MovieResponse.Movie> movies = response.body().getResults();

                    if (movies != null && !movies.isEmpty()) {
                        for (MovieResponse.Movie movie : movies) {
                            if (movie.getData() != null) {
                                String videoUrl = movie.getData().getQualityMax() != null ?
                                        movie.getData().getQualityMax() :
                                        movie.getData().getQuality480();

                                if (videoUrl != null && movie.getPreview() != null) {
                                    trailers.add(new Trailer(
                                            movie.getId(),
                                            movie.getName(),
                                            movie.getPreview(),
                                            videoUrl
                                    ));
                                }
                            }
                        }
                    }
                }

                // Update adapter with game ID
                trailerAdapter.setTrailers(trailers, gameId);
                hasTrailers = true;
                trailerSectionProcessed = true;

                checkForEmptyMedia();
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Trailer loading failed for game ID: " + gameId, t);

                // Make sure screenshots remain visible
                binding.tvScreenshotsTitle.setVisibility(View.VISIBLE);
                binding.rvScreenshots.setVisibility(View.VISIBLE);

                // Set trailers with game ID even on failure
                trailerAdapter.setTrailers(null, gameId);

                trailerSectionProcessed = true;
                hasTrailers = true;

                checkForEmptyMedia();
            }
        });
    }

    private void checkForEmptyMedia() {
        // Only show empty message if both sections have been fully processed
        // and are actually empty (which shouldn't happen with hard-coded items)
        if (screenshotSectionProcessed && trailerSectionProcessed &&
                !hasScreenshots && !hasTrailers) {
            binding.tvEmptyMedia.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptyMedia.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}