package com.eslamdev.mawjaz.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.eslamdev.mawjaz.BuildConfig;
import com.eslamdev.mawjaz.R;
import com.eslamdev.mawjaz.adapter.MovieAdapter;
import com.eslamdev.mawjaz.database.GenreMoviesViewModel;
import com.eslamdev.mawjaz.database.GenreMoviesViewModelFactory;
import com.google.android.material.button.MaterialButton;

public class GenreMoviesFragment extends Fragment {

    private GenreMoviesViewModel movieViewModel; // تغيير النوع
    private int genreId;

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyStateLayout;
    private View errorStateLayout;
    private TextView errorTextView;
    private LinearLayoutManager layoutManager;

    // دالة جديدة لإنشاء الـ Fragment مع تمرير الـ ID
    public static GenreMoviesFragment newInstance(int genreId) {
        GenreMoviesFragment fragment = new GenreMoviesFragment();
        Bundle args = new Bundle();
        args.putInt("GENRE_ID", genreId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            genreId = getArguments().getInt("GENRE_ID");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // استخدم layout موحد للـ fragments التي تعرض قوائم
        View view = inflater.inflate(R.layout.fragment_top_rated, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupViewModel();
        observeViewModel();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        errorStateLayout = view.findViewById(R.id.errorStateLayout);
        errorTextView = view.findViewById(R.id.errorTextView);
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        movieAdapter = new MovieAdapter(getContext());
        recyclerView.setAdapter(movieAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                boolean isLoading = movieViewModel.getIsLoading().getValue() != null && movieViewModel.getIsLoading().getValue();

                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5 && firstVisibleItemPosition >= 0 && totalItemCount > 0) {
                    movieViewModel.loadNextPage();
                }
            }
        });
    }

    private void setupViewModel() {
        showLoading();
        String apiKey = BuildConfig.TMDB_API_KEY;
        // استخدام الـ Factory والـ ViewModel الجديد
        GenreMoviesViewModelFactory factory = new GenreMoviesViewModelFactory(requireActivity().getApplication(), apiKey, genreId);
        movieViewModel = new ViewModelProvider(this, factory).get(GenreMoviesViewModel.class);
    }

    private void observeViewModel() {
        movieViewModel.getMovies().observe(getViewLifecycleOwner(), movies -> {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            if (movies != null && !movies.isEmpty()) {
                movieAdapter.setMovies(movies);
                showContent();
            } else if (movies != null) {
                showEmptyState();
            } else {
                showErrorState(getString(R.string.error_generic));
            }
        });

        movieViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // (اختياري) يمكنك إضافة منطق هنا لإظهار ProgressBar في آخر القائمة
        });
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (movieViewModel != null) {
                movieViewModel.loadFirstPage();
            }
        });

        MaterialButton errorRetryButton = errorStateLayout.findViewById(R.id.btnRetryError);
        if (errorRetryButton != null) {
            errorRetryButton.setOnClickListener(v -> {
                if (movieViewModel != null) {
                    movieViewModel.loadFirstPage();
                }
            });
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
        errorStateLayout.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        errorStateLayout.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
        errorStateLayout.setVisibility(View.GONE);
        TextView emptyText = emptyStateLayout.findViewById(R.id.emptyStateText);
        if (emptyText != null) {
            emptyText.setText(R.string.no_movies_in_genre); // رسالة عامة
        }
    }

    private void showErrorState(String message) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
        errorStateLayout.setVisibility(View.VISIBLE);
        errorTextView.setText(message);
    }
}