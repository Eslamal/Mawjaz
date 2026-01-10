package com.eslamdev.mawjaz.view;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout; // <-- تم التعديل: استيراد LinearLayout
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eslamdev.mawjaz.R;
import com.eslamdev.mawjaz.adapter.MovieAdapter;
import com.eslamdev.mawjaz.api.Movie;
import com.eslamdev.mawjaz.database.AppDatabase;
import com.eslamdev.mawjaz.database.WatchlistMovieDao;
import com.eslamdev.mawjaz.database.WatchlistMovieEntity;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WatchlistActivity extends BaseActivity implements MovieAdapter.OnMovieActionListener {

    private RecyclerView watchlistRecyclerView;
    private LinearLayout emptyWatchlistText; // <-- تم التعديل: تغيير النوع من TextView لـ LinearLayout
    private MovieAdapter movieAdapter;
    private WatchlistMovieDao watchlistMovieDao;
    private ExecutorService databaseExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist);

        MaterialToolbar toolbar = findViewById(R.id.watchlist_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.watchlist_title)); // إضافة العنوان
        }

        watchlistRecyclerView = findViewById(R.id.watchlistRecyclerView);
        emptyWatchlistText = findViewById(R.id.emptyWatchlistText); // الربط الآن صحيح مع LinearLayout

        // تهيئة قاعدة البيانات
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        watchlistMovieDao = db.watchlistMovieDao();
        databaseExecutor = Executors.newSingleThreadExecutor();

        // إعداد الأدابتير
        // تأكد أن هذا الكونستركتور موجود في MovieAdapter، وإلا استخدم new MovieAdapter(this);
        movieAdapter = new MovieAdapter(this, true, R.layout.item_movie_grid);
        movieAdapter.setOnMovieActionListener(this);

        watchlistRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        watchlistRecyclerView.setAdapter(movieAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWatchlistMovies();
    }

    private void loadWatchlistMovies() {
        databaseExecutor.execute(() -> {
            List<WatchlistMovieEntity> watchlistEntities = watchlistMovieDao.getAllWatchlistMovies();
            final List<Movie> moviesToDisplay = new ArrayList<>();
            for (WatchlistMovieEntity entity : watchlistEntities) {
                moviesToDisplay.add(new Movie(
                        entity.getId(), entity.getTitle(), entity.getVoteAverage(),
                        entity.getOverview(), entity.getPosterPath(), entity.getReleaseDate()
                ));
            }

            runOnUiThread(() -> {
                if (moviesToDisplay.isEmpty()) {
                    emptyWatchlistText.setVisibility(View.VISIBLE);
                    watchlistRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyWatchlistText.setVisibility(View.GONE);
                    watchlistRecyclerView.setVisibility(View.VISIBLE);
                    movieAdapter.setMovies(moviesToDisplay);
                }
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMovieRemovedFromFavorites(Movie movie) {
        WatchlistMovieEntity movieToDelete = new WatchlistMovieEntity(
                movie.getId(), movie.getTitle(), movie.getVoteAverage(),
                movie.getOverview(), movie.getPosterPath(), movie.getReleaseDate()
        );

        databaseExecutor.execute(() -> {
            watchlistMovieDao.deleteWatchlistMovie(movieToDelete);
            runOnUiThread(this::loadWatchlistMovies);
        });

        Toast.makeText(this, movie.getTitle() + " " + getString(R.string.removed_from_watchlist), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavoriteStatusChanged(Movie movie, boolean isFavorite) {
        // لا نحتاج لعمل شيء هنا
    }
}