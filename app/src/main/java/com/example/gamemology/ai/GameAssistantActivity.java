package com.example.gamemology.ai;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gamemology.R;
import com.example.gamemology.ai.adapter.ChatAdapter;
import com.example.gamemology.ai.adapter.ChatMessage;
import com.example.gamemology.ai.utils.AIPromptUtils;
import com.example.gamemology.databinding.ActivityGameAssistantBinding;
import com.example.gamemology.models.Game;
import com.example.gamemology.utils.Constants;
import com.example.gamemology.utils.NetworkUtils;
import com.google.android.material.chip.Chip;

public class GameAssistantActivity extends AppCompatActivity {

    private ActivityGameAssistantBinding binding;
    private GeminiClient geminiClient;
    private ChatAdapter chatAdapter;
    private Game contextGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameAssistantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.game_assistant);
        }

        // Initialize components
        setupChatAdapter();
        setupGeminiClient();
        setupInputField();

        // Check for game context from intent
        if (getIntent().hasExtra(Constants.EXTRA_GAME)) {
            contextGame = getIntent().getParcelableExtra(Constants.EXTRA_GAME);
            setupGameContext(contextGame);
        }

        // Add welcome message
        addAssistantMessage(getString(R.string.welcome_message));
    }

    private void setupChatAdapter() {
        chatAdapter = new ChatAdapter(this);
        binding.rvChat.setLayoutManager(new LinearLayoutManager(this));
        binding.rvChat.setAdapter(chatAdapter);
    }

    private void setupGeminiClient() {
        geminiClient = new GeminiClient(this);
    }

    private void setupInputField() {
        // Send button click handler
        binding.btnSend.setOnClickListener(v -> sendMessage());

        // Enable pressing enter on keyboard to send
        binding.etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void setupGameContext(Game game) {
        if (game == null) return;

        // Update system prompt with game context
        String contextPrompt = AIPromptUtils.createGameContextPrompt(game);
        geminiClient.setSystemPrompt(contextPrompt);

        // Set context chip text
        binding.chipGameContext.setText(getString(R.string.context_game, game.getName()));
        binding.chipGameContext.setVisibility(View.VISIBLE);

        // Suggest contextual questions
        addSuggestionChips(game);
    }

    private void addSuggestionChips(Game game) {
        binding.chipGroupSuggestions.removeAllViews();

        String[] suggestions = {
                "Tell me about " + game.getName(),
                "Similar games to " + game.getName(),
                "Is " + game.getName() + " good for kids?",
                "Gameplay tips for " + game.getName()
        };

        for (String suggestion : suggestions) {
            Chip chip = new Chip(this);
            chip.setText(suggestion);
            chip.setClickable(true);
            chip.setCheckable(false);

            chip.setOnClickListener(v -> {
                binding.etMessage.setText(suggestion);
                sendMessage();
            });

            binding.chipGroupSuggestions.addView(chip);
        }

        binding.chipGroupSuggestions.setVisibility(View.VISIBLE);
    }

    private void sendMessage() {
        String message = binding.etMessage.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }

        // Check for network connectivity
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
            return;
        }

        // Add user message to chat
        addUserMessage(message);

        // Clear input field
        binding.etMessage.setText("");

        // Show loading indicator
        binding.progressBar.setVisibility(View.VISIBLE);

        // Send to Gemini
        geminiClient.sendPrompt(message, new GeminiClient.ResponseCallback() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    addAssistantMessage(response);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(GameAssistantActivity.this,
                            errorMessage, Toast.LENGTH_SHORT).show();
                    addAssistantMessage(getString(R.string.ai_error_message));
                });
            }
        });
    }

    private void addUserMessage(String message) {
        chatAdapter.addMessage(new ChatMessage(message, true));
        scrollToBottom();
    }

    private void addAssistantMessage(String message) {
        chatAdapter.addMessage(new ChatMessage(message, false));
        scrollToBottom();
    }

    private void scrollToBottom() {
        binding.rvChat.post(() ->
                binding.rvChat.smoothScrollToPosition(
                        Math.max(chatAdapter.getItemCount() - 1, 0)));
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