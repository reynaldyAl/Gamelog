package com.example.gamemology.ui.detail;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.gamemology.R;
import com.example.gamemology.api.ApiClient;
import com.example.gamemology.api.ApiService;
import com.example.gamemology.api.responses.GameResponse;
import com.example.gamemology.database.DatabaseHelper;
import com.example.gamemology.databinding.ActivityDetailBinding;
import com.example.gamemology.databinding.LayoutDetailOfflineBinding;
import com.example.gamemology.models.Game;
import com.example.gamemology.repository.GameRepository;
import com.example.gamemology.ui.detail.tabs.AboutFragment;
import com.example.gamemology.ui.detail.tabs.AchievementsFragment;
import com.example.gamemology.ui.detail.tabs.DLCFragment;
import com.example.gamemology.ui.detail.tabs.MediaFragment;
import com.example.gamemology.utils.Constants;
import com.example.gamemology.utils.NetworkStatusHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity";
    private static final long OFFLINE_NOTICE_TIMEOUT = 3000; // 3 seconds for auto-dismiss

    private ActivityDetailBinding binding;
    private LayoutDetailOfflineBinding offlineContentBinding;

    private GameRepository gameRepository;
    private DatabaseHelper dbHelper;
    private int gameId;
    private Game currentGame;
    private boolean isFavorite = false;
    private boolean isOffline = false;
    private boolean hasCachedData = false;

    // For handling auto-dismiss snackbar
    private final Handler noticeHandler = new Handler(Looper.getMainLooper());
    private final Runnable hideOfflineNoticeRunnable = this::hideOfflineSnackbar;
    private Snackbar offlineSnackbar;

    private static final int TAB_COUNT = 4;
    private static final int ABOUT_TAB = 0;
    private static final int MEDIA_TAB = 1;
    private static final int DLC_TAB = 2;
    private static final int ACHIEVEMENTS_TAB = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup offline content view
        View offlineContentView = getLayoutInflater().inflate(R.layout.layout_detail_offline, null);
        offlineContentBinding = LayoutDetailOfflineBinding.bind(offlineContentView);
        binding.contentLayout.addView(offlineContentBinding.getRoot());
        offlineContentBinding.getRoot().setVisibility(View.GONE);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        gameRepository = new GameRepository(this);
        dbHelper = DatabaseHelper.getInstance(this);

        // Set up retry button click listener for offline mode
        offlineContentBinding.btnRetryDetail.setOnClickListener(v -> loadGameDetails(gameId));

        // Check if we have a game ID
        if (getIntent().hasExtra(Constants.EXTRA_GAME_ID)) {
            gameId = getIntent().getIntExtra(Constants.EXTRA_GAME_ID, -1);

            if (gameId != -1) {
                isFavorite = dbHelper.isGameFavorite(gameId);
                updateFavoriteButton();

                // Check cached data availability
                String cachedDetails = dbHelper.getCachedGameDetails(gameId);
                hasCachedData = cachedDetails != null;

                // Check network status
                isOffline = !NetworkStatusHelper.getInstance(this).isNetworkAvailable();

                // Handle offline state immediately
                if (isOffline && !hasCachedData) {
                    showOfflineContent(true);
                } else {
                    loadGameDetails(gameId);
                }
            } else {
                finish();
                return;
            }
        } else {
            finish();
            return;
        }

        binding.fabFavorite.setOnClickListener(v -> toggleFavorite());

        // Observe network status changes
        NetworkStatusHelper.getInstance(this).observe(this, isConnected -> {
            if (isConnected && isOffline) {
                // Just got back online
                isOffline = false;
                showOfflineContent(false);
                hideOfflineSnackbar(); // Hide offline notice if showing
                showSnackbar(getString(R.string.connection_restored));
                loadGameDetails(gameId); // Reload data
            } else if (!isConnected) {
                // Just went offline
                isOffline = true;
                if (!hasCachedData) {
                    showOfflineContent(true);
                } else {
                    showOfflineSnackbar(); // Show temporary notice
                }
            }
        });
    }

    private void loadGameDetails(int gameId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.viewPager.setVisibility(View.GONE);
        showOfflineContent(false);

        // Use repository instead of direct API call
        gameRepository.getGameDetails(gameId).observe(this, gameResponse -> {
            binding.progressBar.setVisibility(View.GONE);

            if (gameResponse != null) {
                hasCachedData = true;
                setupGameDetails(gameResponse);
                setupViewPager();
                binding.viewPager.setVisibility(View.VISIBLE);
            } else {
                // No data available, could be offline with no cache
                if (isOffline) {
                    showOfflineContent(true);
                    offlineContentBinding.txtOfflineMessage.setText(R.string.detail_offline_no_cache);
                } else {
                    // Online but failed to get data
                    showSnackbar(getString(R.string.error_loading_game_details));
                    finish();
                }
            }
        });
    }

    private void setupGameDetails(GameResponse gameResponse) {
        // Create game object
        currentGame = new Game();
        currentGame.setId(gameResponse.getId());
        currentGame.setName(gameResponse.getName());
        currentGame.setReleased(gameResponse.getReleased());
        currentGame.setBackgroundImage(gameResponse.getBackgroundImage());
        currentGame.setRating(gameResponse.getRating());
        currentGame.setDescription(gameResponse.getDescription());

        // Set UI components
        binding.tvGameTitle.setText(gameResponse.getName());
        binding.tvReleaseDate.setText(getString(R.string.released_date, gameResponse.getReleased()));
        binding.tvRating.setText(String.format("%.1f", gameResponse.getRating()));

        // Load backdrop image
        if (gameResponse.getBackgroundImage() != null) {
            Glide.with(this)
                    .load(gameResponse.getBackgroundImage())
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_game_banner)
                    .error(R.drawable.placeholder_game_banner)
                    .into(binding.imgBackdrop);
        }

        // If offline, show a snackbar
        if (isOffline && hasCachedData) {
            showOfflineSnackbar();
        }
    }

    private void showOfflineSnackbar() {
        // Cancel any existing snackbar
        hideOfflineSnackbar();

        // Create and show new snackbar
        offlineSnackbar = Snackbar.make(
                binding.coordinator,
                R.string.offline_using_cached_data,
                Snackbar.LENGTH_INDEFINITE);

        // Style the snackbar
        View snackbarView = offlineSnackbar.getView();
        snackbarView.setBackgroundColor(Color.parseColor("#323232")); // Dark gray background
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);

        offlineSnackbar.show();

        // Auto-dismiss after timeout
        noticeHandler.postDelayed(hideOfflineNoticeRunnable, OFFLINE_NOTICE_TIMEOUT);
    }

    private void hideOfflineSnackbar() {
        if (offlineSnackbar != null && offlineSnackbar.isShown()) {
            offlineSnackbar.dismiss();
        }
        noticeHandler.removeCallbacks(hideOfflineNoticeRunnable);
    }

    public void refreshData() {
        // Show offline snackbar again if offline and has data
        if (isOffline && hasCachedData) {
            showOfflineSnackbar();
        }

        // Reload game data
        loadGameDetails(gameId);
    }

    private void setupViewPager() {
        DetailPagerAdapter pagerAdapter = new DetailPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    switch (position) {
                        case ABOUT_TAB:
                            tab.setText(R.string.about);
                            tab.setIcon(R.drawable.ic_info);
                            break;
                        case MEDIA_TAB:
                            tab.setText(R.string.media);
                            tab.setIcon(R.drawable.ic_media);
                            break;
                        case DLC_TAB:
                            tab.setText(R.string.dlc);
                            tab.setIcon(R.drawable.ic_dlc);
                            break;
                        case ACHIEVEMENTS_TAB:
                            tab.setText(R.string.achievements);
                            tab.setIcon(R.drawable.ic_achievement);
                            break;
                    }
                }
        ).attach();
    }

    private void toggleFavorite() {
        if (currentGame == null) return;

        isFavorite = !isFavorite;

        if (isFavorite) {
            dbHelper.addGameToFavorites(currentGame);
            showSnackbar(getString(R.string.added_to_favorites));
        } else {
            dbHelper.removeGameFromFavorites(currentGame.getId());
            showSnackbar(getString(R.string.removed_from_favorites));
        }

        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        binding.fabFavorite.setImageResource(
                isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.coordinator, message, Snackbar.LENGTH_SHORT).show();
    }

    private void showOfflineContent(boolean show) {
        if (show) {
            binding.viewPager.setVisibility(View.GONE);
            binding.appBarLayout.setVisibility(View.GONE); // Hide AppBar
            binding.fabFavorite.setVisibility(View.GONE);  // Hide FAB
            offlineContentBinding.getRoot().setVisibility(View.VISIBLE);
        } else {
            binding.appBarLayout.setVisibility(View.VISIBLE);
            binding.fabFavorite.setVisibility(View.VISIBLE);
            offlineContentBinding.getRoot().setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        noticeHandler.removeCallbacks(hideOfflineNoticeRunnable);
        hideOfflineSnackbar();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ViewPager Adapter for Tabs
    private class DetailPagerAdapter extends FragmentStateAdapter {

        public DetailPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Bundle args = new Bundle();
            args.putInt(Constants.EXTRA_GAME_ID, gameId);
            if (currentGame != null) {
                args.putParcelable(Constants.EXTRA_GAME, currentGame);
            }

            Fragment fragment;
            switch (position) {
                case MEDIA_TAB:
                    fragment = new MediaFragment();
                    break;
                case DLC_TAB:
                    fragment = new DLCFragment();
                    break;
                case ACHIEVEMENTS_TAB:
                    fragment = new AchievementsFragment();
                    break;
                case ABOUT_TAB:
                default:
                    fragment = new AboutFragment();
                    break;
            }
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return TAB_COUNT;
        }
    }
}