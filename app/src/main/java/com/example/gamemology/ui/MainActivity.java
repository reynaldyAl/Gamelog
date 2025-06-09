package com.example.gamemology.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.gamemology.R;
import com.example.gamemology.ai.GameAssistantActivity;
import com.example.gamemology.databinding.ActivityMainBinding;
import com.example.gamemology.models.User;
import com.example.gamemology.ui.favorite.FavoriteFragment;
import com.example.gamemology.ui.home.HomeFragment;
import com.example.gamemology.ui.profile.ProfileFragment;
import com.example.gamemology.ui.search.SearchActivity;
import com.example.gamemology.ui.settings.SettingsActivity;
import com.example.gamemology.utils.Constants;
import com.example.gamemology.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private SessionManager sessionManager;
    private MenuItem profileMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Initialize session manager
        sessionManager = SessionManager.getInstance(this);

        // Set up bottom navigation
        binding.bottomNavigation.setOnNavigationItemSelectedListener(navListener);

        // Default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment(), Constants.TAG_HOME_FRAGMENT)
                    .commit();
        }
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                String tag = "";

                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                    tag = Constants.TAG_HOME_FRAGMENT;
                    setTitle(R.string.app_name);
                } else if (itemId == R.id.nav_favorites) {
                    selectedFragment = new FavoriteFragment();
                    tag = Constants.TAG_FAVORITE_FRAGMENT;
                    setTitle(R.string.favorites);
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                    tag = Constants.TAG_PROFILE_FRAGMENT;
                    setTitle(R.string.profile);
                } else if (itemId == R.id.action_settings) {
                    // Launch Settings Activity
                    startActivity(new Intent(this, SettingsActivity.class));
                    return true;
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment, tag)
                            .commit();
                    return true;
                }
                return false;
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Save reference to profile menu item for later updates
        profileMenuItem = menu.findItem(R.id.action_profile);

        // Load profile picture into the icon
        loadProfilePicture();

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Refresh profile icon whenever menu is prepared
        if (profileMenuItem != null) {
            loadProfilePicture();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void loadProfilePicture() {
        try {
            // Get current user data
            User currentUser = sessionManager.getUser();

            if (currentUser != null && currentUser.getProfileImage() != null &&
                    !currentUser.getProfileImage().isEmpty()) {

                File imgFile = new File(currentUser.getProfileImage());
                if (imgFile.exists()) {
                    // Use Glide to load the image and set it as the menu icon
                    Glide.with(this)
                            .asBitmap()
                            .load(imgFile)
                            .circleCrop()
                            .into(new SimpleTarget<Bitmap>(100, 100) {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource,
                                                            Transition<? super Bitmap> transition) {
                                    if (profileMenuItem != null) {
                                        profileMenuItem.setIcon(new BitmapDrawable(
                                                getResources(), resource));
                                    }
                                }
                            });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile picture: " + e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_ai_assistant) {
            Intent intent = new Intent(this, GameAssistantActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_profile) {
            // Navigate to profile fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment(), Constants.TAG_PROFILE_FRAGMENT)
                    .commit();
            binding.bottomNavigation.setSelectedItemId(R.id.nav_profile);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Call this method to refresh the profile image in the toolbar
     * For example, after a user updates their profile
     */
    public void refreshProfileImage() {
        invalidateOptionsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh profile image when returning to the activity
        refreshProfileImage();
    }

    /**
     * Helper method to create circular bitmap (alternative to Glide's circleCrop)
     */
    private Bitmap createCircularBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int diameter = Math.min(width, height);
        Bitmap output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        int centerX = diameter / 2;
        int centerY = diameter / 2;

        canvas.drawCircle(centerX, centerY, diameter / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, (diameter - width) / 2, (diameter - height) / 2, paint);

        return output;
    }
}