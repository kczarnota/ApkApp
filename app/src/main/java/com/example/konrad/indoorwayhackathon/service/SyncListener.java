package com.example.konrad.indoorwayhackathon.service;


import com.indoorway.android.common.sdk.model.RegisteredVisitor;
import com.indoorway.android.common.sdk.model.VisitorLocation;

import java.util.Map;

public interface SyncListener
{

    void onSyncCompleted(Map<RegisteredVisitor, VisitorLocation> visitorLocations);

    void onSyncError();

}
