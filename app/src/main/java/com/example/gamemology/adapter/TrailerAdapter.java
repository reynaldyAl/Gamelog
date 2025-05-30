package com.example.gamemology.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gamemology.R;
import com.example.gamemology.models.Trailer;

import java.util.ArrayList;
import java.util.List;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {
    private static final String TAG = "TrailerAdapter";
    private final Context context;
    private final List<Trailer> trailerList;
    private OnTrailerClickListener listener;
    private int currentGameId = -1;

    public TrailerAdapter(Context context) {
        this.context = context;
        this.trailerList = new ArrayList<>();
    }

    // Add this method to clear out previous game data
    public void clearItems() {
        this.trailerList.clear();
        notifyDataSetChanged();
        Log.d(TAG, "All trailers cleared");
    }

    // Update this method to use game-specific trailers
    private void addHardCodedTrailersForGame(int gameId) {
        this.currentGameId = gameId;

        // Add different hard-coded trailers based on game ID
        // This ensures we have fallback videos unique to each game
        switch (gameId % 3) {
            case 0:
                trailerList.add(new Trailer(
                        1,
                        "Official Game Trailer #" + gameId,
                        "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
                        "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                ));
                break;
            case 1:
                trailerList.add(new Trailer(
                        1,
                        "Gameplay Preview #" + gameId,
                        "https://i.ytimg.com/vi/LXb3EKWsInQ/maxresdefault.jpg",
                        "https://www.youtube.com/watch?v=LXb3EKWsInQ"
                ));
                break;
            case 2:
                trailerList.add(new Trailer(
                        1,
                        "Launch Trailer #" + gameId,
                        "https://i.ytimg.com/vi/NYBEYoAuFGA/maxresdefault.jpg",
                        "https://www.youtube.com/watch?v=NYBEYoAuFGA"
                ));
                break;
        }

        Log.d(TAG, "Added hard-coded trailer for game ID: " + gameId);
    }

    @NonNull
    @Override
    public TrailerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trailer, parent, false);
        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerViewHolder holder, int position) {
        Trailer trailer = trailerList.get(position);
        holder.bind(trailer);
        Log.d(TAG, "Binding trailer at position " + position + ": " + trailer.getName());
    }

    @Override
    public int getItemCount() {
        return trailerList.size();
    }

    public void setTrailers(List<Trailer> trailers, int gameId) {
        this.trailerList.clear();

        // Only add hard-coded trailer if there are no real ones
        if (trailers == null || trailers.isEmpty()) {
            addHardCodedTrailersForGame(gameId);
        } else {
            this.trailerList.addAll(trailers);
            Log.d(TAG, "Added " + trailers.size() + " real trailers for game ID: " + gameId);
        }

        Log.d(TAG, "setTrailers called, new list size: " + trailerList.size() + " for game ID: " + gameId);
        notifyDataSetChanged();
    }

    // Click listener interface
    public interface OnTrailerClickListener {
        void onTrailerClick(Trailer trailer);
    }

    public void setOnTrailerClickListener(OnTrailerClickListener listener) {
        this.listener = listener;
    }

    // ViewHolder class
    public class TrailerViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgThumbnail;
        private final TextView tvTrailerName;

        public TrailerViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.img_trailer_thumbnail);
            tvTrailerName = itemView.findViewById(R.id.tv_trailer_name);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTrailerClick(trailerList.get(position));
                }
            });
        }

        public void bind(Trailer trailer) {
            tvTrailerName.setText(trailer.getName());

            if (trailer.getPreviewImage() != null && !trailer.getPreviewImage().isEmpty()) {
                Log.d(TAG, "Loading image: " + trailer.getPreviewImage());
                Glide.with(context)
                        .load(trailer.getPreviewImage())
                        .placeholder(R.drawable.placeholder_trailer)
                        .error(R.drawable.placeholder_trailer)
                        .into(imgThumbnail);
            } else {
                Log.d(TAG, "No preview image, using placeholder");
                imgThumbnail.setImageResource(R.drawable.placeholder_trailer);
            }
        }
    }
}