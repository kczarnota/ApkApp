package com.example.konrad.indoorwayhackathon.net.login;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/oauth/token")
    @Headers("Authorization: Basic dGVzdGp3dGNsaWVudGlkOlhZN2ttem9OemwxMDA=")
    @FormUrlEncoded
    Call<LoginResponse> doLogin(@Field("grant_type") String grant, @Field("username") String username, @Field("password") String password);

    @GET("/api/item/all")
    @FormUrlEncoded
    Call<LoginResponse> getItems(@HeaderMap Map<String, String> headers);
}

