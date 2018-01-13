package com.example.konrad.indoorwayhackathon.service;


import com.indoorway.android.common.sdk.model.VisitorLocation;

import java.util.Map;

public interface SyncListener {

    void onSyncCompleted(Map<String, VisitorLocation> visitorLocations);

    void onSyncError();

}
