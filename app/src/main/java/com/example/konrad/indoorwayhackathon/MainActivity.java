package com.example.konrad.indoorwayhackathon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.indoorway.android.common.sdk.IndoorwaySdk;
import com.indoorway.android.common.sdk.listeners.generic.Action0;
import com.indoorway.android.common.sdk.listeners.generic.Action1;
import com.indoorway.android.common.sdk.model.IndoorwayBuilding;
import com.indoorway.android.common.sdk.model.Sex;
import com.indoorway.android.common.sdk.model.Visitor;
import com.indoorway.android.map.sdk.view.IndoorwayMapView;

import java.util.List;


public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private Visitor mVisitor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*        mVisitor = new Visitor();
        // optional: set more detailed informations if you have one
        mVisitor.setGroupUuid("<users group identifier>");   // user group
        mVisitor.setName("John Smith");                               // user name
        mVisitor.setAge(60);                                          // user age
        mVisitor.setSex(Sex.MALE);                          // user gender*/

       // IndoorwaySdk.instance().visitor().setup(mVisitor);
/*
        IndoorwaySdk.instance().buildings().setOnCompletedListener(new Action1<List<IndoorwayBuilding>>()
        {
            @Override
            public void onAction(List<IndoorwayBuilding> indoorwayBuildings)
            {
                for (IndoorwayBuilding b: indoorwayBuildings) {
                    //Log.d(Utils.getTag(TAG), b.getName());

                }
            }
        });
*/
        IndoorwayMapView indoorwayMapView = findViewById(R.id.mapView);
        indoorwayMapView
                // perform map loading using building UUID and map UUID
                .load(Utils.BUILDING_UUID, Utils.SECOND_FLOOR_UUID);
       /* indoorwayMapView
                // optional: assign callback for map loading failure
                .setOnMapLoadFailedListener(new Action0() {
                    @Override
                    public void onAction() {
                        // called on every map load error
                        Log.d(Utils.getTag(TAG), "onAction: map load failed");
                    }
                });

*/
    }
}
