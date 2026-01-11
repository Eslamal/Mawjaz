package com.eslamdev.mawjaz.database;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import com.eslamdev.mawjaz.api.Movie;

public class GenreMoviesViewModel extends AndroidViewModel {
    private final MovieRepository movieRepository;
    private final String apiKey;
    private final int genreId;
    private final MutableLiveData<List<Movie>> movies = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private int currentPage = 1;
    private boolean isLastPage = false;

    public GenreMoviesViewModel(@NonNull Application application, String apiKey, int genreId) {
        super(application);
        this.movieRepository = new MovieRepository(application);
        this.apiKey = apiKey;
        this.genreId = genreId;
        loadFirstPage();
    }

    public LiveData<List<Movie>> getMovies() { return movies; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void loadFirstPage() {
        currentPage = 1;
        isLastPage = false;
        isLoading.setValue(true);
        movieRepository.fetchMoviesByGenre(apiKey, "en-US", genreId, currentPage, new MovieRepository.OnMoviesFetchedListener() {
            @Override
            public void onSuccess(List<Movie> newMovies) {
                movies.setValue(newMovies);
                isLoading.setValue(false);
            }
            @Override
            public void onFailure() {
                movies.setValue(null);
                isLoading.setValue(false);
            }
        });
    }

    public void loadNextPage() {
        if (Boolean.TRUE.equals(isLoading.getValue()) || isLastPage) return;
        isLoading.setValue(true);
        currentPage++;
        movieRepository.fetchMoviesByGenre(apiKey, "en-US", genreId, currentPage, new MovieRepository.OnMoviesFetchedListener() {
            @Override
            public void onSuccess(List<Movie> newMovies) {
                if (newMovies == null || newMovies.isEmpty()) {
                    isLastPage = true;
                } else {
                    List<Movie> currentList = new ArrayList<>(movies.getValue() != null ? movies.getValue() : new ArrayList<>());
                    currentList.addAll(newMovies);
                    movies.setValue(currentList);
                }
                isLoading.setValue(false);
            }
            @Override
            public void onFailure() {
                isLoading.setValue(false);
            }
        });
    }
}