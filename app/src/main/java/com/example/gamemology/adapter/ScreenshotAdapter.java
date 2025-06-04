package com.example.gamemology.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.gamemology.R;
import com.example.gamemology.models.Screenshot;

import java.util.ArrayList;
import java.util.List;

public class ScreenshotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ScreenshotAdapter";
    private static final int VIEW_TYPE_SCREENSHOT = 0;
    private static final int VIEW_TYPE_LOAD_MORE = 1;

    private final Context context;
    private final List<Screenshot> screenshotList;
    private OnScreenshotClickListener listener;
    private OnLoadMoreClickListener loadMoreListener;
    private int currentGameId = -1;
    private boolean hasMoreScreenshots = false;
    private boolean isLoadingMore = false;

    public ScreenshotAdapter(Context context) {
        this.context = context;
        this.screenshotList = new ArrayList<>();
    }

    public void clearItems() {
        this.screenshotList.clear();
        notifyDataSetChanged();
        Log.d(TAG, "All screenshots cleared");
    }

    private void addHardCodedScreenshotsForGame(int gameId) {
        // Existing code for hardcoded screenshots...
    }

    @Override
    public int getItemViewType(int position) {
        return (position < screenshotList.size()) ? VIEW_TYPE_SCREENSHOT : VIEW_TYPE_LOAD_MORE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LOAD_MORE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_load_more, parent, false);
            return new LoadMoreViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_screenshot, parent, false);
            return new ScreenshotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ScreenshotViewHolder) {
            Screenshot screenshot = screenshotList.get(position);
            ((ScreenshotViewHolder) holder).bind(screenshot);
            Log.d(TAG, "Binding screenshot at position " + position + ": " + screenshot.getImageUrl());
        } else if (holder instanceof LoadMoreViewHolder) {
            ((LoadMoreViewHolder) holder).bind(isLoadingMore);
        }
    }

    @Override
    public int getItemCount() {
        return screenshotList.size() + (hasMoreScreenshots ? 1 : 0);
    }

    public void setScreenshots(List<Screenshot> screenshots, int gameId) {
        this.screenshotList.clear();

        // Only add hard-coded screenshots if there are no real ones
        if (screenshots == null || screenshots.isEmpty()) {
            addHardCodedScreenshotsForGame(gameId);
            hasMoreScreenshots = false;
        } else {
            this.screenshotList.addAll(screenshots);
            hasMoreScreenshots = true;
            Log.d(TAG, "Added " + screenshots.size() + " real screenshots for game ID: " + gameId);
        }

        Log.d(TAG, "setScreenshots called, new list size: " + screenshotList.size() + " for game ID: " + gameId);
        notifyDataSetChanged();
    }

    public void addMoreScreenshots(List<Screenshot> newScreenshots, boolean hasMore) {
        int startPosition = screenshotList.size();
        this.screenshotList.addAll(newScreenshots);
        this.hasMoreScreenshots = hasMore;
        this.isLoadingMore = false;

        notifyItemRangeInserted(startPosition, newScreenshots.size());
        notifyItemChanged(screenshotList.size()); // Update load more button

        Log.d(TAG, "Added " + newScreenshots.size() + " more screenshots. Has more: " + hasMore);
    }

    public void setLoadingMore(boolean isLoading) {
        this.isLoadingMore = isLoading;
        if (hasMoreScreenshots) {
            notifyItemChanged(screenshotList.size());
        }
    }

    public void setHasMoreScreenshots(boolean hasMore) {
        if (this.hasMoreScreenshots != hasMore) {
            this.hasMoreScreenshots = hasMore;
            notifyDataSetChanged();
        }
    }

    // Screenshot click listener interface
    public interface OnScreenshotClickListener {
        void onScreenshotClick(Screenshot screenshot, int position);
    }

    // Load more click listener interface
    public interface OnLoadMoreClickListener {
        void onLoadMoreClick();
    }

    public void setOnScreenshotClickListener(OnScreenshotClickListener listener) {
        this.listener = listener;
    }

    public void setOnLoadMoreClickListener(OnLoadMoreClickListener listener) {
        this.loadMoreListener = listener;
    }

    // ViewHolder for screenshots
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

    // ViewHolder for load more button
    public class LoadMoreViewHolder extends RecyclerView.ViewHolder {
        private final Button btnLoadMore;
        private final ProgressBar progressBar;

        public LoadMoreViewHolder(@NonNull View itemView) {
            super(itemView);
            btnLoadMore = itemView.findViewById(R.id.btn_load_more);
            progressBar = itemView.findViewById(R.id.progress_bar);

            btnLoadMore.setOnClickListener(v -> {
                if (loadMoreListener != null && !isLoadingMore) {
                    isLoadingMore = true;
                    updateLoadingState();
                    loadMoreListener.onLoadMoreClick();
                }
            });
        }

        public void bind(boolean isLoading) {
            isLoadingMore = isLoading;
            updateLoadingState();
        }


        private void updateLoadingState() {
            btnLoadMore.setVisibility(isLoadingMore ? View.INVISIBLE : View.VISIBLE);
            progressBar.setVisibility(isLoadingMore ? View.VISIBLE : View.GONE);
        }
    }
}