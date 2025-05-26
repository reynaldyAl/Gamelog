package com.example.gamemology.ui.detail;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.gamemology.R;
import com.example.gamemology.databinding.ActivityImageViewerBinding;
import com.example.gamemology.utils.Constants;
import com.github.chrisbanes.photoview.PhotoView; // Updated import

public class ImageViewerActivity extends AppCompatActivity {

    private ActivityImageViewerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        if (getIntent().hasExtra(Constants.EXTRA_IMAGE_URL)) {
            String imageUrl = getIntent().getStringExtra(Constants.EXTRA_IMAGE_URL);
            loadImage(imageUrl);
        } else {
            Toast.makeText(this, R.string.error_loading_image, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadImage(String imageUrl) {
        binding.progressBar.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.placeholder_game_banner)
                .into(binding.photoView)
                .getSize((width, height) -> {
                    // Image loaded successfully
                    binding.progressBar.setVisibility(View.GONE);

                    // No need for PhotoViewAttacher in the new version
                    // The PhotoView automatically handles zoom functionality
                });
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