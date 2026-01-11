package com.eslamdev.mawjaz.api;

import com.google.gson.annotations.SerializedName;

public class TvShow {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("vote_average")
    private double voteAverage;

    @SerializedName("overview")
    private String overview;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("first_air_date")
    private String firstAirDate;
    @SerializedName("original_language")
    private String originalLanguage;

    public int getId() { return id; }
    public String getName() { return name; }
    public double getVoteAverage() { return voteAverage; }
    public String getOverview() { return overview; }
    public String getPosterPath() { return posterPath; }
    public String getFirstAirDate() { return firstAirDate; }
    public String getOriginalLanguage() {
        return originalLanguage;
    }
}