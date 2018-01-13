package com.example.konrad.indoorwayhackathon.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.indoorway.android.common.sdk.IndoorwaySdk;
import com.indoorway.android.common.sdk.listeners.generic.Action1;
import com.indoorway.android.common.sdk.model.VisitorLocation;
import com.indoorway.android.common.sdk.task.IndoorwayTask;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

public class VisitorSyncService extends Service
{
    Set<SyncListener> listeners = new CopyOnWriteArraySet<>();
    AtomicBoolean isRunning = new AtomicBoolean(false);
    AtomicBoolean isDownloading = new AtomicBoolean(false);
    Thread workerThread;
    Handler handler;
    public static final int SYNC_FREQUENCY_MS = 3000;

    public VisitorSyncService()
    {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning.set(true);
        handler = new Handler(Looper.getMainLooper());
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning.get()) {
                    try {
                        // polling locations
                        fetchLocations();
                        Thread.sleep(SYNC_FREQUENCY_MS);
                    } catch (InterruptedException e) {
                        // exit
                    }
                }
            }
        });
        workerThread.start();
    }

    private void fetchLocations() {
        if (isDownloading.compareAndSet(false, true)) {
            IndoorwaySdk.instance()
                    .visitors()
                    .locations()
                    .setOnCompletedListener(new Action1<List<VisitorLocation>>() {
                        @Override
                        public void onAction(List<VisitorLocation> visitorLocations) {
                            isDownloading.set(false);
                            onVisitorLocationsFetched(visitorLocations);
                        }
                    })
                    .setOnFailedListener(new Action1<IndoorwayTask.ProcessingException>() {
                        @Override
                        public void onAction(IndoorwayTask.ProcessingException e) {
                            isDownloading.set(false);
                            onVisitorLocationsFetchFailed();
                        }
                    }).execute();
        }
    }

    private void onVisitorLocationsFetchFailed()
    {
    }

    private void onVisitorLocationsFetched(List<VisitorLocation> visitorLocations)
    {
        Map<String, VisitorLocation> map = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -5);
        Date fiveMinutesAgo = calendar.getTime();
        for (VisitorLocation location : visitorLocations) {
            if (location.getTimestamp() != null && !location.getTimestamp().before(fiveMinutesAgo))
                map.put(location.getVisitorUuid(), location);
        }

        for (SyncListener listener : listeners) {
            listener.onSyncCompleted(map);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return new VisitorBinder(this);
    }

    public void registerListener(SyncListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(SyncListener listener) {
        listeners.remove(listener);
    }
}
