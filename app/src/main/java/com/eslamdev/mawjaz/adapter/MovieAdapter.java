package com.eslamdev.mawjaz.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.eslamdev.mawjaz.api.Movie;
import com.eslamdev.mawjaz.view.DetailActivity;
import com.google.android.material.button.MaterialButton;
import com.eslamdev.mawjaz.database.AppDatabase;
import com.eslamdev.mawjaz.database.FavoriteMovieDao;
import com.eslamdev.mawjaz.database.FavoriteMovieEntity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private final Context context;
    private List<Movie> movies = new ArrayList<>();
    private final FavoriteMovieDao favoriteMovieDao;
    private final ExecutorService databaseExecutor;
    private final boolean showDeleteButton;
    private final int layoutId;

    public interface OnMovieActionListener {
        void onMovieRemovedFromFavorites(Movie movie);
        void onFavoriteStatusChanged(Movie movie, boolean isFavorite);
    }
    private OnMovieActionListener movieActionListener;

    public void setOnMovieActionListener(OnMovieActionListener listener) {
        this.movieActionListener = listener;
    }

    public MovieAdapter(Context context, boolean showDeleteButton, int layoutId) {
        this.context = context;
        this.showDeleteButton = showDeleteButton;
        this.layoutId = layoutId;
        AppDatabase db = AppDatabase.getInstance(context);
        this.favoriteMovieDao = db.favoriteMovieDao();
        this.databaseExecutor = Executors.newSingleThreadExecutor();
    }

    public MovieAdapter(Context context) {
        this(context, false, R.layout.item_movie);
    }

    public void setMovies(List<Movie> movies) {
        this.movies = (movies != null) ? movies : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(this.layoutId, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);

        if (holder.title != null) {
            holder.title.setText(movie.getTitle());
        }
        if (holder.rating != null) {
            holder.rating.setText(String.format("%.1f", movie.getVoteAverage()));
        }
        if (holder.releaseYear != null) {
            String releaseDate = movie.getReleaseDate();
            if (releaseDate != null && releaseDate.length() >= 4) {
                holder.releaseYear.setText(releaseDate.substring(0, 4));
            } else {
                holder.releaseYear.setText("N/A");
            }
        }

        Picasso.get().load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath()).into(holder.poster);
        ViewCompat.setTransitionName(holder.poster, "poster_" + movie.getId());
        if (showDeleteButton) {
            if (holder.favoriteButton != null) holder.favoriteButton.setVisibility(View.GONE);
            if (holder.deleteButton != null) holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            if (holder.favoriteButton != null) {
                holder.favoriteButton.setVisibility(View.VISIBLE);
                databaseExecutor.execute(() -> {
                    boolean isFavorite = favoriteMovieDao.isMovieFavorite(movie.getId());
                    holder.itemView.post(() -> updateFavoriteButtonState(holder.favoriteButton, isFavorite));
                });
            }
            if (holder.deleteButton != null) holder.deleteButton.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("id", movie.getId());
            intent.putExtra("title", movie.getTitle());
            intent.putExtra("image_url", "https://image.tmdb.org/t/p/w500" + movie.getPosterPath());

            intent.putExtra("original_language", movie.getOriginalLanguage());

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    (Activity) context,
                    holder.poster,
                    ViewCompat.getTransitionName(holder.poster)
            );
            context.startActivity(intent, options.toBundle());
        });

        if (holder.favoriteButton != null) {
            holder.favoriteButton.setOnClickListener(v -> {
                FavoriteMovieEntity favoriteMovie = new FavoriteMovieEntity(
                        movie.getId(), movie.getTitle(), movie.getVoteAverage(),
                        movie.getOverview(), movie.getPosterPath(), movie.getReleaseDate()
                );
                databaseExecutor.execute(() -> {
                    boolean isCurrentlyFavorite = favoriteMovieDao.isMovieFavorite(favoriteMovie.getId());
                    if (isCurrentlyFavorite) {
                        favoriteMovieDao.deleteFavoriteMovie(favoriteMovie);
                        holder.itemView.post(() -> {
                            updateFavoriteButtonState(holder.favoriteButton, false);
                            Toast.makeText(context, movie.getTitle() + " removed from favorites", Toast.LENGTH_SHORT).show();
                            if (movieActionListener != null) {
                                movieActionListener.onFavoriteStatusChanged(movie, false);
                            }
                        });
                    } else {
                        favoriteMovieDao.insertFavoriteMovie(favoriteMovie);
                        holder.itemView.post(() -> {
                            updateFavoriteButtonState(holder.favoriteButton, true);
                            Toast.makeText(context, movie.getTitle() + " added to favorites", Toast.LENGTH_SHORT).show();
                            if (movieActionListener != null) {
                                movieActionListener.onFavoriteStatusChanged(movie, true);
                            }
                        });
                    }
                });
            });
        }

        if (holder.deleteButton != null) {
            holder.deleteButton.setOnClickListener(v -> {
                if (movieActionListener != null) {
                    movieActionListener.onMovieRemovedFromFavorites(movie);
                }
            });
        }
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
        return movies != null ? movies.size() : 0;
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView title, rating, releaseYear;
        ImageView poster;
        MaterialButton favoriteButton, deleteButton;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            poster = itemView.findViewById(R.id.poster);
            rating = itemView.findViewById(R.id.rating);
            releaseYear = itemView.findViewById(R.id.releaseYear);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}