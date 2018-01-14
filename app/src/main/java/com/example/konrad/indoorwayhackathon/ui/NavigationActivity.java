package com.example.konrad.indoorwayhackathon.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.konrad.indoorwayhackathon.R;
import com.example.konrad.indoorwayhackathon.Utils;
import com.indoorway.android.common.sdk.listeners.generic.Action1;
import com.indoorway.android.common.sdk.model.Coordinates;
import com.indoorway.android.common.sdk.model.IndoorwayMap;
import com.indoorway.android.common.sdk.model.IndoorwayPosition;
import com.indoorway.android.fragments.sdk.map.IndoorwayMapFragment;
import com.indoorway.android.fragments.sdk.map.MapFragment;
import com.indoorway.android.location.sdk.IndoorwayLocationSdk;
import com.indoorway.android.map.sdk.view.drawable.figures.DrawableIcon;
import com.indoorway.android.map.sdk.view.drawable.layers.MarkersLayer;
import com.indoorway.android.map.sdk.view.drawable.textures.BitmapTexture;

public class NavigationActivity extends AppCompatActivity implements IndoorwayMapFragment.OnMapFragmentReadyListener
{
    private String targetId;
    private Handler handler;
    private Runnable runnable;
    private MarkersLayer visitorLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        this.targetId = this.getIntent().getStringExtra("target");
        setTitle("Navigation to point");

        handler = new Handler();
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    IndoorwayPosition pos = IndoorwayLocationSdk.instance().position().latest();
                    if (pos != null)
                    {
                        Coordinates cor = pos.getCoordinates();
                        visitorLayer.add(
                                new DrawableIcon(
                                        "me1",   // icon identifier
                                        "me", // texture identifier
                                        cor,
                                        3f,  // icon size vertically
                                        3f   // icon size horizontally
                                )
                        );
                    }
                } catch (Exception e)
                {
                } finally
                {
                    handler.postDelayed(runnable, 100);
                }
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onMapFragmentReady(final MapFragment mapFragment)
    {
        mapFragment.getMapView().load(Utils.BUILDING_UUID, Utils.SECOND_FLOOR_UUID);
        this.visitorLayer = mapFragment.getMapView().getMarker().addLayer(12f);
        mapFragment.getMapView().setOnMapLoadCompletedListener(new Action1<IndoorwayMap>()
        {
            @Override
            public void onAction(IndoorwayMap indoorwayMap)
            {
                IndoorwayLocationSdk.instance().position().onChange().register(new Action1<IndoorwayPosition>()
                {
                    @Override
                    public void onAction(IndoorwayPosition indoorwayPosition)
                    {
                        mapFragment.getMapView().getNavigation().start(indoorwayPosition.getCoordinates(), targetId);
                    }
                });

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.profile);
                visitorLayer.registerTexture(new BitmapTexture("me", bitmap));
            }
        });
    }
}
