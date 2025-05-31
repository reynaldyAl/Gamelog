package com.example.gamemology.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * A helper class that provides network status updates as LiveData
 */
public class NetworkStatusHelper extends LiveData<Boolean> {
    private final Context context;
    private final ConnectivityManager connectivityManager;
    private final NetworkReceiver networkReceiver;

    // Singleton pattern
    private static NetworkStatusHelper instance;

    public static NetworkStatusHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkStatusHelper(context.getApplicationContext());
        }
        return instance;
    }

    private NetworkStatusHelper(Context context) {
        this.context = context;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkReceiver = new NetworkReceiver();
    }

    @Override
    protected void onActive() {
        super.onActive();
        updateConnection();
        context.registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        try {
            context.unregisterReceiver(networkReceiver);
        } catch (Exception e) {
            // Receiver might not be registered
        }
    }

    /**
     * Updates the connection status
     */
    private void updateConnection() {
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            setValue(activeNetwork != null && activeNetwork.isConnectedOrConnecting());
        } else {
            setValue(false);
        }
    }

    /**
     * Receiver that gets notified on network changes
     */
    private class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateConnection();
        }
    }

    /**
     * Get current network status immediately (not as LiveData)
     */
    public boolean isNetworkAvailable() {
        return NetworkUtils.isNetworkAvailable(context);
    }
}