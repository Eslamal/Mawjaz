package com.eslamdev.mawjaz.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CountrySpecificProviders {
    @SerializedName("link")
    private String link;

    @SerializedName("flatrate")
    private List<Provider> flatrate;

    public String getLink() {
        return link;
    }

    public List<Provider> getFlatrate() {
        return flatrate;
    }
}