package com.example.konrad.indoorwayhackathon.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.konrad.indoorwayhackathon.R;
import com.example.konrad.indoorwayhackathon.Utils;
import com.indoorway.android.common.sdk.IndoorwaySdk;
import com.indoorway.android.common.sdk.listeners.generic.Action1;
import com.indoorway.android.common.sdk.model.IndoorwayMap;
import com.indoorway.android.common.sdk.model.IndoorwayPosition;
import com.indoorway.android.fragments.sdk.map.IndoorwayMapFragment;
import com.indoorway.android.fragments.sdk.map.MapFragment;
import com.indoorway.android.location.sdk.IndoorwayLocationSdk;

public class NavigationActivity extends AppCompatActivity implements IndoorwayMapFragment.OnMapFragmentReadyListener {
    public IndoorwayMap currentMap;
    private String targetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        this.targetId = this.getIntent().getStringExtra("target");
    }

    @Override
    public void onMapFragmentReady(final MapFragment mapFragment) {
        mapFragment.getMapView().load(Utils.BUILDING_UUID, Utils.SECOND_FLOOR_UUID);
        mapFragment.getMapView().setOnMapLoadCompletedListener(new Action1<IndoorwayMap>() {
            @Override
            public void onAction(IndoorwayMap indoorwayMap) {
                IndoorwayLocationSdk.instance().position().onChange().register(new Action1<IndoorwayPosition>() {
                    @Override
                    public void onAction(IndoorwayPosition indoorwayPosition) {
                        mapFragment.getMapView().getNavigation().start(indoorwayPosition.getCoordinates(), targetId);
                    }
                });
            }
        });
    }
}
