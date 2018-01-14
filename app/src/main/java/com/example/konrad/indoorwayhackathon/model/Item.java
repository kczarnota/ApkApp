package com.example.konrad.indoorwayhackathon.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Item
{
    @SerializedName("localization")
    @Expose
    public Localization localization;
    @SerializedName("value")
    @Expose
    public int value;
    @SerializedName("name")
    @Expose
    public String name;
}
