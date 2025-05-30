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
import com.example.gamemology.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private List<Category> categories;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Category category);
    }

    public CategoryAdapter(Context context) {
        this.context = context;
        this.categories = new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    public void addCategories(List<Category> categories) {
        int positionStart = this.categories.size();
        this.categories.addAll(categories);
        notifyItemRangeInserted(positionStart, categories.size());
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgCategory;
        private final TextView tvName;
        private final TextView tvGamesCount;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategory = itemView.findViewById(R.id.img_category);
            tvName = itemView.findViewById(R.id.tv_category_name);
            tvGamesCount = itemView.findViewById(R.id.tv_games_count);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(categories.get(position));
                }
            });
        }

        public void bind(Category category) {
            tvName.setText(category.getName());
            tvGamesCount.setText(context.getString(R.string.games_count, category.getGamesCount()));

            if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(category.getImageUrl())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.placeholder_game)
                        .error(R.drawable.placeholder_game)
                        .centerCrop()
                        .into(imgCategory);
            } else {
                imgCategory.setImageResource(R.drawable.placeholder_game);
            }
        }
    }
}