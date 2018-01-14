package com.example.konrad.indoorwayhackathon.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.indoorway.android.common.sdk.IndoorwaySdk;
import com.indoorway.android.common.sdk.listeners.generic.Action1;
import com.indoorway.android.common.sdk.model.RegisteredVisitor;
import com.indoorway.android.common.sdk.model.VisitorLocation;
import com.indoorway.android.common.sdk.task.IndoorwayTask;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

public class VisitorSyncService extends Service
{
    public static final int SYNC_FREQUENCY_MS = 100;

    Set<SyncListener> mListeners = new CopyOnWriteArraySet<>();
    AtomicBoolean mIsRunning = new AtomicBoolean(false);
    AtomicBoolean mIsDownloading = new AtomicBoolean(false);
    Thread mWorkerThread;
    Handler mHandler;

    public VisitorSyncService()
    {

    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        mIsRunning.set(true);
        mHandler = new Handler(Looper.getMainLooper());
        mWorkerThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (mIsRunning.get())
                {
                    try
                    {
                        // polling locations
                        fetchLocations();
                        Thread.sleep(SYNC_FREQUENCY_MS);
                    } catch (InterruptedException e)
                    {
                        // exit
                    }
                }
            }
        });
        mWorkerThread.start();
    }

    private void fetchLocations()
    {
        if (mIsDownloading.compareAndSet(false, true))
        {
            IndoorwaySdk.instance()
                    .visitors()
                    .locations()
                    .setOnCompletedListener(new Action1<List<VisitorLocation>>()
                    {
                        @Override
                        public void onAction(List<VisitorLocation> visitorLocations)
                        {
                            getVisitorsNow(visitorLocations);
                        }
                    })
                    .setOnFailedListener(new Action1<IndoorwayTask.ProcessingException>()
                    {
                        @Override
                        public void onAction(IndoorwayTask.ProcessingException e)
                        {
                            mIsDownloading.set(false);
                        }
                    }).execute();
        }
    }

    private void onVisitorLocationsFetchFailed()
    {
    }

    private void getVisitorsNow(List<VisitorLocation> visitorLocations)
    {
        final List<VisitorLocation> visitorLocationLinkedList = new LinkedList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1);
        Date fiveMinutesAgo = calendar.getTime();
        for (VisitorLocation location : visitorLocations)
        {
            if (location.getTimestamp() != null && !location.getTimestamp().before(fiveMinutesAgo))
                visitorLocationLinkedList.add(location);
        }
        IndoorwaySdk.instance()
                .visitors()
                .list()
                .setOnCompletedListener(new Action1<List<RegisteredVisitor>>()
                {
                    @Override
                    public void onAction(List<RegisteredVisitor> visitors)
                    {
                        Map<RegisteredVisitor, VisitorLocation> map = new HashMap<>();
                        mIsDownloading.set(false);
                        for (RegisteredVisitor visitor : visitors)
                        {
                            for (VisitorLocation visitorLocation : visitorLocationLinkedList)
                            {
                                if (visitor.getUuid().equals(visitorLocation.getVisitorUuid()))
                                {
                                    map.put(visitor, visitorLocation);
                                    onVisitorLocationsFetched(map);
                                }
                            }
                        }
                    }
                })
                .setOnFailedListener(new Action1<IndoorwayTask.ProcessingException>()
                {
                    @Override
                    public void onAction(IndoorwayTask.ProcessingException e)
                    {
                        mIsDownloading.set(false);
                        onVisitorLocationsFetchFailed();
                    }
                }).execute();

    }

    private void onVisitorLocationsFetched(Map<RegisteredVisitor, VisitorLocation> visitorLocations)
    {
        for (SyncListener listener : mListeners)
        {
            listener.onSyncCompleted(visitorLocations);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return new VisitorBinder(this);
    }

    public void registerListener(SyncListener listener)
    {
        mListeners.add(listener);
    }

    public void unregisterListener(SyncListener listener)
    {
        mListeners.remove(listener);
    }
}
