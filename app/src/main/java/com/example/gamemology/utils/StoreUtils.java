package com.example.gamemology.utils;

import com.example.gamemology.R;

public class StoreUtils {

    public static int getStoreIconResource(String storeSlug) {
        if (storeSlug == null) {
            return R.drawable.ic_store_generic;
        }

        switch (storeSlug) {
            case "steam":
                return R.drawable.ic_store_steam;
            case "playstation-store":
                return R.drawable.ic_store_ps;
            case "xbox-store":
                return R.drawable.ic_store_xbox;
            case "nintendo":
                return R.drawable.ic_store_nintendo;
            case "epic-games":
                return R.drawable.ic_store_epic;
            case "gog":
                return R.drawable.ic_store_gog;
            case "apple-appstore":
                return R.drawable.ic_store_apple;
            case "google-play":
                return R.drawable.ic_store_google_play;
            case "itch":
                return R.drawable.ic_store_itch;
            default:
                return R.drawable.ic_store_generic;
        }
    }
}