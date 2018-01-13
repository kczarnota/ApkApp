package com.example.konrad.indoorwayhackathon.net.login;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Localization
{
    @SerializedName("longitude")
    @Expose
    public double longitude;
    @SerializedName("latitude")
    @Expose
    public double latitude;
}
