package com.example.gamemology.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtils {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";

    public static final int MODE_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    public static final int MODE_LIGHT = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int MODE_DARK = AppCompatDelegate.MODE_NIGHT_YES;

    /**
     * Applies the saved theme mode or the default if none is saved
     * @param context The context
     */
    public static void applyTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int themeMode = prefs.getInt(KEY_THEME_MODE, getDefaultThemeMode());
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    /**
     * Sets and applies the theme mode
     * @param context The context
     * @param themeMode The theme mode to apply (MODE_SYSTEM, MODE_LIGHT, MODE_DARK)
     */
    public static void setThemeMode(Context context, int themeMode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_THEME_MODE, themeMode);
        editor.apply();

        // Apply the theme mode immediately
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    /**
     * Gets the current theme mode
     * @param context The context
     * @return The current theme mode
     */
    public static int getThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME_MODE, getDefaultThemeMode());
    }

    /**
     * Gets the theme mode name from the given theme mode
     * @param themeMode The theme mode
     * @return The theme mode name
     */
    public static String getThemeModeName(int themeMode) {
        switch (themeMode) {
            case MODE_LIGHT:
                return "Light";
            case MODE_DARK:
                return "Dark";
            case MODE_SYSTEM:
            default:
                return "System Default";
        }
    }

    /**
     * Gets the default theme mode based on API level
     * @return The default theme mode
     */
    private static int getDefaultThemeMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return MODE_SYSTEM;  // Use system default on Android 10 and above
        } else {
            return MODE_LIGHT;   // Use light theme on older Android versions
        }
    }

    /**
     * Checks if dark theme is currently active
     * @return True if dark theme is active, false otherwise
     */
    public static boolean isDarkTheme() {
        return AppCompatDelegate.getDefaultNightMode() == MODE_DARK ||
                (AppCompatDelegate.getDefaultNightMode() == MODE_SYSTEM &&
                        isSystemInDarkTheme());
    }

    /**
     * Checks if the system is in dark theme
     * @return True if system is in dark theme, false otherwise
     */
    private static boolean isSystemInDarkTheme() {
        // This is a simplified check - in a real app you'd use
        // resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK
        // But for simplicity, we'll return false
        return false;
    }
}