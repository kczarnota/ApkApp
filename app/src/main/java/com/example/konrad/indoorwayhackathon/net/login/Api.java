package com.example.konrad.indoorwayhackathon.net.login;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Api {
    public static ApiService getApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.100.194:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(ApiService.class);
    }
}
