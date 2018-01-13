package com.example.konrad.indoorwayhackathon;


import android.app.Application;
import android.util.Log;

import com.indoorway.android.common.sdk.IndoorwaySdk;

public class IndoorwayApplication extends Application
{
    public static final String API_KEY = "ba1817ea-6e91-482c-8cf6-dbaa90b247cf";
    public static final String ACTIVITY_TAG = IndoorwayApplication.class.getSimpleName();

    @Override
    public void onCreate()
    {
        super.onCreate();
        // init application context on each Application start
        IndoorwaySdk.initContext(this);
        // it's up to you when to initialize IndoorwaySdk, once initialized it will work forever!
        IndoorwaySdk.configure(API_KEY);
        Log.d(Utils.getTag(ACTIVITY_TAG), "onCreate: creating application");
        Log.d(Utils.getTag(ACTIVITY_TAG), "onCreate: " + IndoorwaySdk.isConfigured());
    }
}
