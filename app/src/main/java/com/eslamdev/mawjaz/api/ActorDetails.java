package com.eslamdev.mawjaz.api;

import com.google.gson.annotations.SerializedName;

public class ActorDetails {
    @SerializedName("name")
    private String name;

    @SerializedName("biography")
    private String biography;

    @SerializedName("birthday")
    private String birthday;

    @SerializedName("place_of_birth")
    private String placeOfBirth;

    @SerializedName("profile_path")
    private String profilePath;


    public String getName() { return name; }
    public String getBiography() { return biography; }
    public String getBirthday() { return birthday; }
    public String getPlaceOfBirth() { return placeOfBirth; }
    public String getProfilePath() { return profilePath; }
}