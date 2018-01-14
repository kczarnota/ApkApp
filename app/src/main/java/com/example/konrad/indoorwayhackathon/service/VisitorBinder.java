package com.example.konrad.indoorwayhackathon.service;

import android.os.Binder;

public class VisitorBinder extends Binder
{
    private VisitorSyncService mService;

    public VisitorBinder(VisitorSyncService service)
    {
        mService = service;
    }

    public VisitorSyncService getService()
    {
        return mService;
    }
}
