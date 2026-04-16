package com.eslamdev.mawjaz.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TMDbApi {

    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(@Query("api_key") String apiKey, @Query("language") String language, @Query("page") int page);

    @GET("movie/top_rated")
    Call<MovieResponse> getTopRatedMovies(@Query("api_key") String apiKey, @Query("language") String language, @Query("page") int page);

    @GET("discover/movie")
    Call<MovieResponse> discoverArabicMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("with_original_language") String originalLanguage,
            @Query("page") int page
    );


    @GET("movie/{movie_id}/credits")
    Call<CreditsResponse> getMovieCredits(@Path("movie_id") int movieId, @Query("api_key") String apiKey, @Query("language") String language);

    @GET("movie/{movie_id}/watch/providers")
    Call<WatchProviderResults> getWatchProviders(@Path("movie_id") int movieId, @Query("api_key") String apiKey);


    @GET("tv/popular")
    Call<TvShowResponse> getPopularTvShows(@Query("api_key") String apiKey, @Query("language") String language, @Query("page") int page);

    @GET("tv/top_rated")
    Call<TvShowResponse> getTopRatedTvShows(@Query("api_key") String apiKey, @Query("language") String language, @Query("page") int page);

    @GET("discover/tv")
    Call<TvShowResponse> discoverArabicTvShows(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("with_original_language") String originalLanguage,
            @Query("page") int page
    );

    @GET("tv/{tv_id}")
    Call<TvShowDetails> getTvShowDetails(@Path("tv_id") int tvId, @Query("api_key") String apiKey, @Query("language") String language);

    @GET("tv/{tv_id}/credits")
    Call<CreditsResponse> getTvShowCredits(@Path("tv_id") int tvId, @Query("api_key") String apiKey, @Query("language") String language);

    @GET("tv/{tv_id}/watch/providers")
    Call<WatchProviderResults> getTvShowWatchProviders(@Path("tv_id") int tvId, @Query("api_key") String apiKey);


    @GET("search/movie")
    Call<MovieResponse> searchMovies(@Query("api_key") String apiKey, @Query("query") String query, @Query("language") String language);
    @GET("search/tv")
    Call<TvShowResponse> searchTvShows(@Query("api_key") String apiKey, @Query("query") String query, @Query("language") String language);

    @GET("person/{person_id}")
    Call<ActorDetails> getPersonDetails(@Path("person_id") int personId, @Query("api_key") String apiKey);

    @GET("genre/movie/list")
    Call<GenreListResponse> getGenres(@Query("api_key") String apiKey, @Query("language") String language);

    @GET("discover/movie")
    Call<MovieResponse> discoverMoviesByGenre(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("with_genres") int genreId,
            @Query("page") int page
    );

    @GET("discover/movie")
    Call<MovieResponse> discoverMoviesByCountry(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("with_origin_country") String countryCode,
            @Query("page") int page
    );

    @GET("discover/tv")
    Call<TvShowResponse> discoverTvShowsByCountry(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("with_origin_country") String countryCode,
            @Query("page") int page
    );
    @GET("movie/{movie_id}")
    Call<Movie> getMovieDetails(
            @Path("movie_id") int movieId,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET("trending/all/day")
    Call<ContentResponse> getTrending(
                                       @Query("api_key") String apiKey,
                                       @Query("language") String language
    );


}