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
import com.example.gamemology.models.Achievement;

import java.util.ArrayList;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private final Context context;
    private List<Achievement> achievements;

    public AchievementAdapter(Context context) {
        this.context = context;
        this.achievements = new ArrayList<>();
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        holder.bind(achievement);
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    public void setAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
        notifyDataSetChanged();
    }

    public void addAchievements(List<Achievement> achievements) {
        int positionStart = this.achievements.size();
        this.achievements.addAll(achievements);
        notifyItemRangeInserted(positionStart, achievements.size());
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgAchievement;
        private final TextView tvName;
        private final TextView tvDescription;
        private final TextView tvPercent;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAchievement = itemView.findViewById(R.id.img_achievement);
            tvName = itemView.findViewById(R.id.tv_achievement_name);
            tvDescription = itemView.findViewById(R.id.tv_achievement_description);
            tvPercent = itemView.findViewById(R.id.tv_achievement_percent);
        }

        public void bind(Achievement achievement) {
            tvName.setText(achievement.getName());
            tvDescription.setText(achievement.getDescription());
            tvPercent.setText(String.format("Earned by %.1f%% of players", achievement.getPercent()));

            // Load achievement image
            if (achievement.getImage() != null && !achievement.getImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(achievement.getImage())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.placeholder_achievement)
                        .error(R.drawable.placeholder_achievement)
                        .into(imgAchievement);
            } else {
                imgAchievement.setImageResource(R.drawable.placeholder_achievement);
            }
        }
    }
}