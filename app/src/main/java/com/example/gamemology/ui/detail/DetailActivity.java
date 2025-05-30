package com.example.gamemology.ui.detail;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.View;

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
import com.example.gamemology.models.Game;
import com.example.gamemology.ui.detail.tabs.AboutFragment;
import com.example.gamemology.ui.detail.tabs.AchievementsFragment;
import com.example.gamemology.ui.detail.tabs.DLCFragment;
import com.example.gamemology.ui.detail.tabs.MediaFragment;
import com.example.gamemology.utils.Constants;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    private ApiService apiService;
    private DatabaseHelper dbHelper;
    private int gameId;
    private Game currentGame;
    private boolean isFavorite = false;

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

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // We'll set a custom title
        }

        apiService = ApiClient.getInstance().getApiService();
        dbHelper = DatabaseHelper.getInstance(this);

        if (getIntent().hasExtra(Constants.EXTRA_GAME_ID)) {
            gameId = getIntent().getIntExtra(Constants.EXTRA_GAME_ID, -1);

            if (gameId != -1) {
                isFavorite = dbHelper.isGameFavorite(gameId);
                updateFavoriteButton();
                loadGameDetails(gameId);
            } else {
                finish();
                return;
            }
        } else {
            finish();
            return;
        }

        binding.fabFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void loadGameDetails(int gameId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.contentLayout.setVisibility(View.GONE);

        Call<GameResponse> call = apiService.getGameDetail(gameId);
        call.enqueue(new Callback<GameResponse>() {
            @Override
            public void onResponse(@NonNull Call<GameResponse> call, @NonNull Response<GameResponse> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    GameResponse gameResponse = response.body();
                    setupGameDetails(gameResponse);
                    setupViewPager();
                    binding.contentLayout.setVisibility(View.VISIBLE);
                } else {
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GameResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                finish();
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
            args.putParcelable(Constants.EXTRA_GAME, (Parcelable) currentGame);

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