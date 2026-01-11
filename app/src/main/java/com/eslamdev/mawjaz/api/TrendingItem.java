package com.eslamdev.mawjaz.api;

import com.google.gson.annotations.SerializedName;

public class TrendingItem {

    @SerializedName("id")
    private int id;

    @SerializedName("media_type")
    private String mediaType;

    @SerializedName("overview")
    private String overview;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("vote_average")
    private double voteAverage;
    @SerializedName("original_language")
    private String originalLanguage;


    @SerializedName("title")
    private String title;
    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("name")
    private String name;
    @SerializedName("first_air_date")
    private String firstAirDate;


    public int getId() { return id; }
    public String getMediaType() { return mediaType; }
    public String getOverview() { return overview; }
    public String getPosterPath() { return posterPath; }
    public double getVoteAverage() { return voteAverage; }
    public String getOriginalLanguage() { return originalLanguage; }
    public String getTitle() { return title; }
    public String getReleaseDate() { return releaseDate; }
    public String getName() { return name; }
    public String getFirstAirDate() { return firstAirDate; }
}