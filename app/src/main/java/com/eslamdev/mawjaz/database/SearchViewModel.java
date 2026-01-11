package com.eslamdev.mawjaz.database;

import android.app.Application;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.eslamdev.mawjaz.api.ContentItem;
import com.eslamdev.mawjaz.util.LocalHelper;
import java.util.List;

public class SearchViewModel extends AndroidViewModel {

    private final MovieRepository movieRepository;
    private final String apiKey;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();

    public final LiveData<List<ContentItem>> results;

    public SearchViewModel(@NonNull Application application, String apiKey) {
        super(application);
        this.movieRepository = new MovieRepository(application);
        this.apiKey = apiKey;

        this.results = Transformations.switchMap(searchQuery, query -> {
            String currentLanguageCode = LocalHelper.getPersistedLanguage(getApplication());
            if (currentLanguageCode.equals("system")) {
                currentLanguageCode = Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
            }

            if (query == null || query.trim().isEmpty()) {
                return movieRepository.fetchTrending(apiKey, currentLanguageCode);
            } else {
                return movieRepository.searchAllContent(apiKey, query, currentLanguageCode);
            }
        });
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void loadInitialData() {
        if (searchQuery.getValue() == null || !searchQuery.getValue().isEmpty()) {
            searchQuery.setValue("");
        }
    }
}