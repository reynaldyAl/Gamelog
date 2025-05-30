package com.example.gamemology.adapter;

import android.content.Context;
import android.util.Log;
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
    private static final String TAG = "ScreenshotAdapter";
    private final Context context;
    private final List<Screenshot> screenshotList;
    private OnScreenshotClickListener listener;
    private int currentGameId = -1;

    public ScreenshotAdapter(Context context) {
        this.context = context;
        this.screenshotList = new ArrayList<>();
    }

    // Add this method to clear out previous game data
    public void clearItems() {
        this.screenshotList.clear();
        notifyDataSetChanged();
        Log.d(TAG, "All screenshots cleared");
    }

    // Update this method to use game-specific screenshots
    private void addHardCodedScreenshotsForGame(int gameId) {
        this.currentGameId = gameId;

        // Add different hard-coded screenshots based on game ID
        // This ensures we have fallback images unique to each game
        switch (gameId % 5) {
            case 0:
                screenshotList.add(new Screenshot(
                        1,
                        "https://media.rawg.io/media/screenshots/7b8/7b8895a23e8ca0dbd9e1ba24696579d9.jpg"
                ));
                screenshotList.add(new Screenshot(
                        2,
                        "https://media.rawg.io/media/screenshots/6aa/6aa56ef1485c8b287a913fa842883daa.jpg"
                ));
                break;
            case 1:
                screenshotList.add(new Screenshot(
                        1,
                        "https://media.rawg.io/media/screenshots/3d6/3d6066e45a2d241b5e92ab3ec98da34c.jpg"
                ));
                screenshotList.add(new Screenshot(
                        2,
                        "https://media.rawg.io/media/screenshots/a2e/a2e99d5003e6c3850bd14c85096b23a3.jpg"
                ));
                break;
            case 2:
                screenshotList.add(new Screenshot(
                        1,
                        "https://media.rawg.io/media/screenshots/375/375f84d018242d7519a230f623981217.jpg"
                ));
                screenshotList.add(new Screenshot(
                        2,
                        "https://media.rawg.io/media/screenshots/4c7/4c7f3c6761b7c24e40a5dfd4dff23cb5.jpg"
                ));
                break;
            case 3:
                screenshotList.add(new Screenshot(
                        1,
                        "https://media.rawg.io/media/screenshots/fd3/fd3a97519e6d5b416b4a22a2c450f7be.jpg"
                ));
                screenshotList.add(new Screenshot(
                        2,
                        "https://media.rawg.io/media/screenshots/7af/7af428c6596224d8c7c8d80a048bddf1.jpg"
                ));
                break;
            case 4:
                screenshotList.add(new Screenshot(
                        1,
                        "https://media.rawg.io/media/screenshots/58d/58d3d1e2cdf62b830bd5a5c5753935a5.jpg"
                ));
                screenshotList.add(new Screenshot(
                        2,
                        "https://media.rawg.io/media/screenshots/955/9556b26fa9b0c676b528fc092c8f8944.jpg"
                ));
                break;
        }

        Log.d(TAG, "Added hard-coded screenshots for game ID: " + gameId);
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
        Log.d(TAG, "Binding screenshot at position " + position + ": " + screenshot.getImageUrl());
    }

    @Override
    public int getItemCount() {
        return screenshotList.size();
    }

    public void setScreenshots(List<Screenshot> screenshots, int gameId) {
        this.screenshotList.clear();

        // Only add hard-coded screenshots if there are no real ones
        if (screenshots == null || screenshots.isEmpty()) {
            addHardCodedScreenshotsForGame(gameId);
        } else {
            this.screenshotList.addAll(screenshots);
            Log.d(TAG, "Added " + screenshots.size() + " real screenshots for game ID: " + gameId);
        }

        Log.d(TAG, "setScreenshots called, new list size: " + screenshotList.size() + " for game ID: " + gameId);
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
                Log.d(TAG, "Loading screenshot: " + screenshot.getImageUrl());
                Glide.with(context)
                        .load(screenshot.getImageUrl())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.placeholder_screenshot)
                        .error(R.drawable.placeholder_screenshot)
                        .into(imgScreenshot);
            } else {
                Log.d(TAG, "Screenshot has null or empty URL, using placeholder");
                imgScreenshot.setImageResource(R.drawable.placeholder_screenshot);
            }
        }
    }
}