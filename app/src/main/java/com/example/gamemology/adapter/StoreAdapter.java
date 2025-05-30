package com.example.gamemology.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamemology.R;
import com.example.gamemology.api.responses.StoreResponse;
import com.example.gamemology.utils.StoreUtils;

import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {
    private static final String TAG = "StoreAdapter";
    private final Context context;
    private final List<StoreResponse.StoreItem> stores;

    public StoreAdapter(Context context, List<StoreResponse.StoreItem> stores) {
        this.context = context;
        this.stores = stores;
        Log.d(TAG, "StoreAdapter created with " + (stores != null ? stores.size() : 0) + " stores");

        // Debug each store
        if (stores != null) {
            for (int i = 0; i < stores.size(); i++) {
                StoreResponse.StoreItem item = stores.get(i);
                if (item != null) {
                    Log.d(TAG, "Store " + i + ": Store ID=" + item.getStoreId() +
                            ", URL: " + item.getUrl());

                    // Get mapped store info
                    StoreResponse.Store store = item.getStore();
                    if (store != null) {
                        Log.d(TAG, "Mapped to: Name=" + store.getName() +
                                ", Slug=" + store.getSlug());
                    }
                } else {
                    Log.e(TAG, "Store " + i + " is null");
                }
            }
        }
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        StoreResponse.StoreItem storeItem = stores.get(position);
        holder.bind(storeItem);
    }

    @Override
    public int getItemCount() {
        return stores != null ? stores.size() : 0;
    }

    class StoreViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgStore;
        private final TextView tvStoreName;
        private final Button btnVisitStore;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            imgStore = itemView.findViewById(R.id.img_store);
            tvStoreName = itemView.findViewById(R.id.tv_store_name);
            btnVisitStore = itemView.findViewById(R.id.btn_visit_store);
        }

        public void bind(StoreResponse.StoreItem storeItem) {
            if (storeItem == null) {
                Log.e(TAG, "Store item is null");
                setUnknownStore();
                return;
            }

            // Get store ID for debugging
            int storeId = storeItem.getStoreId();
            Log.d(TAG, "Processing store with ID: " + storeId);

            // Get mapped store info
            StoreResponse.Store store = storeItem.getStore();

            if (store == null) {
                Log.e(TAG, "Store object is null for store ID: " + storeId);
                setUnknownStore();
                return;
            }

            String storeName = store.getName();
            String storeSlug = store.getSlug();
            String storeUrl = storeItem.getUrl();

            Log.d(TAG, "Binding store: " + storeName + " (slug: " + storeSlug + ")");
            Log.d(TAG, "Store URL: " + storeUrl);

            // Set store name
            tvStoreName.setText(storeName);

            // Set store icon based on slug
            int storeIconRes = StoreUtils.getStoreIconResource(storeSlug);
            Log.d(TAG, "Setting icon resource: " + storeIconRes + " for " + storeSlug);
            imgStore.setImageResource(storeIconRes);

            // Set visit store button click listener
            if (storeUrl != null && !storeUrl.isEmpty()) {
                btnVisitStore.setEnabled(true);
                btnVisitStore.setOnClickListener(v -> {
                    try {
                        Log.d(TAG, "Opening URL: " + storeUrl);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl));
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error opening URL: " + e.getMessage());
                        Toast.makeText(context, "Cannot open store link", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e(TAG, "Store URL is null or empty");
                btnVisitStore.setEnabled(false);
            }
        }

        private void setUnknownStore() {
            tvStoreName.setText(R.string.unknown_store);
            imgStore.setImageResource(R.drawable.ic_store_generic);
            btnVisitStore.setEnabled(false);
        }
    }
}