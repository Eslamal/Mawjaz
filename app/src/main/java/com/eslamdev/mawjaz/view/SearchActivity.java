package com.eslamdev.mawjaz.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eslamdev.mawjaz.BuildConfig;
import com.eslamdev.mawjaz.R;
import com.eslamdev.mawjaz.adapter.ContentAdapter;
import com.eslamdev.mawjaz.api.ContentItem;
import com.eslamdev.mawjaz.api.Genre;
import com.eslamdev.mawjaz.database.GenresViewModel;
import com.eslamdev.mawjaz.database.SearchViewModel;
import com.eslamdev.mawjaz.database.SearchViewModelFactory;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchActivity extends BaseActivity {

    private TextInputEditText searchEditText;
    private TextInputLayout searchInputLayout;
    private RecyclerView recyclerView;
    private ContentAdapter contentAdapter;
    private ShimmerFrameLayout shimmerFrameLayout;
    private View emptyStateLayout;
    private TextView resultsTitleTextView;

    private ChipGroup chipGroupGenres;
    private ChipGroup chipGroupRecent;
    private View recentSearchesHeader;
    private MaterialButton btnClearHistory;

    private SearchViewModel searchViewModel;
    private GenresViewModel genresViewModel;

    private static final String PREFS_SEARCH = "search_prefs";
    private static final String KEY_RECENT_SEARCHES = "recent_searches_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModels();
        setupSearchInput();
        loadRecentSearches();
    }

    private void initViews() {
        searchEditText = findViewById(R.id.searchEditText);
        searchInputLayout = findViewById(R.id.searchInputLayout);
        recyclerView = findViewById(R.id.recyclerView);
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        resultsTitleTextView = findViewById(R.id.text_view_results_title);

        chipGroupGenres = findViewById(R.id.chip_group_genres);
        chipGroupRecent = findViewById(R.id.chip_group_recent);
        recentSearchesHeader = findViewById(R.id.recent_searches_header);
        btnClearHistory = findViewById(R.id.button_clear_history);

        btnClearHistory.setOnClickListener(v -> clearSearchHistory());
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupRecyclerView() {
        contentAdapter = new ContentAdapter(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(contentAdapter);
    }

    private void setupViewModels() {
        String apiKey = BuildConfig.TMDB_API_KEY;
        SearchViewModelFactory factory = new SearchViewModelFactory(getApplication(), apiKey);
        searchViewModel = new ViewModelProvider(this, factory).get(SearchViewModel.class);

        searchViewModel.results.observe(this, items -> {
            if (items == null) {
                showEmptyState();
            } else if (items.isEmpty()) {
                showEmptyState();
            } else {
                showContent(items);
            }
        });

        genresViewModel = new ViewModelProvider(this).get(GenresViewModel.class);
        genresViewModel.getGenres().observe(this, genres -> {
            if (genres != null) {
                populateGenreChips(genres);
            }
        });
    }

    private void setupSearchInput() {
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchEditText.getText().toString());
                return true;
            }
            return false;
        });

        searchInputLayout.setEndIconOnClickListener(v -> {
            searchEditText.setText("");
            showRecentSearchesState();
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    showRecentSearchesState();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) return;

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

        showLoading();
        resultsTitleTextView.setText(R.string.search_results_title);
        resultsTitleTextView.setVisibility(View.VISIBLE);

        hideHistoryAndGenres();
        saveToHistory(query);
        searchViewModel.setSearchQuery(query);
    }

    private void showLoading() {
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void showContent(List<ContentItem> items) {
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        contentAdapter.setItems(items);
    }

    private void showEmptyState() {
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    private void showRecentSearchesState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
        shimmerFrameLayout.setVisibility(View.GONE);
        resultsTitleTextView.setVisibility(View.GONE);

        loadRecentSearches();

        View genreScroll = (View) chipGroupGenres.getParent();
        if (genreScroll != null) genreScroll.setVisibility(View.VISIBLE);
    }

    private void hideHistoryAndGenres() {
        chipGroupRecent.setVisibility(View.GONE);
        recentSearchesHeader.setVisibility(View.GONE);

        View genreScroll = (View) chipGroupGenres.getParent();
        if (genreScroll != null) genreScroll.setVisibility(View.GONE);

    }


    private void populateGenreChips(List<Genre> genres) {
        chipGroupGenres.removeAllViews();
        for (Genre genre : genres) {
            Chip chip = new Chip(this);
            chip.setText(genre.getName());
            chip.setCheckable(false);
            chip.setChipBackgroundColorResource(R.color.colorSurfaceVariant);
            chip.setOnClickListener(v -> {
                searchEditText.setText(genre.getName());
                performSearch(genre.getName());
            });
            chipGroupGenres.addView(chip);
        }

        View genreScroll = (View) chipGroupGenres.getParent();
        if (genreScroll != null && !genres.isEmpty() && searchEditText.getText().length() == 0) {
            genreScroll.setVisibility(View.VISIBLE);
        }
    }

    private void saveToHistory(String query) {
        SharedPreferences prefs = getSharedPreferences(PREFS_SEARCH, MODE_PRIVATE);
        String history = prefs.getString(KEY_RECENT_SEARCHES, "");

        List<String> historyList = new ArrayList<>(Arrays.asList(history.split(",")));

        historyList.remove("");
        if (historyList.contains(query)) {
            historyList.remove(query);
        }

        historyList.add(0, query);

        if (historyList.size() > 10) {
            historyList = historyList.subList(0, 10);
        }

        String newHistory = TextUtils.join(",", historyList);
        prefs.edit().putString(KEY_RECENT_SEARCHES, newHistory).apply();
    }

    private void loadRecentSearches() {
        SharedPreferences prefs = getSharedPreferences(PREFS_SEARCH, MODE_PRIVATE);
        String history = prefs.getString(KEY_RECENT_SEARCHES, "");

        chipGroupRecent.removeAllViews();
        if (history.isEmpty()) {
            recentSearchesHeader.setVisibility(View.GONE);
            chipGroupRecent.setVisibility(View.GONE);
        } else {
            if (searchEditText.getText().length() == 0) {
                recentSearchesHeader.setVisibility(View.VISIBLE);
                chipGroupRecent.setVisibility(View.VISIBLE);
            }

            String[] items = history.split(",");
            for (String query : items) {
                if (query.trim().isEmpty()) continue;

                Chip chip = new Chip(this);
                chip.setText(query);
                chip.setCheckable(false);
                chip.setChipBackgroundColorResource(R.color.colorSurfaceVariant);
                chip.setOnClickListener(v -> {
                    searchEditText.setText(query);
                    performSearch(query);
                });
                chipGroupRecent.addView(chip);
            }
        }
    }

    private void clearSearchHistory() {
        SharedPreferences prefs = getSharedPreferences(PREFS_SEARCH, MODE_PRIVATE);
        prefs.edit().remove(KEY_RECENT_SEARCHES).apply();
        loadRecentSearches();
        Toast.makeText(this, "History Cleared", Toast.LENGTH_SHORT).show();
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