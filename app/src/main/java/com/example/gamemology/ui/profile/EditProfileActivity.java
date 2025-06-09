package com.example.gamemology.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.gamemology.R;
import com.example.gamemology.database.DatabaseHelper;
import com.example.gamemology.databinding.ActivityEditProfileBinding;
import com.example.gamemology.models.User;
import com.example.gamemology.utils.FileUtils;
import com.example.gamemology.utils.SessionManager;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private ActivityEditProfileBinding binding;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private User currentUser;
    private Uri selectedImageUri;
    private String originalEmail;
    private boolean hasUnsavedChanges = false;

    public static final int RESULT_PROFILE_UPDATED = 101;
    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize binding first to avoid null pointer exceptions
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up UI components
        setupToolbar();

        // Initialize data after UI setup
        initializeData();

        // Set up event listeners
        setupEventListeners();
    }

    private void setupToolbar() {
        // Set up the custom toolbar instead of the action bar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.edit_profile);
        }

        // Handle navigation click directly on the toolbar
        binding.toolbar.setNavigationOnClickListener(v -> handleBackPress());
    }

    private void initializeData() {
        try {
            Log.d(TAG, "Initializing data");

            sessionManager = SessionManager.getInstance(this);
            dbHelper = DatabaseHelper.getInstance(this);
            currentUser = sessionManager.getUser();

            if (currentUser == null) {
                Log.e(TAG, "Current user is null");
                Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Load current user data
            binding.etUsername.setText(currentUser.getUsername());
            binding.etEmail.setText(currentUser.getEmail());
            originalEmail = currentUser.getEmail(); // Store original for change detection

            // Load profile image if available
            loadProfileImage();

            Log.d(TAG, "Data initialization complete");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing data", e);
            Toast.makeText(this, "Error loading profile data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupEventListeners() {
        // Button click listeners
        binding.btnChangePhoto.setOnClickListener(v -> showImagePickerOptions());
        binding.cardProfile.setOnClickListener(v -> showImagePickerOptions());
        binding.tvChangePhoto.setOnClickListener(v -> showImagePickerOptions());
        binding.btnSave.setOnClickListener(v -> validateAndSaveChanges());
        binding.tvCancel.setOnClickListener(v -> handleBackPress());

        // Text change listener to detect changes
        binding.etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateEmail();
            }
        });
    }

    private void loadProfileImage() {
        Log.d(TAG, "Loading profile image");
        try {
            if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
                File imageFile = new File(currentUser.getProfileImage());
                if (imageFile.exists()) {
                    // Load the image with proper error handling
                    Glide.with(this)
                            .load(imageFile)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.drawable.profile_placeholder)
                            .error(R.drawable.profile_placeholder)
                            .into(binding.imgProfile);

                    Log.d(TAG, "Profile image loaded from: " + currentUser.getProfileImage());
                } else {
                    Log.w(TAG, "Profile image file doesn't exist: " + currentUser.getProfileImage());
                    binding.imgProfile.setImageResource(R.drawable.profile_placeholder);
                }
            } else {
                Log.d(TAG, "No profile image path provided");
                binding.imgProfile.setImageResource(R.drawable.profile_placeholder);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile image", e);
            binding.imgProfile.setImageResource(R.drawable.profile_placeholder);
        }
    }

    private void showImagePickerOptions() {
        if (checkPermissions()) {
            openImagePicker();
        } else {
            requestPermissions();
        }
    }

    private void openImagePicker() {
        String[] options = {getString(R.string.camera), getString(R.string.gallery), getString(R.string.cancel)};

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_profile_picture)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Camera
                        ImagePicker.with(this)
                                .cameraOnly()
                                .cropSquare()
                                .compress(1024)
                                .start();
                    } else if (which == 1) {
                        // Gallery
                        ImagePicker.with(this)
                                .galleryOnly()
                                .cropSquare()
                                .compress(1024)
                                .start();
                    }
                })
                .show();
    }

    private boolean checkPermissions() {
        // For Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) ==
                            PackageManager.PERMISSION_GRANTED;
        }
        // For Android 10+ (API 29+)
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED;
        }
        // For older versions
        else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        // Always need camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
        }

        // For Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        }
        // For Android 10 to 12
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        // For older versions
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE
            );
        } else {
            // All permissions already granted
            openImagePicker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;

            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
            } else {
                allGranted = false;
            }

            if (allGranted) {
                openImagePicker();
            } else {
                // Show message with option to go to settings
                boolean shouldShowRationale = false;
                for (String permission : permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        shouldShowRationale = true;
                        break;
                    }
                }

                if (shouldShowRationale) {
                    // User denied permission but not permanently
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.permissions_required)
                            .setMessage(getString(R.string.permissions_explanation))
                            .setPositiveButton(getString(R.string.try_again), (dialog, which) -> requestPermissions())
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show();
                } else {
                    // User permanently denied permission, direct to settings
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.permissions_required)
                            .setMessage(getString(R.string.permissions_settings_required))
                            .setPositiveButton(getString(R.string.go_to_settings), (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            // Get the image from ImagePicker
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Display the selected image
                Glide.with(this).load(selectedImageUri).into(binding.imgProfile);
                hasUnsavedChanges = true;

                // Show immediate feedback
                Toast.makeText(this, R.string.photo_selected, Toast.LENGTH_SHORT).show();

                // Show overlay effect to indicate selection
                binding.imgProfileOverlay.setAlpha(0.1f);
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateEmail() {
        String email = binding.etEmail.getText().toString().trim();

        // Clear previous errors
        binding.tilEmail.setError(null);

        // Check if email is empty
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.required_field));
            return false;
        }

        // Check if email is valid
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.invalid_email));
            return false;
        }

        // Email is different from original
        if (!email.equals(originalEmail)) {
            hasUnsavedChanges = true;
        }

        return true;
    }

    private void validateAndSaveChanges() {
        if (!validateEmail()) {
            return;
        }

        // If there are changes, show confirmation dialog
        if (hasUnsavedChanges || selectedImageUri != null) {
            showSaveConfirmationDialog();
        } else {
            // No changes to save
            Toast.makeText(this, R.string.no_changes_to_save, Toast.LENGTH_SHORT).show();
        }
    }

    private void showSaveConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.save_changes)
                .setMessage(R.string.save_changes_confirmation)
                .setPositiveButton(R.string.save, (dialog, which) -> saveChanges())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void saveChanges() {
        String email = binding.etEmail.getText().toString().trim();

        // Show loading state
        showLoading(true);

        // Copy image file to app's private directory if selected
        String profileImagePath = currentUser.getProfileImage();
        if (selectedImageUri != null) {
            try {
                // First delete the old profile image if it exists
                if (profileImagePath != null && !profileImagePath.isEmpty()) {
                    File oldFile = new File(profileImagePath);
                    if (oldFile.exists()) {
                        if (!oldFile.delete()) {
                            Log.w(TAG, "Could not delete old profile image: " + profileImagePath);
                        }
                    }
                }

                // Create a unique filename using timestamp
                String uniqueFileName = "profile_" + currentUser.getId() + "_" + System.currentTimeMillis();
                File destFile = FileUtils.createImageFile(this, uniqueFileName);

                FileUtils.copyFile(this, selectedImageUri, destFile);
                profileImagePath = destFile.getAbsolutePath();
                Log.d(TAG, "Saved profile image to: " + profileImagePath);
            } catch (Exception e) {
                Log.e(TAG, "Error saving profile image", e);
                showLoading(false);
                showErrorDialog(getString(R.string.error_saving_image), e.getMessage());
                return;
            }
        }

        // Update user profile
        currentUser.setEmail(email);
        currentUser.setProfileImage(profileImagePath);

        // Save to database with retry capability
        saveToDatabase();
    }

    private void saveToDatabase() {
        try {
            int result = dbHelper.updateUserProfile(currentUser);
            if (result > 0) {
                // Update session with updated user
                sessionManager.saveUser(currentUser);
                showLoading(false);
                showSuccessAndFinish();
            } else {
                showLoading(false);
                showRetryDialog(getString(R.string.database_update_failed));
            }
        } catch (Exception e) {
            Log.e(TAG, "Database error", e);
            showLoading(false);
            showRetryDialog(getString(R.string.database_error));
        }
    }

    private void showSuccessAndFinish() {
        Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
        setResult(RESULT_PROFILE_UPDATED);
        finish();
    }

    private void showLoading(boolean isLoading) {
        binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!isLoading);
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showRetryDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_updating_profile)
                .setMessage(message)
                .setPositiveButton(R.string.retry, (dialog, which) -> saveToDatabase())
                .setNegativeButton(R.string.cancel, (dialog, which) -> binding.btnSave.setEnabled(true))
                .show();
    }

    private boolean checkForChanges() {
        if (selectedImageUri != null) {
            return true;
        }

        String currentEmail = binding.etEmail.getText().toString().trim();
        return !currentEmail.equals(originalEmail);
    }

    private void handleBackPress() {
        if (checkForChanges()) {
            showDiscardChangesDialog();
        } else {
            finish();
        }
    }

    private void showDiscardChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.discard_changes)
                .setMessage(R.string.discard_changes_message)
                .setPositiveButton(R.string.discard, (dialog, which) -> finish())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handleBackPress();
    }
}