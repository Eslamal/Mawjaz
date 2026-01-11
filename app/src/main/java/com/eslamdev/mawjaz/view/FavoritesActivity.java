package com.eslamdev.mawjaz.view;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eslamdev.mawjaz.R;
import com.eslamdev.mawjaz.adapter.MovieAdapter;
import com.eslamdev.mawjaz.api.Movie;
import com.eslamdev.mawjaz.database.AppDatabase;
import com.eslamdev.mawjaz.database.FavoriteMovieDao;
import com.eslamdev.mawjaz.database.FavoriteMovieEntity;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoritesActivity extends BaseActivity implements MovieAdapter.OnMovieActionListener {

    private RecyclerView favoritesRecyclerView;
    private LinearLayout emptyFavoritesText;
    private MovieAdapter movieAdapter;
    private FavoriteMovieDao favoriteMovieDao;
    private ExecutorService databaseExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        MaterialToolbar toolbar = findViewById(R.id.favorites_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.favorites_title));
        }

        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        emptyFavoritesText = findViewById(R.id.emptyFavoritesText);

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        favoriteMovieDao = db.favoriteMovieDao();
        databaseExecutor = Executors.newSingleThreadExecutor();

        movieAdapter = new MovieAdapter(this);
        movieAdapter.setOnMovieActionListener(this);

        favoritesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        favoritesRecyclerView.setAdapter(movieAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoriteMovies();
    }

    private void loadFavoriteMovies() {
        databaseExecutor.execute(() -> {
            List<FavoriteMovieEntity> favoriteEntities = favoriteMovieDao.getAllFavoriteMovies();
            final List<Movie> moviesToDisplay = new ArrayList<>();
            for (FavoriteMovieEntity entity : favoriteEntities) {
                moviesToDisplay.add(new Movie(
                        entity.getId(), entity.getTitle(), entity.getVoteAverage(),
                        entity.getOverview(), entity.getPosterPath(), entity.getReleaseDate()
                ));
            }

            runOnUiThread(() -> {
                if (moviesToDisplay.isEmpty()) {
                    emptyFavoritesText.setVisibility(View.VISIBLE);
                    favoritesRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyFavoritesText.setVisibility(View.GONE);
                    favoritesRecyclerView.setVisibility(View.VISIBLE);
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
        FavoriteMovieEntity movieToDelete = new FavoriteMovieEntity(
                movie.getId(), movie.getTitle(), movie.getVoteAverage(),
                movie.getOverview(), movie.getPosterPath(), movie.getReleaseDate()
        );

        databaseExecutor.execute(() -> {
            favoriteMovieDao.deleteFavoriteMovie(movieToDelete);
            runOnUiThread(this::loadFavoriteMovies);
        });

        String rem = getString(R.string.removed_from_favorites);
        Toast.makeText(this, movie.getTitle() + " " + rem, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavoriteStatusChanged(Movie movie, boolean isFavorite) {
        // لا نحتاج لتنفيذ شيء هنا لأننا في صفحة المفضلة، أي تغيير يعني الحذف
    }
}