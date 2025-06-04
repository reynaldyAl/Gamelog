package com.example.gamemology.utils;

import android.os.Build;
import android.util.Log;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {
    private static final String TAG = "PasswordUtils";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    public static String hashPassword(String password) {
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            // Hash password with salt
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();

            // Format as base64(iterations):base64(salt):base64(hash)
            String iterations = Integer.toString(ITERATIONS);
            String saltStr = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                saltStr = Base64.getEncoder().encodeToString(salt);
            }
            String hashStr = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                hashStr = Base64.getEncoder().encodeToString(hash);
            }

            return iterations + ":" + saltStr + ":" + hashStr;
        } catch (Exception e) {
            Log.e(TAG, "Error hashing password", e);
            return null;
        }
    }

    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Split stored hash into parts
            String[] parts = storedHash.split(":");
            if (parts.length != 3) return false;

            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                salt = Base64.getDecoder().decode(parts[1]);
            }
            byte[] hash = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                hash = Base64.getDecoder().decode(parts[2]);
            }

            // Hash input password with same parameters
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, hash.length * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] testHash = factory.generateSecret(spec).getEncoded();

            // Compare hashes
            int diff = hash.length ^ testHash.length;
            for (int i = 0; i < hash.length && i < testHash.length; i++) {
                diff |= hash[i] ^ testHash[i];
            }
            return diff == 0;
        } catch (Exception e) {
            Log.e(TAG, "Error verifying password", e);
            return false;
        }
    }
}