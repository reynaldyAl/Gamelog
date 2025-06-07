package com.example.gamemology.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.gamemology.models.User;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "GamelogSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER = "user";

    private static SessionManager instance;
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Gson gson;
    private Context context;

    private SessionManager(Context context) {
        this.pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = pref.edit();
        this.gson = new Gson();
        this.context = context.getApplicationContext();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            if (context == null) {
                throw new IllegalArgumentException("Context cannot be null for SessionManager initialization");
            }
            instance = new SessionManager(context.getApplicationContext());
        } else if (context != null) {
            // Update context if provided
            instance.context = context.getApplicationContext();
        }
        return instance;
    }

    /**
     * Set login status
     *
     * @param isLoggedIn true if logged in, false otherwise
     */
    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    /**
     * Check if user is logged in
     *
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Save user data in session
     *
     * @param user User to save
     */
    public void saveUser(User user) {
        String userJson = gson.toJson(user);
        editor.putString(KEY_USER, userJson);
        editor.apply();
        setLogin(true);
    }

    /**
     * Get current user
     *
     * @return User object or null if not logged in
     */
    public User getUser() {
        String userJson = pref.getString(KEY_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    /**
     * Logout and clear user data
     * Also cleans up user-specific cached data from database
     */
    public void logout() {
        if (context != null) {
            // First get the user ID before clearing the session
            User currentUser = getUser();

            // Clear session data
            editor.clear();
            editor.apply();

            if (currentUser != null) {
                // Clean up user-specific data in background
                new Thread(() -> {
                    OfflineManager offlineManager = OfflineManager.getInstance(context);
                    offlineManager.clearUserCache(currentUser.getId());
                }).start();
            }
        } else {
            // Just clear session data if context is null
            editor.clear();
            editor.apply();
        }
    }
}