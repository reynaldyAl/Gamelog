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

    private SessionManager(Context context) {
        this.pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = pref.edit();
        this.gson = new Gson();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void saveUser(User user) {
        String userJson = gson.toJson(user);
        editor.putString(KEY_USER, userJson);
        editor.apply();
        setLogin(true);
    }

    public User getUser() {
        String userJson = pref.getString(KEY_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}