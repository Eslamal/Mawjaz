package com.eslamdev.mawjaz.database;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class DetailViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final int movieId;
    private final String apiKey;
    private final String originalLanguage;

    public DetailViewModelFactory(Application application, int movieId, String apiKey, String originalLanguage) {
        this.application = application;
        this.movieId = movieId;
        this.apiKey = apiKey;
        this.originalLanguage = originalLanguage;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DetailViewModel.class)) {
            return (T) new DetailViewModel(application, movieId, apiKey, originalLanguage);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}