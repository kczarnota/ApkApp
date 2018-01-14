package com.example.konrad.indoorwayhackathon.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.Arrays;
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
                List<IndoorwayObjectParameters> objects = indoorwayMap.getObjects();
                items = new ArrayList<IndoorwayObjectParameters>();
                items.addAll(objects);
                adapter = new QuickNavItemsListAdapter(QuickNavigationActivity.this, R.layout.pritip_list_item, items);
                quickNavTargesList.setAdapter(adapter);
            }
        }).execute();
    }

    private void startNavigation(String targetId) {
        Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
        intent.putExtra("target", targetId);
        startActivity(intent);
    }
}
