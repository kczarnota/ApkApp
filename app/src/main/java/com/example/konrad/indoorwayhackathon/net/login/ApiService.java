package com.example.konrad.indoorwayhackathon.net.login;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/oauth/token")
    @Headers("Authorization: Basic dGVzdGp3dGNsaWVudGlkOlhZN2ttem9OemwxMDA=")
    @FormUrlEncoded
    Call<LoginResponse> doLogin(@Field("grant_type") String grant, @Field("username") String username, @Field("password") String password);
}

