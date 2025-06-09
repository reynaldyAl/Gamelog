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
                return R.drawable.ic_store_playstation;


            case "xbox":
            case "xbox360":
            case "xbox-marketplace":
                return R.drawable.ic_xbox;

            case "xbox-store":
            case "microsoft-store":
            case "windows-store":
                return R.drawable.ic_microsoft_store;

            case "nintendo":
            case "nintendo-eshop":
                return R.drawable.ic_nintendo;

            case "epic-games":
            case "epic":
                return R.drawable.ic_store_epic_games;

            case "gog":
                return R.drawable.ic_gog;

            case "apple-appstore":
            case "app-store":
                return R.drawable.ic_apple;

            case "google-play":
            case "play-store":
                return R.drawable.ic_gstore;

            case "itch":
            case "itch.io":
                return R.drawable.ic_itch;

            default:
                Log.w(TAG, "Unknown store slug: " + slug + ", using generic icon");
                return R.drawable.ic_store_generic;
        }
    }
}