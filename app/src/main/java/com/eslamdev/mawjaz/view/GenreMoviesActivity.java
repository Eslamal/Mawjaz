package com.eslamdev.mawjaz.view;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;

import com.eslamdev.mawjaz.R;
import com.google.android.material.appbar.MaterialToolbar;

public class GenreMoviesActivity extends BaseActivity {

    public static final String EXTRA_GENRE_ID = "GENRE_ID";
    public static final String EXTRA_GENRE_NAME = "GENRE_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_movies);

        int genreId = getIntent().getIntExtra(EXTRA_GENRE_ID, -1);
        String genreName = getIntent().getStringExtra(EXTRA_GENRE_NAME);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(genreName != null ? genreName : getString(R.string.main_tab_movies));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null && genreId != -1) {
            GenreMoviesFragment fragment = GenreMoviesFragment.newInstance(genreId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}