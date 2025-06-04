package com.example.gamemology.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gamemology.R;
import com.example.gamemology.database.DatabaseHelper;
import com.example.gamemology.databinding.ActivityLoginBinding;
import com.example.gamemology.models.User;
import com.example.gamemology.ui.MainActivity;
import com.example.gamemology.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        // Jika sudah login, langsung ke main activity
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        // Button Login Click Handler
        binding.btnLogin.setOnClickListener(view -> {
            // Hide keyboard and show loading
            binding.progressBar.setVisibility(View.VISIBLE);

            // Validate inputs
            if (validateInputs()) {
                loginUser();
            } else {
                binding.progressBar.setVisibility(View.GONE);
            }
        });

        // Register prompt click
        binding.tvRegisterPrompt.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        User user = dbHelper.loginUser(username, password);

        if (user != null) {
            // Login berhasil
            sessionManager.saveUser(user);
            Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // Login gagal
            Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    private boolean validateInputs() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            binding.tilUsername.setError(getString(R.string.required_field));
            return false;
        } else {
            binding.tilUsername.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.required_field));
            return false;
        } else {
            binding.tilPassword.setError(null);
        }

        return true;
    }
}