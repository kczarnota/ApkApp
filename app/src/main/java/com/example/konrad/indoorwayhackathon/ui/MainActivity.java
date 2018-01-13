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
import com.indoorway.android.common.sdk.model.RegisteredVisitor;
import com.indoorway.android.common.sdk.model.Sex;
import com.indoorway.android.common.sdk.model.Visitor;
import com.indoorway.android.common.sdk.model.VisitorLocation;
import com.indoorway.android.fragments.sdk.map.IndoorwayMapFragment;
import com.indoorway.android.fragments.sdk.map.MapFragment;
import com.indoorway.android.map.sdk.view.drawable.figures.DrawableText;
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
                public void onSyncCompleted(Map<RegisteredVisitor, VisitorLocation> visitorLocations)
                {
                    if (currentMap == null)
                        return;

                    /*IndoorwaySdk.instance().visitors().list().setOnCompletedListener(new Action1<List<RegisteredVisitor>>()
                    {
                        @Override
                        public void onAction(List<RegisteredVisitor> registeredVisitors)
                        {
                            for (RegisteredVisitor vis : registeredVisitors) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.add(Calendar.MINUTE, -5);
                                Date fiveMinutesAgo = calendar.getTime();
                                if(getTimestamp() != null && !location.getTimestamp().before(fiveMinutesAgo))
                                    Log.d(TAG, "onAction: " + vis.getName());
                            }
                        }
                    }).execute();*/

                    for (Map.Entry<RegisteredVisitor, VisitorLocation> visitor: visitorLocations.entrySet())
                    {
                        VisitorLocation location = visitor.getValue();
                        RegisteredVisitor v = visitor.getKey();
                        Log.d(TAG, "onSyncCompleted: " + location.toString());
                        IndoorwayPosition position = location.getPosition();
                        if (position != null && position.getMapUuid().equals(currentMap.getMapUuid()))
                        {
                            visitorLayer.add(new DrawableText(location.getVisitorUuid(),
                                    position.getCoordinates(),
                                    v.getName(),
                                    2));
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
        mVisitor.setName("Jan");
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
