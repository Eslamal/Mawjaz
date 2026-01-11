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
import com.eslamdev.mawjaz.api.ContentItem;
import com.eslamdev.mawjaz.view.DetailActivity;
import com.eslamdev.mawjaz.view.TvShowDetailActivity;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class HomeMovieAdapter extends RecyclerView.Adapter<HomeMovieAdapter.HomeViewHolder> {

    private final Context context;
    private List<ContentItem> items = new ArrayList<>();

    public HomeMovieAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<ContentItem> newItems) {
        if (newItems != null && newItems.size() > 10) {
            this.items = newItems.subList(0, 10);
        } else {
            this.items = newItems != null ? newItems : new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_home, parent, false);
        return new HomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        ContentItem item = items.get(position);

        holder.title.setText(item.getTitle());
        holder.rating.setText(String.format("%.1f", item.getVoteAverage()));

        String date = item.getReleaseDate();
        if (date != null && date.length() >= 4) {
            holder.releaseYear.setText(date.substring(0, 4));
        } else {
            holder.releaseYear.setText("");
        }

        if (item.getPosterPath() != null) {
            Picasso.get()
                    .load("https://image.tmdb.org/t/p/w342" + item.getPosterPath())
                    .placeholder(R.color.cardview_dark_background)
                    .into(holder.poster);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if ("tv".equals(item.getType())) {
                intent = new Intent(context, TvShowDetailActivity.class);
            } else {
                intent = new Intent(context, DetailActivity.class);
            }
            intent.putExtra("id", item.getId());
            intent.putExtra("title", item.getTitle());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HomeViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, rating, releaseYear;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster);
            title = itemView.findViewById(R.id.title);
            rating = itemView.findViewById(R.id.rating);
            releaseYear = itemView.findViewById(R.id.releaseYear);
        }
    }
}