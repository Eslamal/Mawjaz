package com.eslamdev.mawjaz.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GenreListResponse {
    @SerializedName("genres")
    private List<Genre> genres;

    public List<Genre> getGenres() { return genres; }
}