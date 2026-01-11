package com.eslamdev.mawjaz.database;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SearchViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final String apiKey;
    public SearchViewModelFactory(Application application, String apiKey) {
        this.application = application;
        this.apiKey = apiKey;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SearchViewModel.class)) {
            return (T) new SearchViewModel(application, apiKey);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}