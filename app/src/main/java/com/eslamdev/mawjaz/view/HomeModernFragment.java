package com.eslamdev.mawjaz.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eslamdev.mawjaz.BuildConfig;
import com.eslamdev.mawjaz.R;
import com.eslamdev.mawjaz.adapter.HomeMovieAdapter;
import com.eslamdev.mawjaz.api.ContentItem;
import com.eslamdev.mawjaz.database.MovieRepository;
import com.eslamdev.mawjaz.util.LocalHelper;
import com.squareup.picasso.Picasso;
import java.util.List;
import java.util.Random;

public class HomeModernFragment extends Fragment {

    private RecyclerView rvTrending, rvPopular, rvTopRated;
    private HomeMovieAdapter trendingAdapter, popularAdapter, topRatedAdapter;
    private ImageView featuredPoster;
    private TextView featuredTitle, featuredGenre;
    private Button btnFeaturedDetails;
    private MovieRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_modern, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerViews();
        loadData();
    }

    private void initViews(View view) {
        rvTrending = view.findViewById(R.id.rv_trending);
        rvPopular = view.findViewById(R.id.rv_popular);
        rvTopRated = view.findViewById(R.id.rv_top_rated);

        featuredPoster = view.findViewById(R.id.featured_poster);
        featuredTitle = view.findViewById(R.id.featured_title);
        featuredGenre = view.findViewById(R.id.featured_genre);
        btnFeaturedDetails = view.findViewById(R.id.btn_featured_details);
    }

    private void setupRecyclerViews() {
        trendingAdapter = new HomeMovieAdapter(requireContext());
        rvTrending.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTrending.setAdapter(trendingAdapter);

        popularAdapter = new HomeMovieAdapter(requireContext());
        rvPopular.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPopular.setAdapter(popularAdapter);

        topRatedAdapter = new HomeMovieAdapter(requireContext());
        rvTopRated.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTopRated.setAdapter(topRatedAdapter);
    }

    private void loadData() {
        repository = new MovieRepository(requireActivity().getApplication());
        String apiKey = BuildConfig.TMDB_API_KEY;
        String lang = LocalHelper.getPersistedLanguage(requireContext());
        if ("system".equals(lang)) lang = "en-US";

        repository.fetchTrending(apiKey, lang).observe(getViewLifecycleOwner(), items -> {
            if (items != null && !items.isEmpty()) {
                trendingAdapter.setItems(items);
                setupFeaturedMovie(items.get(new Random().nextInt(Math.min(items.size(), 5))));
            }
        });

        repository.getPopularMoviesHome(apiKey, lang).observe(getViewLifecycleOwner(), items -> {
            if (items != null) popularAdapter.setItems(items);
        });

        repository.getTopRatedMoviesHome(apiKey, lang).observe(getViewLifecycleOwner(), items -> {
            if (items != null) topRatedAdapter.setItems(items);
        });
    }

    private void setupFeaturedMovie(ContentItem item) {
        featuredTitle.setText(item.getTitle());
        featuredGenre.setText(item.getReleaseDate());

        if (item.getPosterPath() != null) {
            Picasso.get()
                    .load("https://image.tmdb.org/t/p/w780" + item.getPosterPath())
                    .into(featuredPoster);
        }

        btnFeaturedDetails.setOnClickListener(v -> {
            Intent intent;
            if ("tv".equals(item.getType())) {
                intent = new Intent(requireContext(), TvShowDetailActivity.class);
            } else {
                intent = new Intent(requireContext(), DetailActivity.class);
            }
            intent.putExtra("id", item.getId());
            intent.putExtra("title", item.getTitle());
            startActivity(intent);
        });
    }
}