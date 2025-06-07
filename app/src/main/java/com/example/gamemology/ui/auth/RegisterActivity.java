package com.example.gamemology.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamemology.R;
import com.example.gamemology.database.DatabaseHelper;
import com.example.gamemology.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Enable Up navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.register);
        }

        dbHelper = DatabaseHelper.getInstance(this);
        // Fix: Ensure database tables exist before attempting registration
        dbHelper.ensureTablesExist();

        // Register button click handler
        binding.btnRegister.setOnClickListener(view -> {
            hideKeyboard();
            binding.progressBar.setVisibility(View.VISIBLE);

            // Disable button to prevent multiple clicks
            binding.btnRegister.setEnabled(false);

            if (validateInputs()) {
                registerUser();
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnRegister.setEnabled(true);
            }
        });

        // Login prompt click
        binding.tvLoginPrompt.setOnClickListener(view -> {
            finish(); // Go back to login
        });
    }

    private void registerUser() {
        String username = binding.etUsername.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Check if username already exists
        if (dbHelper.isUsernameExists(username)) {
            binding.tilUsername.setError(getString(R.string.username_exists));
            binding.progressBar.setVisibility(View.GONE);
            binding.btnRegister.setEnabled(true);
            return;
        }

        // Check if email already exists
        if (dbHelper.isEmailExists(email)) {
            binding.tilEmail.setError(getString(R.string.email_exists));
            binding.progressBar.setVisibility(View.GONE);
            binding.btnRegister.setEnabled(true);
            return;
        }

        // Attempt to register user
        long result = dbHelper.registerUser(username, email, password);

        if (result != -1) {
            // Registration successful
            Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        } else {
            // Registration failed
            Toast.makeText(this, R.string.register_failed, Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            binding.btnRegister.setEnabled(true);
        }
    }

    private boolean validateInputs() {
        String username = binding.etUsername.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
        boolean isValid = true;

        // Check empty fields
        if (TextUtils.isEmpty(username)) {
            binding.tilUsername.setError(getString(R.string.required_field));
            isValid = false;
        } else if (username.length() < 3) {
            binding.tilUsername.setError(getString(R.string.username_too_short));
            isValid = false;
        } else if (username.contains(" ")) {
            binding.tilUsername.setError(getString(R.string.username_no_spaces));
            isValid = false;
        } else {
            binding.tilUsername.setError(null);
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.required_field));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.invalid_email));
            isValid = false;
        } else {
            binding.tilEmail.setError(null);
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.required_field));
            isValid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.password_too_short));
            isValid = false;
        } else if (!isPasswordStrong(password)) {
            binding.tilPassword.setError(getString(R.string.password_weak));
            isValid = false;
        } else {
            binding.tilPassword.setError(null);
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.required_field));
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.password_mismatch));
            isValid = false;
        } else {
            binding.tilConfirmPassword.setError(null);
        }

        return isValid;
    }

    private boolean isPasswordStrong(String password) {
        // Password should contain at least one number and one letter
        boolean hasNumber = false;
        boolean hasLetter = false;

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                hasNumber = true;
            } else if (Character.isLetter(c)) {
                hasLetter = true;
            }

            if (hasNumber && hasLetter) {
                return true;
            }
        }

        return false;
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            view.clearFocus();
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