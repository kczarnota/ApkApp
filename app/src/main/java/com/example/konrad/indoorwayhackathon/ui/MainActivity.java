package com.example.konrad.indoorwayhackathon.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.konrad.indoorwayhackathon.R;
import com.example.konrad.indoorwayhackathon.Utils;
import com.example.konrad.indoorwayhackathon.service.SyncListener;
import com.example.konrad.indoorwayhackathon.service.VisitorBinder;
import com.example.konrad.indoorwayhackathon.service.VisitorSyncService;
import com.indoorway.android.common.sdk.IndoorwaySdk;
import com.indoorway.android.common.sdk.listeners.generic.Action1;
import com.indoorway.android.common.sdk.model.IndoorwayMap;
import com.indoorway.android.common.sdk.model.IndoorwayPosition;
import com.indoorway.android.common.sdk.model.Sex;
import com.indoorway.android.common.sdk.model.Visitor;
import com.indoorway.android.common.sdk.model.VisitorLocation;
import com.indoorway.android.fragments.sdk.map.IndoorwayMapFragment;
import com.indoorway.android.fragments.sdk.map.MapFragment;
import com.indoorway.android.map.sdk.view.drawable.figures.DrawableIcon;
import com.indoorway.android.map.sdk.view.drawable.layers.MarkersLayer;

import java.util.Map;


public class MainActivity extends AppCompatActivity implements IndoorwayMapFragment.OnMapFragmentReadyListener
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private Visitor mVisitor;
    MarkersLayer visitorLayer;
    private VisitorSyncService syncVisitorSeviceHandle;
    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            VisitorBinder binder = (VisitorBinder) iBinder;
            syncVisitorSeviceHandle = binder.getService();
            syncVisitorSeviceHandle.registerListener(new SyncListener()
            {
                @Override
                public void onSyncCompleted(Map<String, VisitorLocation> visitorLocations)
                {
                    if (currentMap == null)
                        return;

                    Log.d(TAG, "onSyncCompleted: map is not null");
                    for (VisitorLocation location : visitorLocations.values())
                    {
                        IndoorwayPosition position = location.getPosition();
                        if (position != null && position.getMapUuid().equals(currentMap.getMapUuid()))
                        {
                            visitorLayer.add(new DrawableIcon(
                                    location.getVisitorUuid(),
                                    location.getVisitorUuid(),
                                    position.getCoordinates(),
                                    2f,
                                    2f));
                        }
                        else
                        {
                            visitorLayer.remove(location.getVisitorUuid());
                        }
                    }
                }

                @Override
                public void onSyncError()
                {

                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {

        }
    };
    private IndoorwayMap currentMap;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVisitor = new Visitor();
        // optional: set more detailed informations if you have one
        mVisitor.setName("John Smith");
        mVisitor.setAge(60);
        mVisitor.setSex(Sex.MALE);
        mVisitor.setShareLocation(true);
        IndoorwaySdk.instance().visitor().setup(mVisitor);
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        Intent intent = new Intent(this, VisitorSyncService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop()
    {
        unbindService(serviceConnection);
        super.onStop();
    }

    @Override
    public void onMapFragmentReady(MapFragment mapFragment)
    {
        mapFragment.getMapView().load(Utils.BUILDING_UUID, Utils.SECOND_FLOOR_UUID);
        mapFragment.getMapView().setOnMapLoadCompletedListener(new Action1<IndoorwayMap>()
        {
            @Override
            public void onAction(IndoorwayMap indoorwayMap)
            {
                currentMap = indoorwayMap;
                if(currentMap != null) {
                    Log.d(TAG, "onMapFragmentReady: not null");
                }
            }
        });

        this.visitorLayer = mapFragment.getMapView().getMarker().addLayer(12f);
    }
}
