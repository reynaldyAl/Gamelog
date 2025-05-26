package com.example.gamemology.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gamemology.R;
import com.example.gamemology.api.responses.StoreResponse;
import com.example.gamemology.utils.StoreUtils;

import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private final Context context;
    private final List<StoreResponse.StoreItem> stores;

    public StoreAdapter(Context context, List<StoreResponse.StoreItem> stores) {
        this.context = context;
        this.stores = stores;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        StoreResponse.StoreItem store = stores.get(position);
        holder.bind(store);
    }

    @Override
    public int getItemCount() {
        return stores.size();
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
            if (storeItem.getStore() != null) {
                tvStoreName.setText(storeItem.getStore().getName());

                // Set store icon based on slug
                int storeIconRes = StoreUtils.getStoreIconResource(storeItem.getStore().getSlug());
                imgStore.setImageResource(storeIconRes);

                // Set visit store button click listener
                btnVisitStore.setOnClickListener(v -> {
                    if (storeItem.getUrl() != null && !storeItem.getUrl().isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(storeItem.getUrl()));
                        context.startActivity(intent);
                    }
                });
            }
        }
    }
}