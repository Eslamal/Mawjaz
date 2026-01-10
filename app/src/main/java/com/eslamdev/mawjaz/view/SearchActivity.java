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

    // UI Components
    private TextInputEditText searchEditText;
    private TextInputLayout searchInputLayout;
    private RecyclerView recyclerView;
    private ContentAdapter contentAdapter;
    private ShimmerFrameLayout shimmerFrameLayout;
    private View emptyStateLayout;
    private TextView resultsTitleTextView;

    // Chips & Headers
    private ChipGroup chipGroupGenres;
    private ChipGroup chipGroupRecent;
    private View recentSearchesHeader;
    private MaterialButton btnClearHistory;

    // ViewModels
    private SearchViewModel searchViewModel;
    private GenresViewModel genresViewModel;

    // Data for History
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
        loadRecentSearches(); // تحميل سجل البحث
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
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // عرض شبكي (Grid)
        recyclerView.setAdapter(contentAdapter);
    }

    private void setupViewModels() {
        String apiKey = BuildConfig.TMDB_API_KEY;

        // 1. إعداد Search ViewModel
        SearchViewModelFactory factory = new SearchViewModelFactory(getApplication(), apiKey);
        searchViewModel = new ViewModelProvider(this, factory).get(SearchViewModel.class);

        // مراقبة نتائج البحث
        searchViewModel.results.observe(this, items -> {
            if (items == null) {
                showEmptyState();
            } else if (items.isEmpty()) {
                showEmptyState();
            } else {
                showContent(items);
            }
        });

        // 2. إعداد Genres ViewModel لجلب الأنواع
        genresViewModel = new ViewModelProvider(this).get(GenresViewModel.class);
        genresViewModel.getGenres().observe(this, genres -> {
            if (genres != null) {
                populateGenreChips(genres);
            }
        });
    }

    private void setupSearchInput() {
        // الاستماع لزر "بحث" في لوحة المفاتيح
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchEditText.getText().toString());
                return true;
            }
            return false;
        });

        // زر المسح (X) داخل خانة البحث
        searchInputLayout.setEndIconOnClickListener(v -> {
            searchEditText.setText("");
            showRecentSearchesState(); // العودة لحالة السجل عند المسح
        });

        // مراقبة الكتابة
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

        // 1. إخفاء الكيبورد
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

        // 2. تحديث الواجهة
        showLoading();
        resultsTitleTextView.setText(R.string.search_results_title);
        resultsTitleTextView.setVisibility(View.VISIBLE);

        // إخفاء الاقتراحات والسجل أثناء البحث
        hideHistoryAndGenres();

        // 3. حفظ في السجل
        saveToHistory(query);

        // 4. تنفيذ البحث
        searchViewModel.setSearchQuery(query);
    }

    // --- التحكم في حالات الواجهة ---

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
        contentAdapter.setItems(items); // تأكد أن ContentAdapter يحتوي على دالة setItems
    }

    private void showEmptyState() {
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    private void showRecentSearchesState() {
        // إعادة تهيئة الواجهة لعرض السجل والاقتراحات
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
        shimmerFrameLayout.setVisibility(View.GONE);
        resultsTitleTextView.setVisibility(View.GONE);

        loadRecentSearches(); // تحديث السجل

        // إظهار العناوين والـ Chips
        View genreScroll = (View) chipGroupGenres.getParent(); // الـ ScrollView الأب
        if (genreScroll != null) genreScroll.setVisibility(View.VISIBLE);

        // إظهار عنوان "اقتراحات الأنواع"
        // (نبحث عن الـ TextView اللي قبل الـ ScrollView مباشرة في الـ LinearLayout)
        // الحل الأبسط: نتأكد من الـ Visibility في loadRecentSearches و populateGenreChips
    }

    private void hideHistoryAndGenres() {
        chipGroupRecent.setVisibility(View.GONE);
        recentSearchesHeader.setVisibility(View.GONE);

        View genreScroll = (View) chipGroupGenres.getParent();
        if (genreScroll != null) genreScroll.setVisibility(View.GONE);

        // إخفاء عنوان "اقتراحات الأنواع" صعب الوصول له برمجيا بدون ID مباشر
        // لذا سنعتمد على إخفاء الـ Parent ScrollView كما فعلنا
    }

    // --- منطق الـ Chips ---

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

        // التأكد من أن القسم ظاهر لو في بيانات
        View genreScroll = (View) chipGroupGenres.getParent();
        if (genreScroll != null && !genres.isEmpty() && searchEditText.getText().length() == 0) {
            genreScroll.setVisibility(View.VISIBLE);
        }
    }

    // --- منطق سجل البحث (SharedPreferences) ---

    private void saveToHistory(String query) {
        SharedPreferences prefs = getSharedPreferences(PREFS_SEARCH, MODE_PRIVATE);
        String history = prefs.getString(KEY_RECENT_SEARCHES, "");

        // تحويل السلسلة لقائمة لتجنب التكرار ولإضافة الجديد في الأول
        List<String> historyList = new ArrayList<>(Arrays.asList(history.split(",")));

        // تنظيف القائمة (إزالة الفراغات والعنصر المكرر إذا وجد)
        historyList.remove(""); // إزالة أي عناصر فارغة
        if (historyList.contains(query)) {
            historyList.remove(query);
        }

        // إضافة البحث الجديد في المقدمة
        historyList.add(0, query);

        // الاحتفاظ بآخر 10 عمليات بحث فقط
        if (historyList.size() > 10) {
            historyList = historyList.subList(0, 10);
        }

        // الحفظ مرة أخرى
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
            // فقط أظهر السجل لو مربع البحث فاضي
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