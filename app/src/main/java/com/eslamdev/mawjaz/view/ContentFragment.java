package com.eslamdev.mawjaz.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.eslamdev.mawjaz.adapter.ContentAdapter;
import com.eslamdev.mawjaz.database.ContentViewModel;
import com.eslamdev.mawjaz.database.ContentViewModelFactory;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;

public class ContentFragment extends Fragment {

    private ContentViewModel viewModel;
    private RecyclerView recyclerView;
    private ContentAdapter contentAdapter;
    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyStateLayout, errorStateLayout;
    private TextView errorTextView;
    private ShimmerFrameLayout shimmerFrameLayout;

    // --- THIS IS THE METHOD THAT NEEDS TO BE CORRECTED ---
    // It now accepts four parameters, including countryCode
    public static ContentFragment newInstance(String contentType, String category, String language, String countryCode) {
        ContentFragment fragment = new ContentFragment();
        Bundle args = new Bundle();
        args.putString("CONTENT_TYPE", contentType);
        args.putString("CATEGORY", category);
        args.putString("LANGUAGE", language);
        args.putString("COUNTRY_CODE", countryCode); // <-- Add countryCode to bundle
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_popular, container, false);
        initializeViews(view);
        setupRecyclerView();
        setupViewModel();
        observeViewModel();
        setupListeners();
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        errorStateLayout = view.findViewById(R.id.errorStateLayout);
        errorTextView = view.findViewById(R.id.errorTextView);
        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(getContext());
        contentAdapter = new ContentAdapter(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(contentAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (viewModel == null) return;

                boolean isLoading = viewModel.getIsLoading().getValue() != null && viewModel.getIsLoading().getValue();

                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5 && firstVisibleItemPosition >= 0 && totalItemCount > 0) {
                    viewModel.loadNextPage();
                }
            }
        });
    }

    private void setupViewModel() {
        if (getArguments() != null) {
            showLoading();
            String apiKey = BuildConfig.TMDB_API_KEY;
            String contentType = getArguments().getString("CONTENT_TYPE");
            String category = getArguments().getString("CATEGORY");
            String language = getArguments().getString("LANGUAGE");
            // --- THIS IS THE OTHER REQUIRED CHANGE ---
            // Get the countryCode from the bundle
            String countryCode = getArguments().getString("COUNTRY_CODE");

            // Pass the countryCode to the factory
            ContentViewModelFactory factory = new ContentViewModelFactory(requireActivity().getApplication(), apiKey, contentType, category, language, countryCode);
            viewModel = new ViewModelProvider(this, factory).get(ContentViewModel.class);
        }
    }

    private void observeViewModel() {
        if (viewModel == null) return;

        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            swipeRefreshLayout.setRefreshing(false);
            if (items != null && !items.isEmpty()) {
                contentAdapter.setItems(items);
                showContent();
            } else if (items != null) {
                showEmptyState();
            } else {
                showErrorState(getString(R.string.error_generic));
            }
        });
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (viewModel != null) viewModel.loadFirstPage();
        });

        MaterialButton retryButton = errorStateLayout.findViewById(R.id.btnRetryError);
        if (retryButton != null) {
            retryButton.setOnClickListener(v -> {
                if (viewModel != null) viewModel.loadFirstPage();
            });
        }
    }

    private void showLoading() {
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
        errorStateLayout.setVisibility(View.GONE);
    }

    private void showContent() {
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        errorStateLayout.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
        errorStateLayout.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
        errorStateLayout.setVisibility(View.VISIBLE);
        if (errorTextView != null) {
            errorTextView.setText(message);
        }
    }
}