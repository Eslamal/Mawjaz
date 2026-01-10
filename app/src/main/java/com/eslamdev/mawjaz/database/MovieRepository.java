package com.eslamdev.mawjaz.database;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.eslamdev.mawjaz.api.ContentItem;
import com.eslamdev.mawjaz.api.ContentResponse;
import com.eslamdev.mawjaz.api.Movie;
import com.eslamdev.mawjaz.api.MovieResponse;
import com.eslamdev.mawjaz.api.TMDbApi;
import com.eslamdev.mawjaz.api.TrendingItem;
import com.eslamdev.mawjaz.api.TvShow;
import com.eslamdev.mawjaz.api.TvShowResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieRepository {
    private static final String TAG = "MovieRepository";
    private final TMDbApi api;

    /**
     * واجهة Callback للتواصل بين الـ Repository والـ ViewModel.
     */
    public interface OnMoviesFetchedListener {
        void onSuccess(List<Movie> movies);
        void onFailure();
    }
    public interface OnTvShowsFetchedListener {
        void onSuccess(List<TvShow> tvShows);
        void onFailure();
    }

    public MovieRepository(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(TMDbApi.class);
        // تم حذف كود قاعدة البيانات مؤقتًا للتركيز على التحميل التدريجي
    }

    /**
     * الدالة الجديدة لجلب الأفلام التي تقبل رقم الصفحة و Callback.
     * @param page رقم الصفحة المطلوب تحميلها.
     * @param listener الـ Callback الذي سيتم استدعاؤه عند اكتمال الطلب.
     */
    public void fetchMovies(String apiKey, String category, String language, String countryCode, int page, OnMoviesFetchedListener listener) {
        Call<MovieResponse> call;
        if ("popular".equals(category)) {
            call = api.getPopularMovies(apiKey, language, page);
        } else if ("top_rated".equals(category)) {
            call = api.getTopRatedMovies(apiKey, language, page);
        } else if ("discover".equals(category) && countryCode != null) {
            // --- ADDED: Logic to handle fetching by country ---
            // We get the movie data in Arabic ("ar-EG") and filter by the country code
            call = api.discoverMoviesByCountry(apiKey, "ar-EG", countryCode, page);
        } else {
            Log.e(TAG, "Unknown movie category: " + category);
            listener.onFailure();
            return;
        }

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    listener.onSuccess(response.body().getResults());
                } else {
                    Log.e(TAG, "API response was not successful: " + response.message());
                    listener.onFailure();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Network request failed: " + t.getMessage());
                listener.onFailure();
            }
        });
    }

    /**
     * دالة البحث تبقى كما هي في الوقت الحالي.
     */
    public LiveData<List<ContentItem>> searchAllContent(String apiKey, String query, String language) {
        MutableLiveData<List<ContentItem>> combinedResults = new MutableLiveData<>();

        List<Movie> movieList = new ArrayList<>();
        List<TvShow> tvShowList = new ArrayList<>();
        AtomicInteger pendingCalls = new AtomicInteger(2);

        // API Call 1: Search for Movies
        api.searchMovies(apiKey, query, language).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    movieList.addAll(response.body().getResults());
                }
                if (pendingCalls.decrementAndGet() == 0) {
                    combineAndPostResults(movieList, tvShowList, combinedResults);
                }
            }
            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                if (pendingCalls.decrementAndGet() == 0) {
                    combineAndPostResults(movieList, tvShowList, combinedResults);
                }
            }
        });

        // API Call 2: Search for TV Shows
        api.searchTvShows(apiKey, query, language).enqueue(new Callback<TvShowResponse>() {
            @Override
            public void onResponse(@NonNull Call<TvShowResponse> call, @NonNull Response<TvShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvShowList.addAll(response.body().getResults());
                }
                if (pendingCalls.decrementAndGet() == 0) {
                    combineAndPostResults(movieList, tvShowList, combinedResults);
                }
            }
            @Override
            public void onFailure(@NonNull Call<TvShowResponse> call, @NonNull Throwable t) {
                if (pendingCalls.decrementAndGet() == 0) {
                    combineAndPostResults(movieList, tvShowList, combinedResults);
                }
            }
        });

        return combinedResults;
    }

    // --- ADDED: Helper method to combine results ---
    private void combineAndPostResults(List<Movie> movies, List<TvShow> tvShows, MutableLiveData<List<ContentItem>> liveData) {
        List<ContentItem> contentItems = new ArrayList<>();
        if (movies != null) {
            for (Movie movie : movies) {
                contentItems.add(ContentItem.fromMovie(movie));
            }
        }
        if (tvShows != null) {
            for (TvShow tvShow : tvShows) {
                contentItems.add(ContentItem.fromTvShow(tvShow));
            }
        }
        liveData.postValue(contentItems);
    }


    public void fetchMoviesByGenre(String apiKey, String language, int genreId, int page, OnMoviesFetchedListener listener) {
        api.discoverMoviesByGenre(apiKey, language, genreId, page).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body().getResults());
                } else {
                    listener.onFailure();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                listener.onFailure();
            }
        });
    }


    public void fetchTvShows(String apiKey, String category, String language, String countryCode, int page, OnTvShowsFetchedListener listener) {
        Call<TvShowResponse> call;
        if ("popular".equals(category)) {
            call = api.getPopularTvShows(apiKey, language, page);
        } else if ("top_rated".equals(category)) {
            call = api.getTopRatedTvShows(apiKey, language, page);
        } else if ("discover".equals(category) && countryCode != null) {
            // --- ADDED: Logic to handle fetching by country ---
            call = api.discoverTvShowsByCountry(apiKey, "ar-EG", countryCode, page);
        } else {
            Log.e(TAG, "Unknown TV show category: " + category);
            listener.onFailure();
            return;
        }

        call.enqueue(new Callback<TvShowResponse>() {
            @Override
            public void onResponse(@NonNull Call<TvShowResponse> call, @NonNull Response<TvShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body().getResults());
                } else {
                    listener.onFailure();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TvShowResponse> call, @NonNull Throwable t) {
                listener.onFailure();
            }
        });
    }


    public void fetchArabicMovies(String apiKey, String language, int page, OnMoviesFetchedListener listener) {
        // نستخدم "ar" كفلتر للغة الأصلية
        api.discoverArabicMovies(apiKey, language, "ar", page).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body().getResults());
                } else {
                    listener.onFailure();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                listener.onFailure();
            }
        });
    }

    public void fetchArabicTvShows(String apiKey, String language, int page, OnTvShowsFetchedListener listener) {
        // نستخدم "ar" كفلتر للغة الأصلية
        api.discoverArabicTvShows(apiKey, language, "ar", page).enqueue(new Callback<TvShowResponse>() {
            @Override
            public void onResponse(@NonNull Call<TvShowResponse> call, @NonNull Response<TvShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body().getResults());
                } else {
                    listener.onFailure();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TvShowResponse> call, @NonNull Throwable t) {
                listener.onFailure();
            }
        });
    }


    public LiveData<List<ContentItem>> fetchTrending(String apiKey, String language) {
        MutableLiveData<List<ContentItem>> trendingResults = new MutableLiveData<>();

        // 1. Call the correct API method that returns a ContentResponse
        api.getTrending(apiKey, language).enqueue(new Callback<ContentResponse>() {
            @Override
            public void onResponse(@NonNull Call<ContentResponse> call, @NonNull Response<ContentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ContentItem> contentItems = new ArrayList<>();
                    // 2. Loop through the list of TrendingItem
                    for (TrendingItem item : response.body().getResults()) {
                        // 3. Convert each TrendingItem to a unified ContentItem
                        ContentItem contentItem = ContentItem.fromTrendingItem(item);
                        // Add to the list only if it's a movie or tv show
                        if (contentItem != null) {
                            contentItems.add(contentItem);
                        }
                    }
                    trendingResults.postValue(contentItems);
                } else {
                    trendingResults.postValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ContentResponse> call, @NonNull Throwable t) {
                trendingResults.postValue(null);
            }
        });
        return trendingResults;
    }

    // --- ضيف الدوال دي في كلاس MovieRepository ---

    // دالة مساعدة لجلب البيانات للصفحة الرئيسية (Popular Movies)
    public LiveData<List<ContentItem>> getPopularMoviesHome(String apiKey, String language) {
        MutableLiveData<List<ContentItem>> data = new MutableLiveData<>();
        api.getPopularMovies(apiKey, language, 1).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ContentItem> items = new ArrayList<>();
                    for (Movie movie : response.body().getResults()) {
                        items.add(ContentItem.fromMovie(movie));
                    }
                    data.postValue(items);
                }
            }
            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                data.postValue(null);
            }
        });
        return data;
    }

    // دالة مساعدة لجلب البيانات للصفحة الرئيسية (Top Rated Movies)
    public LiveData<List<ContentItem>> getTopRatedMoviesHome(String apiKey, String language) {
        MutableLiveData<List<ContentItem>> data = new MutableLiveData<>();
        api.getTopRatedMovies(apiKey, language, 1).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ContentItem> items = new ArrayList<>();
                    for (Movie movie : response.body().getResults()) {
                        items.add(ContentItem.fromMovie(movie));
                    }
                    data.postValue(items);
                }
            }
            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                data.postValue(null);
            }
        });
        return data;
    }
}
