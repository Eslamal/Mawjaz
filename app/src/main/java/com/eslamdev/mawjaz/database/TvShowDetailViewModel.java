package com.eslamdev.mawjaz.database;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.eslamdev.mawjaz.api.ActorDetails;
import com.eslamdev.mawjaz.api.CastMember;
import com.eslamdev.mawjaz.api.TvShowDetails;
import com.eslamdev.mawjaz.database.DetailRepository.WatchProvidersResult;
import java.util.List;

public class TvShowDetailViewModel extends AndroidViewModel {

    private final DetailRepository repository;
    private final String apiKey;
    private final String originalLanguage;

    public final LiveData<TvShowDetails> tvShowDetails;
    public final LiveData<List<CastMember>> tvShowCast;
    public final LiveData<WatchProvidersResult> watchProviders;

    public final LiveData<Boolean> isFavorite;
    public final LiveData<Boolean> isInWatchlist;

    private final MutableLiveData<Integer> actorIdTrigger = new MutableLiveData<>();
    public final LiveData<ActorDetails> actorDetails;


    public TvShowDetailViewModel(@NonNull Application application, int tvId, String apiKey, String originalLanguage) {
        super(application);
        this.repository = new DetailRepository(application);
        this.apiKey = apiKey;
        this.originalLanguage = originalLanguage;

        this.tvShowDetails = repository.getTvShowDetails(tvId, apiKey, originalLanguage);
        this.tvShowCast = repository.getTvShowCast(tvId, apiKey, originalLanguage);
        this.watchProviders = repository.getTvShowWatchProviders(tvId, apiKey);

        this.isFavorite = repository.isFavorite(tvId);
        this.isInWatchlist = repository.isMovieInWatchlist(tvId);
        this.actorDetails = Transformations.switchMap(actorIdTrigger, id ->
                repository.getActorDetails(id, this.apiKey)
        );
    }

    public void toggleFavoriteStatus(FavoriteMovieEntity movie) {
        repository.toggleFavoriteStatus(movie);
    }

    public void toggleWatchlistStatus(FavoriteMovieEntity movie) {
        repository.toggleWatchlistStatus(movie);
    }

    public void fetchActorDetails(int actorId) {
        actorIdTrigger.setValue(actorId);
    }
}