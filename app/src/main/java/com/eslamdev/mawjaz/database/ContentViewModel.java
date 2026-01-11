package com.eslamdev.mawjaz.database;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.eslamdev.mawjaz.api.ContentItem;
import com.eslamdev.mawjaz.api.Movie;
import com.eslamdev.mawjaz.api.TvShow;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ContentViewModel extends AndroidViewModel {
    private final MovieRepository movieRepository;
    private final String apiKey;
    private final String contentType;
    private final String category;
    private final String language;
    private final String countryCode;

    private final MutableLiveData<List<ContentItem>> items = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private int currentPage = 1;
    private boolean isLastPage = false;

    public ContentViewModel(@NonNull Application application, String apiKey, String contentType, String category, String language, String countryCode) {
        super(application);
        this.movieRepository = new MovieRepository(application);
        this.apiKey = apiKey;
        this.contentType = contentType;
        this.category = category;
        this.language = language;
        this.countryCode = countryCode;
        loadFirstPage();
    }

    public LiveData<List<ContentItem>> getItems() { return items; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void loadFirstPage() {
        currentPage = 1;
        isLastPage = false;
        isLoading.setValue(true);
        fetchData();
    }

    public void loadNextPage() {
        if (Boolean.TRUE.equals(isLoading.getValue()) || isLastPage) return;
        isLoading.setValue(true);
        currentPage++;
        fetchData();
    }


    private void fetchData() {
        if ("movie".equals(contentType)) {
            movieRepository.fetchMovies(apiKey, category, language, countryCode, currentPage, new MovieRepository.OnMoviesFetchedListener() {
                @Override
                public void onSuccess(List<Movie> movies) {
                    List<ContentItem> newItems = movies.stream().map(ContentItem::fromMovie).collect(Collectors.toList());
                    handleSuccess(newItems);
                }
                @Override
                public void onFailure() { handleFailure(); }
            });
        } else if ("tv".equals(contentType)) {
            movieRepository.fetchTvShows(apiKey, category, language, countryCode, currentPage, new MovieRepository.OnTvShowsFetchedListener() {
                @Override
                public void onSuccess(List<TvShow> tvShows) {
                    List<ContentItem> newItems = tvShows.stream().map(ContentItem::fromTvShow).collect(Collectors.toList());
                    handleSuccess(newItems);
                }
                @Override
                public void onFailure() { handleFailure(); }
            });
        }
    }

    private void handleSuccess(List<ContentItem> newItems) {
        if (newItems == null || newItems.isEmpty()) {
            isLastPage = true;
        }

        if (currentPage == 1) {
            items.setValue(newItems);
        } else {
            List<ContentItem> currentList = items.getValue() != null ? new ArrayList<>(items.getValue()) : new ArrayList<>();
            if (newItems != null) {
                currentList.addAll(newItems);
            }
            items.setValue(currentList);
        }
        isLoading.setValue(false);
    }

    private void handleFailure() {
        if (currentPage == 1) {
            items.setValue(null);
        }
        isLoading.setValue(false);
    }
}