package com.eslamdev.mawjaz.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.eslamdev.mawjaz.R;
import com.eslamdev.mawjaz.api.ContentItem;
import com.eslamdev.mawjaz.database.AppDatabase;
import com.eslamdev.mawjaz.database.FavoriteMovieDao;
import com.eslamdev.mawjaz.database.FavoriteMovieEntity;
import com.eslamdev.mawjaz.view.DetailActivity;
import com.eslamdev.mawjaz.view.TvShowDetailActivity;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentViewHolder> {

    private final Context context;
    private List<ContentItem> items = new ArrayList<>();
    private final FavoriteMovieDao favoriteMovieDao;
    private final ExecutorService databaseExecutor;

    public ContentAdapter(Context context) {
        this.context = context;
        AppDatabase db = AppDatabase.getInstance(context);
        this.favoriteMovieDao = db.favoriteMovieDao();
        this.databaseExecutor = Executors.newSingleThreadExecutor();
    }

    public void setItems(List<ContentItem> items) {
        this.items = (items != null) ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContentViewHolder holder, int position) {
        ContentItem item = items.get(position);

        holder.title.setText(item.getTitle());
        holder.rating.setText(String.format("%.1f", item.getVoteAverage()));
        String date = item.getReleaseDate();
        if (date != null && date.length() >= 4) {
            holder.releaseYear.setText(date.substring(0, 4));
        } else {
            holder.releaseYear.setText("N/A");
        }
        Picasso.get().load("https://image.tmdb.org/t/p/w500" + item.getPosterPath()).into(holder.poster);
        if (holder.overview != null) {
            if (!TextUtils.isEmpty(item.getOverview())) {
                holder.overview.setVisibility(View.VISIBLE);
                holder.overview.setText(item.getOverview());
            } else {
                holder.overview.setText(R.string.sample_movie_overview);
            }
        }

        // تفعيل زر المفضلة لكل العناصر
        holder.favoriteButton.setVisibility(View.VISIBLE);
        databaseExecutor.execute(() -> {
            boolean isFavorite = favoriteMovieDao.isMovieFavorite(item.getId());
            holder.itemView.post(() -> updateFavoriteButtonState(holder.favoriteButton, isFavorite));
        });


        ViewCompat.setTransitionName(holder.poster, "poster_" + item.getId());

        holder.itemView.setOnClickListener(v -> {

            if ("movie".equals(item.getType())) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("id", item.getId());
                intent.putExtra("title", item.getTitle());
                intent.putExtra("image_url", "https://image.tmdb.org/t/p/w500" + item.getPosterPath());

                intent.putExtra("original_language", item.getOriginalLanguage());

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        (Activity) context, holder.poster, ViewCompat.getTransitionName(holder.poster));
                context.startActivity(intent, options.toBundle());

            } else if ("tv".equals(item.getType())) {
                Intent intent = new Intent(context, TvShowDetailActivity.class);
                intent.putExtra("id", item.getId());
                intent.putExtra("title", item.getTitle());
                intent.putExtra("image_url", "https://image.tmdb.org/t/p/w500" + item.getPosterPath());

                intent.putExtra("original_language", item.getOriginalLanguage());

                context.startActivity(intent);
            }
        });

        holder.favoriteButton.setOnClickListener(v -> {
            FavoriteMovieEntity favoriteItem = new FavoriteMovieEntity(
                    item.getId(), item.getTitle(), item.getVoteAverage(),
                    item.getOverview(), item.getPosterPath(), item.getReleaseDate()
            );
            databaseExecutor.execute(() -> {
                boolean isCurrentlyFavorite = favoriteMovieDao.isMovieFavorite(favoriteItem.getId());
                if (isCurrentlyFavorite) {
                    favoriteMovieDao.deleteFavoriteMovie(favoriteItem);
                    holder.itemView.post(() -> {
                        updateFavoriteButtonState(holder.favoriteButton, false);
                        Toast.makeText(context,context.getString(R.string.removed_from_favorites) , Toast.LENGTH_SHORT).show();
                    });
                } else {
                    favoriteMovieDao.insertFavoriteMovie(favoriteItem);
                    holder.itemView.post(() -> {
                        updateFavoriteButtonState(holder.favoriteButton, true);
                        Toast.makeText(context, context.getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    private void updateFavoriteButtonState(MaterialButton button, boolean isFavorite) {
        if (isFavorite) {
            button.setIconResource(R.drawable.ic_favorite_filled);
            button.setIconTint(ContextCompat.getColorStateList(context, R.color.favorite_icon_tint_active));
        } else {
            button.setIconResource(R.drawable.ic_favorite_border);
            button.setIconTint(ContextCompat.getColorStateList(context, R.color.favorite_icon_tint_inactive));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ContentViewHolder extends RecyclerView.ViewHolder {
        TextView title, rating, releaseYear, overview;
        ImageView poster;
        MaterialButton favoriteButton;

        public ContentViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            poster = itemView.findViewById(R.id.poster);
            rating = itemView.findViewById(R.id.rating);
            releaseYear = itemView.findViewById(R.id.releaseYear);
            overview = itemView.findViewById(R.id.overview);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }
}