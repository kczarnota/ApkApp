package com.example.konrad.indoorwayhackathon;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Utils
{
    public static final String APP_TAG = "IndoorwayHackathon";
    public static final String BUILDING_UUID = "CScrSxCVhQg";
    public static final String ZERO_FLOOR_UUID = "7-QLYjkafkE";
    public static final String FIRST_FLOOR_UUID = "gVI7XXuBFCQ";
    public static final String SECOND_FLOOR_UUID = "3-_M01M3r5w";

    public static final String TOKEN_KEY = "token";

    public static String token;

    public static String getTag(String classTag)
    {
        return APP_TAG + " " + classTag;
    }

    public static void setToken(String t)
    {
        token = t;
    }

    public static String getToken()
    {
        return token;
    }
}
