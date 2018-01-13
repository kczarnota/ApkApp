package com.example.konrad.indoorwayhackathon.net.login;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ItemsList
{
    @SerializedName("list")
    @Expose
    public List<Item> list;
}
