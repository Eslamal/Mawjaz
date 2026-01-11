package com.eslamdev.mawjaz.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.eslamdev.mawjaz.R;
import com.eslamdev.mawjaz.api.TvShow;
import com.eslamdev.mawjaz.view.TvShowDetailActivity;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class TvShowAdapter extends RecyclerView.Adapter<TvShowAdapter.TvShowViewHolder> {

    private final Context context;
    private List<TvShow> tvShows = new ArrayList<>();

    public TvShowAdapter(Context context) {
        this.context = context;
    }

    public void setTvShows(List<TvShow> tvShows) {
        this.tvShows = (tvShows != null) ? tvShows : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TvShowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new TvShowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TvShowViewHolder holder, int position) {
        TvShow tvShow = tvShows.get(position);

        holder.title.setText(tvShow.getName());
        holder.rating.setText(String.format("%.1f", tvShow.getVoteAverage()));

        String firstAirDate = tvShow.getFirstAirDate();
        if (firstAirDate != null && firstAirDate.length() >= 4) {
            holder.releaseYear.setText(firstAirDate.substring(0, 4));
        } else {
            holder.releaseYear.setText("N/A");
        }

        Picasso.get().load("https://image.tmdb.org/t/p/w500" + tvShow.getPosterPath()).into(holder.poster);

        if (holder.favoriteButton != null) {
            holder.favoriteButton.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TvShowDetailActivity.class);
            intent.putExtra("id", tvShow.getId());
            intent.putExtra("title", tvShow.getName());
            intent.putExtra("image_url", "https://image.tmdb.org/t/p/w500" + tvShow.getPosterPath());

            intent.putExtra("original_language", tvShow.getOriginalLanguage());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tvShows.size();
    }

    static class TvShowViewHolder extends RecyclerView.ViewHolder {
        TextView title, rating, releaseYear;
        ImageView poster;
        MaterialButton favoriteButton;

        public TvShowViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            poster = itemView.findViewById(R.id.poster);
            rating = itemView.findViewById(R.id.rating);
            releaseYear = itemView.findViewById(R.id.releaseYear);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }
}