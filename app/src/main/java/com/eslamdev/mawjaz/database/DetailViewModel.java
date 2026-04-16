package com.eslamdev.mawjaz.database;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.eslamdev.mawjaz.api.ActorDetails;
import com.eslamdev.mawjaz.api.CastMember;
import com.eslamdev.mawjaz.api.Movie;

import java.util.List;

public class DetailViewModel extends AndroidViewModel {

    private final DetailRepository repository;
    private final String apiKey;
    private final String originalLanguage;

    public final LiveData<Boolean> isFavorite;
    public final LiveData<List<CastMember>> movieCast;
    public final LiveData<Boolean> isInWatchlist;
    public final LiveData<DetailRepository.WatchProvidersResult> watchProviders;
    public final LiveData<Movie> movieDetails;

    private final MutableLiveData<Integer> actorIdTrigger = new MutableLiveData<>();
    public final LiveData<ActorDetails> actorDetails;

    public DetailViewModel(@NonNull Application application, int movieId, String apiKey, String originalLanguage) {
        super(application);
        this.repository = new DetailRepository(application);
        this.apiKey = apiKey;
        this.originalLanguage = originalLanguage;
        this.movieDetails = repository.getMovieDetails(movieId, apiKey, this.originalLanguage);


        this.isFavorite = repository.isFavorite(movieId);
        this.isInWatchlist = repository.isMovieInWatchlist(movieId);
        this.watchProviders = repository.getWatchProviders(movieId, apiKey);


        this.movieCast = repository.getMovieCast(movieId, apiKey, this.originalLanguage);

        this.actorDetails = Transformations.switchMap(actorIdTrigger, id ->
                repository.getActorDetails(id, this.apiKey)
        );
    }

    public void toggleFavoriteStatus(FavoriteMovieEntity movie) {
        boolean currentlyIsFavorite = isFavorite.getValue() != null && isFavorite.getValue();
        if (currentlyIsFavorite) {
            repository.removeFromFavorites(movie);
        } else {
            repository.addToFavorites(movie);
        }
    }

    public void toggleWatchlistStatus(FavoriteMovieEntity movie) {
        repository.toggleWatchlistStatus(movie);
    }

    public void fetchActorDetails(int actorId) {
        actorIdTrigger.setValue(actorId);
    }
}