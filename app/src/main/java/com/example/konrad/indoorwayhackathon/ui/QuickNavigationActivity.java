package com.example.konrad.indoorwayhackathon.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.konrad.indoorwayhackathon.R;
import com.example.konrad.indoorwayhackathon.Utils;
import com.indoorway.android.common.sdk.IndoorwaySdk;
import com.indoorway.android.common.sdk.listeners.generic.Action1;
import com.indoorway.android.common.sdk.model.IndoorwayMap;
import com.indoorway.android.common.sdk.model.IndoorwayObjectParameters;
import com.indoorway.android.common.sdk.model.IndoorwayPosition;
import com.indoorway.android.location.sdk.IndoorwayLocationSdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QuickNavigationActivity extends AppCompatActivity {
    private static final String TAG = MapActivity.class.getSimpleName();

    @BindView(R.id.quick_actions_list)
    ListView quickNavTargesList;
    private ArrayAdapter<IndoorwayObjectParameters> adapter;
    private ArrayList<IndoorwayObjectParameters> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_navigation);
        ButterKnife.bind(this);

        items = new ArrayList<>();
        adapter = new QuickNavItemsListAdapter(QuickNavigationActivity.this, R.layout.distance_list_item, items);
        quickNavTargesList.setAdapter(adapter);

        quickNavTargesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IndoorwayObjectParameters objectParameters = items.get(position);
                startNavigation(objectParameters.getId());
            }
        });

        IndoorwaySdk.instance().map().details(Utils.BUILDING_UUID, Utils.SECOND_FLOOR_UUID).setOnCompletedListener(new Action1<IndoorwayMap>() {
            @Override
            public void onAction(IndoorwayMap indoorwayMap) {
                final List<IndoorwayObjectParameters> objects = indoorwayMap.getObjects();
                IndoorwayLocationSdk.instance().position().onChange().register(new Action1<IndoorwayPosition>() {
                    @Override
                    public void onAction(final IndoorwayPosition indoorwayPosition) {
                        Collections.sort(objects, new Comparator<IndoorwayObjectParameters>() {
                            @Override
                            public int compare(IndoorwayObjectParameters o1, IndoorwayObjectParameters o2) {
                                double diff = o1.getCenterPoint().getDistanceTo(indoorwayPosition.getCoordinates()) - o2.getCenterPoint().getDistanceTo(indoorwayPosition.getCoordinates());
                                if (diff < 0) {
                                    return -1;
                                } else if (diff > 0) {
                                    return 1;
                                } else return 0;
                            }
                        });
                        items.clear();
                        items.addAll(objects);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).execute();
    }

    private void startNavigation(String targetId) {
        Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
        intent.putExtra("target", targetId);
        startActivity(intent);
    }
}
