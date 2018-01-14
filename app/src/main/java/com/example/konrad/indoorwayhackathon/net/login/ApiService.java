package com.example.konrad.indoorwayhackathon.net.login;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/oauth/token")
    @Headers("Authorization: Basic dGVzdGp3dGNsaWVudGlkOlhZN2ttem9OemwxMDA=")
    @FormUrlEncoded
    Call<LoginResponse> doLogin(@Field("grant_type") String grant, @Field("username") String username, @Field("password") String password);

    @GET("/api/item/all")
    Call<ItemsList> getItems(@HeaderMap Map<String, String> headers);

    @GET("api/quest/1")
    Call<Quest> getQuest(@HeaderMap Map<String, String> headers);

    @GET("/api/user/get/coin/")
    Call<Integer> getCoins(@HeaderMap Map<String, String> headers);

    @GET("/api/user/add/coin")
    Call<Void> postAdditionalPoints(@HeaderMap Map<String, String> headers, @Query("value") String value);

    @GET("/api/subject/2")
    Call<Subjects> getSubjects(@HeaderMap Map<String, String> headers);
}

