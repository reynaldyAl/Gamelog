package com.example.gamemology.utils;

import android.util.Log;
import com.example.gamemology.R;

public class StoreUtils {
    private static final String TAG = "StoreUtils";

    public static int getStoreIconResource(String storeSlug) {
        if (storeSlug == null) {
            Log.e(TAG, "Store slug is null");
            return R.drawable.ic_store_generic;
        }

        // Log the incoming slug for debugging
        Log.d(TAG, "Getting icon for store slug: " + storeSlug);

        // Convert to lowercase to ensure case-insensitive matching
        String slug = storeSlug.toLowerCase();

        switch (slug) {
            case "steam":
                return R.drawable.ic_store_steam;

            case "playstation-store":
            case "playstation":
            case "ps-store":
                return R.drawable.ic_store_ps;

            case "xbox-store":
            case "xbox":
            case "xbox360":
            case "xbox-marketplace":
                return R.drawable.ic_store_xbox;

            case "nintendo":
            case "nintendo-eshop":
                return R.drawable.ic_store_nintendo;

            case "epic-games":
            case "epic":
                return R.drawable.ic_store_epic;

            case "gog":
                return R.drawable.ic_store_gog;

            case "apple-appstore":
            case "app-store":
                return R.drawable.ic_store_apple;

            case "google-play":
            case "play-store":
                return R.drawable.ic_store_google_play;

            case "itch":
            case "itch.io":
                return R.drawable.ic_store_itch;

            default:
                Log.w(TAG, "Unknown store slug: " + slug + ", using generic icon");
                return R.drawable.ic_store_generic;
        }
    }
}