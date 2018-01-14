package com.example.konrad.indoorwayhackathon.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.konrad.indoorwayhackathon.R;
import com.example.konrad.indoorwayhackathon.Utils;
import com.indoorway.android.common.sdk.IndoorwaySdk;
import com.indoorway.android.common.sdk.listeners.generic.Action1;
import com.indoorway.android.common.sdk.model.Coordinates;
import com.indoorway.android.common.sdk.model.IndoorwayMap;
import com.indoorway.android.common.sdk.model.IndoorwayObjectParameters;
import com.indoorway.android.location.sdk.IndoorwayLocationSdk;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class QuickActions extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_actions);
    }

    public void categoryClick(View view)
    {
        IndoorwaySdk.instance().map().details(Utils.BUILDING_UUID, Utils.SECOND_FLOOR_UUID).setOnCompletedListener(new Action1<IndoorwayMap>()
        {
            @Override
            public void onAction(IndoorwayMap indoorwayMap)
            {
                List<IndoorwayObjectParameters> objects = indoorwayMap.getObjects();
                // Hete we should filter places by their tags, but right now they are set all the same
                Collections.shuffle(objects);
                objects = objects.subList(0, ThreadLocalRandom.current().nextInt(0, objects.size()));
                final Coordinates cor = IndoorwayLocationSdk.instance().position().latest().getCoordinates();
                Collections.sort(objects, new Comparator<IndoorwayObjectParameters>()
                {
                    @Override
                    public int compare(IndoorwayObjectParameters o1, IndoorwayObjectParameters o2)
                    {
                        double diff = o1.getCenterPoint().getDistanceTo(cor) - o2.getCenterPoint().getDistanceTo(cor);
                        if (diff < 0)
                        {
                            return -1;
                        } else if (diff > 0)
                        {
                            return 1;
                        } else return 0;
                    }
                });

                startNavigation(objects.get(0).getId());
            }
        }).execute();
    }

    private void startNavigation(String targetId)
    {
        Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
        intent.putExtra("target", targetId);
        startActivity(intent);
    }
}
