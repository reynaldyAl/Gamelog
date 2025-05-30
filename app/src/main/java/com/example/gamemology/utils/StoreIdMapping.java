package com.example.gamemology.utils;

import android.util.Log;
import com.example.gamemology.api.responses.StoreResponse;

/**
 * Mapping utility to get store information from store_id since the API doesn't include full store objects
 */
public class StoreIdMapping {
    private static final String TAG = "StoreIdMapping";

    /**
     * Returns store information based on the store_id from the API
     * @param storeId The store ID from the API
     * @return A Store object with name and slug
     */
    public static StoreResponse.Store getStoreById(int storeId) {
        // Log the lookup for debugging
        Log.d(TAG, "Looking up store info for ID: " + storeId);

        StoreResponse.Store store;

        switch (storeId) {
            case 1:
                store = new StoreResponse.Store(1, "Steam", "steam");
                break;
            case 2:
                store = new StoreResponse.Store(2, "Microsoft Store", "xbox-store");
                break;
            case 3:
                store = new StoreResponse.Store(3, "PlayStation Store", "playstation-store");
                break;
            case 4:
                store = new StoreResponse.Store(4, "App Store", "apple-appstore");
                break;
            case 5:
                store = new StoreResponse.Store(5, "GOG", "gog");
                break;
            case 6:
                store = new StoreResponse.Store(6, "Nintendo Store", "nintendo");
                break;
            case 7:
                store = new StoreResponse.Store(7, "Xbox 360", "xbox360");
                break;
            case 8:
                store = new StoreResponse.Store(8, "Google Play", "google-play");
                break;
            case 9:
                store = new StoreResponse.Store(9, "itch.io", "itch");
                break;
            case 11:
                store = new StoreResponse.Store(11, "Epic Games Store", "epic-games");
                break;
            default:
                store = new StoreResponse.Store(storeId, "Store #" + storeId, "unknown");
                Log.w(TAG, "Unknown store ID: " + storeId);
        }

        Log.d(TAG, "Mapped store ID " + storeId + " to name: " + store.getName() + ", slug: " + store.getSlug());
        return store;
    }
}