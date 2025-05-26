package com.example.gamemology.ui.settings;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamemology.R;
import com.example.gamemology.databinding.ActivitySettingsBinding;
import com.example.gamemology.utils.ThemeUtils;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings);
        }

        setupThemeSelection();
    }

    private void setupThemeSelection() {
        // Set the current theme selection
        int currentThemeMode = ThemeUtils.getThemeMode(this);

        switch (currentThemeMode) {
            case ThemeUtils.MODE_LIGHT:
                binding.rbLightTheme.setChecked(true);
                break;
            case ThemeUtils.MODE_DARK:
                binding.rbDarkTheme.setChecked(true);
                break;
            case ThemeUtils.MODE_SYSTEM:
            default:
                binding.rbSystemTheme.setChecked(true);
                break;
        }

        // Set theme change listener
        binding.rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int themeMode;
            String themeName;

            if (checkedId == R.id.rb_light_theme) {
                themeMode = ThemeUtils.MODE_LIGHT;
                themeName = "Light";
            } else if (checkedId == R.id.rb_dark_theme) {
                themeMode = ThemeUtils.MODE_DARK;
                themeName = "Dark";
            } else {
                themeMode = ThemeUtils.MODE_SYSTEM;
                themeName = "System Default";
            }

            // Apply the selected theme
            ThemeUtils.setThemeMode(this, themeMode);

            // Show confirmation message
            Toast.makeText(this, getString(R.string.theme_changed, themeName), Toast.LENGTH_SHORT).show();
        });

        // Handle About button click
        binding.btnAbout.setOnClickListener(v -> {
            // Show about dialog
            showAboutDialog();
        });
    }

    private void showAboutDialog() {
        new AboutDialogFragment().show(getSupportFragmentManager(), "about_dialog");
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