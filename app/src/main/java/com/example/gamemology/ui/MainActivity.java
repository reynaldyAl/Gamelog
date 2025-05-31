package com.example.gamemology.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.gamemology.R;
import com.example.gamemology.ai.GameAssistantActivity;
import com.example.gamemology.databinding.ActivityMainBinding;
import com.example.gamemology.ui.favorite.FavoriteFragment;
import com.example.gamemology.ui.home.HomeFragment;
import com.example.gamemology.ui.search.SearchActivity;
import com.example.gamemology.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

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
        return true;
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
        }

        return super.onOptionsItemSelected(item);
    }
}