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

        // Register button click handler
        binding.btnRegister.setOnClickListener(view -> {
            binding.progressBar.setVisibility(View.VISIBLE);

            if (validateInputs()) {
                registerUser();
            } else {
                binding.progressBar.setVisibility(View.GONE);
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
        }
    }

    private boolean validateInputs() {
        String username = binding.etUsername.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // Check empty fields
        if (TextUtils.isEmpty(username)) {
            binding.tilUsername.setError(getString(R.string.required_field));
            return false;
        } else {
            binding.tilUsername.setError(null);
        }

        // Validate email
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.invalid_email));
            return false;
        } else {
            binding.tilEmail.setError(null);
        }

        // Validate password
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.password_too_short));
            return false;
        } else {
            binding.tilPassword.setError(null);
        }

        // Validate confirm password
        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.password_mismatch));
            return false;
        } else {
            binding.tilConfirmPassword.setError(null);
        }

        return true;
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