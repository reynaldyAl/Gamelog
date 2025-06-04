package com.example.gamemology.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.gamemology.R;
import com.example.gamemology.adapter.GameAdapter;
import com.example.gamemology.database.DatabaseHelper;
import com.example.gamemology.databinding.FragmentProfileBinding;
import com.example.gamemology.models.Game;
import com.example.gamemology.models.User;
import com.example.gamemology.ui.auth.LoginActivity;
import com.example.gamemology.ui.detail.DetailActivity;
import com.example.gamemology.utils.Constants;
import com.example.gamemology.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private GameAdapter gameAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        sessionManager = SessionManager.getInstance(requireContext());

        User currentUser = sessionManager.getUser();

        if (currentUser != null) {
            setupUserProfile(currentUser);
            setupFavoriteGames(currentUser.getId());
        } else {
            redirectToLogin();
        }

        binding.btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void setupUserProfile(User user) {
        binding.tvUsername.setText(user.getUsername());
        binding.tvEmail.setText(user.getEmail());

        // Format join date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String joinDate = dateFormat.format(new Date(user.getJoinDate()));
        binding.tvJoinDate.setText(getString(R.string.member_since, joinDate));

        // Load profile image if available
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            Glide.with(this)
                    .load(user.getProfileImage())
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .circleCrop()
                    .into(binding.imgProfile);
        }
    }

    private void setupFavoriteGames(int userId) {
        gameAdapter = new GameAdapter(requireContext());
        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFavorites.setAdapter(gameAdapter);

        // Set up click listener
        gameAdapter.setOnItemClickListener(new GameAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Game game) {
                Intent intent = new Intent(requireContext(), DetailActivity.class);
                intent.putExtra(Constants.EXTRA_GAME_ID, game.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Game game, boolean isFavorite) {
                if (!isFavorite) {
                    dbHelper.removeGameFromFavorites(game.getId(), userId);
                    Toast.makeText(requireContext(), R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
                    loadFavoriteGames(userId);
                }
            }
        });

        loadFavoriteGames(userId);
    }

    private void loadFavoriteGames(int userId) {
        List<Game> favoriteGames = dbHelper.getAllFavoriteGames(userId);

        if (favoriteGames.isEmpty()) {
            binding.tvEmptyFavorites.setVisibility(View.VISIBLE);
            binding.rvFavorites.setVisibility(View.GONE);
        } else {
            binding.tvEmptyFavorites.setVisibility(View.GONE);
            binding.rvFavorites.setVisibility(View.VISIBLE);
            gameAdapter.setGames(favoriteGames);
        }
    }

    private void logoutUser() {
        sessionManager.logout();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        User currentUser = sessionManager.getUser();
        if (currentUser != null) {
            loadFavoriteGames(currentUser.getId());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}