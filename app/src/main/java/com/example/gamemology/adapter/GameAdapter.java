package com.example.gamemology.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.gamemology.R;
import com.example.gamemology.models.Game;

import java.util.ArrayList;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private final Context context;
    private final List<Game> gameList;
    private OnItemClickListener listener;

    public GameAdapter(Context context) {
        this.context = context;
        this.gameList = new ArrayList<>();
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = gameList.get(position);
        holder.bind(game);
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    public void setGames(List<Game> games) {
        this.gameList.clear();
        if (games != null) {
            this.gameList.addAll(games);
        }
        notifyDataSetChanged();
    }

    public void addGames(List<Game> games) {
        int startPosition = this.gameList.size();
        this.gameList.addAll(games);
        notifyItemRangeInserted(startPosition, games.size());
    }

    public Game getGameAt(int position) {
        if (position >= 0 && position < gameList.size()) {
            return gameList.get(position);
        }
        return null;
    }

    // Interface for click listener
    public interface OnItemClickListener {
        void onItemClick(Game game);
        void onFavoriteClick(Game game, boolean isFavorite);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // ViewHolder class
    public class GameViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgGame;
        private final ImageView imgFavorite;
        private final TextView txtTitle;
        private final TextView txtReleased;
        private final TextView txtRating;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            imgGame = itemView.findViewById(R.id.img_game);
            imgFavorite = itemView.findViewById(R.id.img_favorite);
            txtTitle = itemView.findViewById(R.id.txt_title);
            txtReleased = itemView.findViewById(R.id.txt_released);
            txtRating = itemView.findViewById(R.id.txt_rating);

            // Set click listener for whole item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(gameList.get(position));
                }
            });

            // Set click listener for favorite button
            imgFavorite.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Game game = gameList.get(position);
                    boolean currentState = game.isFavorite();
                    game.setFavorite(!currentState);
                    notifyItemChanged(position);
                    listener.onFavoriteClick(game, !currentState);
                }
            });
        }

        public void bind(Game game) {
            txtTitle.setText(game.getName());
            txtReleased.setText(game.getReleased());
            txtRating.setText(String.format("%.1f", game.getRating()));

            // Load image with Glide
            if (game.getBackgroundImage() != null && !game.getBackgroundImage().isEmpty()) {
                Glide.with(context)
                        .load(game.getBackgroundImage())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.placeholder_game)
                        .error(R.drawable.placeholder_game)
                        .into(imgGame);
            } else {
                imgGame.setImageResource(R.drawable.placeholder_game);
            }

            // Set favorite icon
            imgFavorite.setImageResource(game.isFavorite() ?
                    R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        }


    }
}