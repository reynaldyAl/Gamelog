package com.example.gamemology.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;

import com.example.gamemology.R;
import com.example.gamemology.ai.GeminiClient;
import com.example.gamemology.ai.utils.AIPromptUtils;
import com.example.gamemology.ui.search.FilterBottomSheet.FilterOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced search activity with AI capabilities
 * This extends the regular SearchActivity but adds AI-powered search features
 */
public class EnhancedSearchActivity extends SearchActivity {
    private static final String TAG = "EnhancedSearchActivity";

    // AI search components
    private SwitchCompat switchAiSearch;
    private TextView searchInterpretation;
    private View aiSearchIndicator;

    private GeminiClient geminiClient;
    private boolean isAiSearchEnabled = false;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Initialize Gemini client first to catch API key issues early
            geminiClient = new GeminiClient(this);

            // Set up enhanced search UI after parent's onCreate has completed
            setupEnhancedSearch();

        } catch (Exception e) {
            Log.e(TAG, "Error setting up enhanced search", e);
            Toast.makeText(this, "Error initializing AI search features", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupEnhancedSearch() {
        // Find the SearchView with the correct ID from layout
        searchView = findViewById(R.id.searchView);
        if (searchView == null) {
            Log.e(TAG, "SearchView not found");
            return;
        }

        // Find the container we added to the search layout
        ViewGroup controlsContainer = findViewById(R.id.search_controls_container);
        if (controlsContainer == null) {
            Log.e(TAG, "Could not find container for AI controls: search_controls_container");
            return;
        }

        // Directly inflate the AI search controls layout
        View aiControlsView = LayoutInflater.from(this)
                .inflate(R.layout.activity_enhanced_search, controlsContainer, false);
        controlsContainer.addView(aiControlsView);

        // Find views in the inflated layout
        switchAiSearch = aiControlsView.findViewById(R.id.switch_ai_search);
        searchInterpretation = aiControlsView.findViewById(R.id.search_interpretation);
        aiSearchIndicator = aiControlsView.findViewById(R.id.ai_search_indicator);

        if (switchAiSearch == null || searchInterpretation == null || aiSearchIndicator == null) {
            Log.e(TAG, "One or more views not found in activity_enhanced_search");
            return;
        }

        // Setup AI search toggle
        switchAiSearch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isAiSearchEnabled = isChecked;
            updateSearchHint();
            aiSearchIndicator.setVisibility(isChecked ? View.VISIBLE : View.GONE);

            // Hide interpretation when toggling off
            if (!isChecked) {
                searchInterpretation.setVisibility(View.GONE);
            }
        });

        // Initial state
        updateSearchHint();

        // Override search query listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (isAiSearchEnabled && query != null && !query.trim().isEmpty()) {
                    performAISearch(query);
                    return true;
                }
                // Let parent activity handle normal search
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false; // Default behavior
            }
        });
    }

    private void updateSearchHint() {
        if (searchView != null) {
            searchView.setQueryHint(isAiSearchEnabled ?
                    getString(R.string.ai_search_hint) :
                    getString(R.string.search_hint));
        }
    }

    private void performAISearch(String naturalQuery) {
        // Show progress
        View progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Set search interpretation hint
        searchInterpretation.setText(getString(R.string.analyzing_query));
        searchInterpretation.setVisibility(View.VISIBLE);

        // Use Gemini to interpret natural language query
        String searchPrompt = AIPromptUtils.createSearchPrompt(naturalQuery);
        geminiClient.sendPrompt(searchPrompt, new GeminiClient.ResponseCallback() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject searchParams = extractJsonFromResponse(response);
                    runOnUiThread(() -> {
                        // Apply extracted search parameters
                        applyAISearchParams(searchParams, naturalQuery);

                        // Extract keywords for the actual search
                        String keywords = searchParams.optString("keywords", naturalQuery);

                        // Execute search through standard interface
                        doSearch(keywords);

                        // Show interpretation
                        searchInterpretation.setText(
                                getString(R.string.ai_search_interpretation,
                                        getSearchDescription(searchParams)));
                        searchInterpretation.setVisibility(View.VISIBLE);
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing AI response", e);
                    runOnUiThread(() -> {
                        // Fall back to regular search
                        doSearch(naturalQuery);
                        Toast.makeText(EnhancedSearchActivity.this,
                                R.string.ai_search_fallback, Toast.LENGTH_SHORT).show();
                        searchInterpretation.setVisibility(View.GONE);
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "AI search error: " + errorMessage);
                runOnUiThread(() -> {
                    // Fall back to regular search
                    doSearch(naturalQuery);
                    Toast.makeText(EnhancedSearchActivity.this,
                            R.string.ai_search_fallback, Toast.LENGTH_SHORT).show();
                    searchInterpretation.setVisibility(View.GONE);
                });
            }
        });
    }

    // Safely execute search without accessing private methods
    private void doSearch(String query) {
        if (searchView != null) {
            // Set query without submitting
            searchView.setQuery(query, false);

            // Manually submit (this will trigger the parent activity)
            searchView.setIconified(false);
            searchView.setIconified(false);

            // Hide progress
            View progressBar = findViewById(R.id.progressBar);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private JSONObject extractJsonFromResponse(String response) throws JSONException {
        // Find JSON content in the response
        String jsonStr = response.trim();

        // Handle response that might have text before/after the JSON
        int startBrace = jsonStr.indexOf("{");
        int endBrace = jsonStr.lastIndexOf("}");

        if (startBrace >= 0 && endBrace > startBrace) {
            jsonStr = jsonStr.substring(startBrace, endBrace + 1);
        }

        return new JSONObject(jsonStr);
    }

    private void applyAISearchParams(JSONObject searchParams, String originalQuery) {
        FilterOptions options = new FilterOptions();

        // Extract and apply genre if present
        if (searchParams.has("genres")) {
            try {
                JSONArray genres = searchParams.getJSONArray("genres");
                if (genres.length() > 0) {
                    String genre = genres.getString(0);
                    // Apply genre filter - would need to map to genre IDs
                    // This is a simplified example
                    // options.setGenreId(...);
                    // options.setGenreName(genre);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing genres", e);
            }
        }

        // Extract and apply platform if present
        if (searchParams.has("platforms")) {
            try {
                JSONArray platforms = searchParams.getJSONArray("platforms");
                if (platforms.length() > 0) {
                    String platform = platforms.getString(0);
                    // Apply platform filter - would need to map to platform IDs
                    // options.setPlatformId(...);
                    // options.setPlatformName(platform);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing platforms", e);
            }
        }

        // Extract and apply year range if present
        if (searchParams.has("yearFrom")) {
            options.setFromYear(searchParams.optString("yearFrom"));
        }

        if (searchParams.has("yearTo")) {
            options.setToYear(searchParams.optString("yearTo"));
        }

        // Extract and apply minimum rating if present
        if (searchParams.has("minRating")) {
            double ratingDouble = searchParams.optDouble("minRating", 0);
            int rating = (int)Math.round(ratingDouble);  // Fix for lossy conversion
            options.setMinRating(rating);
        }

        // Apply the extracted filters
        onFiltersApplied(options);
    }

    private String getSearchDescription(JSONObject params) {
        StringBuilder desc = new StringBuilder();

        // Add keywords
        String keywords = params.optString("keywords", "");
        if (!keywords.isEmpty()) {
            desc.append(keywords);
        }

        // Add genres
        try {
            if (params.has("genres")) {
                JSONArray genres = params.getJSONArray("genres");
                if (genres.length() > 0) {
                    desc.append(" in ");
                    for (int i = 0; i < genres.length(); i++) {
                        desc.append(genres.getString(i));
                        if (i < genres.length() - 1) desc.append(", ");
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error formatting genres", e);
        }

        // Add platforms
        try {
            if (params.has("platforms")) {
                JSONArray platforms = params.getJSONArray("platforms");
                if (platforms.length() > 0) {
                    desc.append(" on ");
                    for (int i = 0; i < platforms.length(); i++) {
                        desc.append(platforms.getString(i));
                        if (i < platforms.length() - 1) desc.append(", ");
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error formatting platforms", e);
        }

        // Add year range
        String yearFrom = params.optString("yearFrom", "");
        String yearTo = params.optString("yearTo", "");
        if (!yearFrom.isEmpty() || !yearTo.isEmpty()) {
            if (!yearFrom.isEmpty() && !yearTo.isEmpty()) {
                desc.append(" from ").append(yearFrom).append(" to ").append(yearTo);
            } else if (!yearFrom.isEmpty()) {
                desc.append(" after ").append(yearFrom);
            } else {
                desc.append(" before ").append(yearTo);
            }
        }

        // Add rating
        double rating = params.optDouble("minRating", 0);
        if (rating > 0) {
            desc.append(" rated ").append(rating).append("+ stars");
        }

        return desc.toString();
    }
}