package com.example.gamemology.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * Create a new image file in the app's private directory
     */
    public static File createImageFile(Context context, String name) throws IOException {
        // Create an image file name
        File storageDir = context.getFilesDir();
        return new File(storageDir, name + ".jpg");
    }

    /**
     * Copy a file from Uri to destination
     */
    public static void copyFile(@NonNull Context context, @NonNull Uri sourceUri, @NonNull File destFile) throws IOException {
        try (InputStream in = context.getContentResolver().openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(destFile)) {

            if (in == null) {
                throw new IOException("Failed to open input stream");
            }

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy file", e);
            throw e;
        }
    }
}