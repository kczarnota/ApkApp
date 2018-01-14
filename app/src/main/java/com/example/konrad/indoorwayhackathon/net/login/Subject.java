package com.example.konrad.indoorwayhackathon.net.login;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Subject
{
    @SerializedName("localization")
    @Expose
    public Localization localization;
    @SerializedName("name")
    @Expose
    public String name;
}
