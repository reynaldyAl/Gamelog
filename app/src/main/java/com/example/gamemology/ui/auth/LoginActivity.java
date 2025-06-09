package com.example.gamemology.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

        // Forgot Password click handler
        binding.tvForgotPassword.setOnClickListener(view -> {
            showPasswordResetDialog();
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

    // Add this as a new method in your LoginActivity class

    private void showPasswordResetDialog() {
        // Create an EditText for the dialog
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint(R.string.email);

        // Use FrameLayout with padding for better UI
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        input.setLayoutParams(params);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle(R.string.reset_password)
                .setMessage(R.string.reset_password_instructions)
                .setView(container)
                .setPositiveButton(R.string.send, (dialog, which) -> {
                    String email = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        // Here you would implement actual password reset logic
                        // For now, just show a confirmation message
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.reset_email_sent, email), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                R.string.invalid_email, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}