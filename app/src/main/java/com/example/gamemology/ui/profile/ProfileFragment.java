package com.example.gamemology.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.gamemology.R;
import com.example.gamemology.database.DatabaseHelper;
import com.example.gamemology.databinding.FragmentProfileBinding;
import com.example.gamemology.models.User;
import com.example.gamemology.ui.auth.LoginActivity;
import com.example.gamemology.utils.Constants;
import com.example.gamemology.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private static final int EDIT_PROFILE_REQUEST = 101;

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
        } else {
            redirectToLogin();
        }

        // Set up click listeners
        binding.btnLogout.setOnClickListener(v -> logoutUser());

        // Profile picture edit button
        binding.btnEditPhoto.setOnClickListener(v -> startEditProfileActivity());

        // Edit profile button
        binding.btnEditProfile.setOnClickListener(v -> startEditProfileActivity());

        // View favorites button - redirects to favorites tab
        binding.btnViewFavorite.setOnClickListener(v ->
                requireActivity().findViewById(R.id.nav_favorites).performClick());
    }

    private void startEditProfileActivity() {
        Intent intent = new Intent(requireContext(), EditProfileActivity.class);
        startActivityForResult(intent, EDIT_PROFILE_REQUEST);
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
                    .into(binding.imgProfile);
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == EditProfileActivity.RESULT_PROFILE_UPDATED) {
            // Profile was updated, refresh the UI
            User updatedUser = sessionManager.getUser();
            if (updatedUser != null) {
                setupUserProfile(updatedUser);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}