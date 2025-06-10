package com.example.gamemology.ai;

import android.content.Context;
import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executor;

public class GeminiClient {
    private static final String TAG = "GeminiClient";
    private static final String API_KEY = "Insert Your Gemini Api Key here"; // Replace with your API key
    private static final String MODEL_NAME = "models/gemini-2.0-flash";

    private final GenerativeModelFutures model;
    private String systemPrompt;
    private final Executor executor;

    public GeminiClient(Context context) {
        GenerativeModel generativeModel = new GenerativeModel(
                MODEL_NAME,
                API_KEY
        );

        this.model = GenerativeModelFutures.from(generativeModel);
        this.executor = MoreExecutors.directExecutor();

        // Default system prompt for gaming context
        this.systemPrompt = "You are Gamemology Assistant, an AI expert about video games. " +
                "You provide helpful, accurate, and concise information about games, " +
                "gaming platforms, game mechanics, and recommendations. " +
                "Your responses should be friendly and conversational.";
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public void sendPrompt(String userPrompt, ResponseCallback callback) {
        try {
            // Combine system prompt and user prompt
            Content content = new Content.Builder()
                    .addText(systemPrompt + "\n\nUser: " + userPrompt)
                    .build();

            // Correct method call: Pass Content directly, not in a list
            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    if (result != null) {
                        // Get the response text using the correct API method
                        String responseText = result.getText();
                        callback.onResponse(responseText);
                    } else {
                        callback.onError("Empty response from AI");
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, "Error generating content", t);
                    callback.onError(t.getMessage());
                }
            }, executor);

        } catch (Exception e) {
            Log.e(TAG, "Exception in sendPrompt", e);
            callback.onError("Error: " + e.getMessage());
        }
    }

    public interface ResponseCallback {
        void onResponse(String response);
        void onError(String errorMessage);
    }
}