package com.example.gamemology.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.gamemology.R;
import com.example.gamemology.models.Screenshot;

import java.util.ArrayList;
import java.util.List;

public class ScreenshotAdapter extends RecyclerView.Adapter<ScreenshotAdapter.ScreenshotViewHolder> {

    private final Context context;
    private final List<Screenshot> screenshotList;
    private OnScreenshotClickListener listener;

    public ScreenshotAdapter(Context context) {
        this.context = context;
        this.screenshotList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ScreenshotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_screenshot, parent, false);
        return new ScreenshotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScreenshotViewHolder holder, int position) {
        Screenshot screenshot = screenshotList.get(position);
        holder.bind(screenshot);
    }

    @Override
    public int getItemCount() {
        return screenshotList.size();
    }

    public void setScreenshots(List<Screenshot> screenshots) {
        this.screenshotList.clear();
        if (screenshots != null) {
            this.screenshotList.addAll(screenshots);
        }
        notifyDataSetChanged();
    }

    // Click listener interface
    public interface OnScreenshotClickListener {
        void onScreenshotClick(Screenshot screenshot, int position);
    }

    public void setOnScreenshotClickListener(OnScreenshotClickListener listener) {
        this.listener = listener;
    }

    // ViewHolder class
    public class ScreenshotViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgScreenshot;

        public ScreenshotViewHolder(@NonNull View itemView) {
            super(itemView);
            imgScreenshot = itemView.findViewById(R.id.img_screenshot);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onScreenshotClick(screenshotList.get(position), position);
                }
            });
        }

        public void bind(Screenshot screenshot) {
            if (screenshot.getImageUrl() != null && !screenshot.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(screenshot.getImageUrl())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.placeholder_screenshot)
                        .error(R.drawable.placeholder_screenshot)
                        .into(imgScreenshot);
            } else {
                imgScreenshot.setImageResource(R.drawable.placeholder_screenshot);
            }
        }
    }
}