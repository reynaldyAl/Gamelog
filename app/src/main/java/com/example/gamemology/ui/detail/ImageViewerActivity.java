package com.example.gamemology.ui.detail;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.gamemology.R;
import com.example.gamemology.databinding.ActivityImageViewerBinding;
import com.example.gamemology.utils.Constants;

public class ImageViewerActivity extends AppCompatActivity {

    private ActivityImageViewerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pastikan panggil ini SEBELUM setContentView
        makeFullScreen();

        binding = ActivityImageViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar - PERHATIKAN: pastikan toolbar ini ada di layout Anda
        if (binding.toolbar != null) {
            setSupportActionBar(binding.toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("");
            }
        }

        // Load gambar jika URL tersedia
        if (getIntent().hasExtra(Constants.EXTRA_IMAGE_URL)) {
            String imageUrl = getIntent().getStringExtra(Constants.EXTRA_IMAGE_URL);
            loadImage(imageUrl);
        } else {
            Toast.makeText(this, R.string.error_loading_image, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Tambahkan click listener untuk toggle UI
        binding.photoView.setOnClickListener(v -> toggleSystemUI());
    }

    private void makeFullScreen() {
        // Metode modern untuk membuat activity fullscreen
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Set warna bar transparan
        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, android.R.color.transparent));

        // Tambahkan flag fullscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        // Sembunyikan system bars
        hideSystemUI();
    }

    private void hideSystemUI() {
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(),
                getWindow().getDecorView());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        controller.hide(WindowInsetsCompat.Type.systemBars());
    }

    private void showSystemUI() {
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(),
                getWindow().getDecorView());
        controller.show(WindowInsetsCompat.Type.systemBars());
    }

    private void toggleSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Untuk Android 11+
            int vis = getWindow().getDecorView().getWindowInsetsController().getSystemBarsBehavior();
            if (vis == WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE) {
                showSystemUI();
            } else {
                hideSystemUI();
            }
        } else {
            // Untuk versi Android lebih lama
            int vis = getWindow().getDecorView().getSystemUiVisibility();
            if ((vis & View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) != 0) {
                showSystemUI();
            } else {
                hideSystemUI();
            }
        }
    }

    private void loadImage(String imageUrl) {
        // Tampilkan progress bar
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        // Load gambar dengan Glide
        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache gambar
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.placeholder_game_banner)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e,
                                                Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                boolean isFirstResource) {
                        if (binding.progressBar != null) {
                            binding.progressBar.setVisibility(View.GONE);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                                   Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                   com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        if (binding.progressBar != null) {
                            binding.progressBar.setVisibility(View.GONE);
                        }
                        return false;
                    }
                })
                .into(binding.photoView);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}