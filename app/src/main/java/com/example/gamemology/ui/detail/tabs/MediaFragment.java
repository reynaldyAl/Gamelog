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
import com.example.gamemology.ui.dialogs.ImageViewerDialogFragment;
import com.example.gamemology.utils.Constants;
import com.google.gson.Gson; // Add this import

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

    // Pagination variables
    private int currentScreenshotPage = 1;
    private boolean isLoadingScreenshots = false;
    private boolean hasMoreScreenshots = false;
    private String nextScreenshotPageUrl = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMediaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated started");

        try {
            apiService = ApiClient.getInstance().getApiService();

            // Reset processing flags
            screenshotSectionProcessed = false;
            trailerSectionProcessed = false;
            currentScreenshotPage = 1;
            nextScreenshotPageUrl = null;

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
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
            showError("Error: " + e.getMessage());
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
        try {
            Log.d(TAG, "Setting up RecyclerViews");

            // Setup Screenshots RecyclerView
            screenshotAdapter = new ScreenshotAdapter(requireContext());
            GridLayoutManager screenshotLayoutManager = new GridLayoutManager(requireContext(), 2);
            // Handle the span size of load more button to span across all columns
            screenshotLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return screenshotAdapter.getItemViewType(position) == 1 ? 2 : 1;
                }
            });
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

                try {
                    // Show image in dialog
                    showImageViewerDialog(screenshot.getImageUrl());
                } catch (Exception e) {
                    Log.e(TAG, "Error showing screenshot dialog", e);
                    Toast.makeText(requireContext(), "Error displaying image", Toast.LENGTH_SHORT).show();
                }
            });

            // Setup Load More click listener
            screenshotAdapter.setOnLoadMoreClickListener(() -> {
                Toast.makeText(requireContext(), "Loading more screenshots...", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Load more clicked, loading page " + (currentScreenshotPage + 1));
                loadMoreScreenshots();
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
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerViews", e);
            showError("Error setting up media viewers: " + e.getMessage());
        }
    }

    // Method to show image in a DialogFragment
    private void showImageViewerDialog(String imageUrl) {
        if (isAdded() && !isDetached()) {
            ImageViewerDialogFragment dialog = ImageViewerDialogFragment.newInstance(imageUrl);
            dialog.show(getParentFragmentManager(), "image_viewer");
        }
    }

    private void loadScreenshots() {
        Log.d(TAG, "Loading screenshots for game: " + gameId + ", page: " + currentScreenshotPage);
        isLoadingScreenshots = true;

        // Ensure screenshots section is visible
        binding.tvScreenshotsTitle.setVisibility(View.VISIBLE);
        binding.rvScreenshots.setVisibility(View.VISIBLE);

        Call<ScreenshotResponse> call = apiService.getGameScreenshots(gameId);
        call.enqueue(new Callback<ScreenshotResponse>() {
            @Override
            public void onResponse(@NonNull Call<ScreenshotResponse> call, @NonNull Response<ScreenshotResponse> response) {
                try {
                    Log.d(TAG, "Screenshots response received for game ID: " + gameId);
                    isLoadingScreenshots = false;

                    // Keep screenshots visible regardless of API response
                    binding.tvScreenshotsTitle.setVisibility(View.VISIBLE);
                    binding.rvScreenshots.setVisibility(View.VISIBLE);

                    List<Screenshot> screenshots = new ArrayList<>();

                    if (response.isSuccessful() && response.body() != null) {
                        // NEW: Add detailed logging of the entire response
                        Gson gson = new Gson();
                        String jsonResponse = gson.toJson(response.body());
                        Log.d(TAG, "Full screenshot response: " + jsonResponse);

                        ScreenshotResponse screenshotResponse = response.body();
                        List<ScreenshotResponse.Result> results = screenshotResponse.getResults();

                        // Store next page URL for pagination if available
                        nextScreenshotPageUrl = screenshotResponse.getNext();
                        hasMoreScreenshots = nextScreenshotPageUrl != null;
                        Log.d(TAG, "Has more screenshots: " + hasMoreScreenshots + ", next URL: " + nextScreenshotPageUrl);

                        // Force "Load More" button to appear for testing
                        // Comment out this line once you confirm the button works
                        hasMoreScreenshots = true;

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

                    // Update adapter with game ID and pagination status
                    screenshotAdapter.setScreenshots(screenshots, gameId);
                    screenshotAdapter.setHasMoreScreenshots(hasMoreScreenshots);
                    hasScreenshots = true;

                    // Force layout refreshing
                    binding.rvScreenshots.post(() -> binding.rvScreenshots.requestLayout());

                    // Mark section as processed
                    screenshotSectionProcessed = true;

                    // Now load trailers, only after screenshots are fully loaded
                    loadTrailers();
                } catch (Exception e) {
                    Log.e(TAG, "Error processing screenshot response", e);
                    onFailure(call, new Throwable("Error processing response: " + e.getMessage()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ScreenshotResponse> call, @NonNull Throwable t) {
                try {
                    Log.e(TAG, "Screenshot loading failed for game ID: " + gameId, t);
                    isLoadingScreenshots = false;

                    // Keep screenshots visible even on error
                    binding.tvScreenshotsTitle.setVisibility(View.VISIBLE);
                    binding.rvScreenshots.setVisibility(View.VISIBLE);

                    // Set screenshots with game ID even on failure
                    screenshotAdapter.setScreenshots(null, gameId);
                    screenshotAdapter.setHasMoreScreenshots(false);

                    // Mark section as processed
                    screenshotSectionProcessed = true;
                    hasScreenshots = true;

                    // Now load trailers
                    loadTrailers();
                } catch (Exception e) {
                    Log.e(TAG, "Error handling screenshot failure", e);
                    if (binding != null) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    // Method to load more screenshots
    private void loadMoreScreenshots() {
        if (isLoadingScreenshots || nextScreenshotPageUrl == null) {
            // For testing, we'll allow it to continue even without a next URL
            if (nextScreenshotPageUrl == null && hasMoreScreenshots) {
                Log.d(TAG, "Next URL is null but we're forcing it to load more for testing");
                // Use a dummy URL just for testing
                nextScreenshotPageUrl = "https://api.rawg.io/api/games/" + gameId + "/screenshots?page=2";
            } else {
                return;
            }
        }

        isLoadingScreenshots = true;
        currentScreenshotPage++;
        screenshotAdapter.setLoadingMore(true);

        Log.d(TAG, "Loading more screenshots for game: " + gameId + ", page: " + currentScreenshotPage);
        Log.d(TAG, "Using URL: " + nextScreenshotPageUrl);

        Call<ScreenshotResponse> call = apiService.getGameScreenshotsNextPage(nextScreenshotPageUrl);
        call.enqueue(new Callback<ScreenshotResponse>() {
            @Override
            public void onResponse(@NonNull Call<ScreenshotResponse> call, @NonNull Response<ScreenshotResponse> response) {
                try {
                    Log.d(TAG, "More screenshots response received for game ID: " + gameId);
                    isLoadingScreenshots = false;

                    List<Screenshot> newScreenshots = new ArrayList<>();
                    boolean hasMore = false;

                    if (response.isSuccessful() && response.body() != null) {
                        // NEW: Log the pagination response
                        Gson gson = new Gson();
                        String jsonResponse = gson.toJson(response.body());
                        Log.d(TAG, "Full pagination response: " + jsonResponse);

                        ScreenshotResponse screenshotResponse = response.body();
                        List<ScreenshotResponse.Result> results = screenshotResponse.getResults();

                        // Store next page URL for pagination
                        nextScreenshotPageUrl = screenshotResponse.getNext();
                        hasMore = nextScreenshotPageUrl != null;

                        Log.d(TAG, "Next pagination URL: " + nextScreenshotPageUrl);

                        if (results != null && !results.isEmpty()) {
                            for (ScreenshotResponse.Result result : results) {
                                if (result.getImage() != null) {
                                    newScreenshots.add(new Screenshot(
                                            result.getId(),
                                            result.getImage()
                                    ));
                                }
                            }
                        }
                    }

                    // Update adapter with new screenshots
                    screenshotAdapter.addMoreScreenshots(newScreenshots, hasMore);
                    Log.d(TAG, "Added " + newScreenshots.size() + " more screenshots");

                    // Force layout refreshing
                    binding.rvScreenshots.post(() -> binding.rvScreenshots.requestLayout());

                } catch (Exception e) {
                    Log.e(TAG, "Error loading more screenshots", e);
                    screenshotAdapter.setLoadingMore(false);
                    Toast.makeText(requireContext(), "Error loading more screenshots", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ScreenshotResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load more screenshots", t);
                isLoadingScreenshots = false;
                screenshotAdapter.setLoadingMore(false);
                Toast.makeText(requireContext(), "Failed to load more screenshots", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTrailers() {
        Log.d(TAG, "Loading trailers for game: " + gameId);

        Call<MovieResponse> call = apiService.getGameMovies(gameId);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                try {
                    if (binding != null) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                    Log.d(TAG, "Trailers response received for game ID: " + gameId);

                    // Make sure screenshots remain visible
                    if (binding != null) {
                        binding.tvScreenshotsTitle.setVisibility(View.VISIBLE);
                        binding.rvScreenshots.setVisibility(View.VISIBLE);
                    }

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
                    if (trailerAdapter != null) {
                        trailerAdapter.setTrailers(trailers, gameId);
                    }
                    hasTrailers = true;
                    trailerSectionProcessed = true;

                    checkForEmptyMedia();
                } catch (Exception e) {
                    Log.e(TAG, "Error processing trailer response", e);
                    onFailure(call, new Throwable("Error processing trailer response: " + e.getMessage()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                try {
                    if (binding != null) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                    Log.e(TAG, "Trailer loading failed for game ID: " + gameId, t);

                    // Make sure screenshots remain visible
                    if (binding != null) {
                        binding.tvScreenshotsTitle.setVisibility(View.VISIBLE);
                        binding.rvScreenshots.setVisibility(View.VISIBLE);
                    }

                    // Set trailers with game ID even on failure
                    if (trailerAdapter != null) {
                        trailerAdapter.setTrailers(null, gameId);
                    }

                    trailerSectionProcessed = true;
                    hasTrailers = true;

                    checkForEmptyMedia();
                } catch (Exception e) {
                    Log.e(TAG, "Error handling trailer failure", e);
                }
            }
        });
    }

    private void checkForEmptyMedia() {
        try {
            // Only show empty message if both sections have been fully processed
            // and are actually empty (which shouldn't happen with hard-coded items)
            if (binding != null) {
                if (screenshotSectionProcessed && trailerSectionProcessed &&
                        !hasScreenshots && !hasTrailers) {
                    binding.tvEmptyMedia.setVisibility(View.VISIBLE);
                } else {
                    binding.tvEmptyMedia.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking for empty media", e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}