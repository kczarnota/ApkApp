package com.example.konrad.indoorwayhackathon.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.konrad.indoorwayhackathon.R;
import com.example.konrad.indoorwayhackathon.Utils;
import com.example.konrad.indoorwayhackathon.net.login.Api;
import com.example.konrad.indoorwayhackathon.net.login.ApiService;
import com.example.konrad.indoorwayhackathon.net.login.Item;
import com.example.konrad.indoorwayhackathon.net.login.ItemsList;
import com.example.konrad.indoorwayhackathon.net.login.Quest;
import com.example.konrad.indoorwayhackathon.service.SyncListener;
import com.example.konrad.indoorwayhackathon.service.VisitorBinder;
import com.example.konrad.indoorwayhackathon.service.VisitorSyncService;
import com.indoorway.android.common.sdk.IndoorwaySdk;
import com.indoorway.android.common.sdk.listeners.generic.Action1;
import com.indoorway.android.common.sdk.model.Coordinates;
import com.indoorway.android.common.sdk.model.IndoorwayMap;
import com.indoorway.android.common.sdk.model.IndoorwayObjectParameters;
import com.indoorway.android.common.sdk.model.IndoorwayPosition;
import com.indoorway.android.common.sdk.model.RegisteredVisitor;
import com.indoorway.android.common.sdk.model.Visitor;
import com.indoorway.android.common.sdk.model.VisitorLocation;
import com.indoorway.android.common.sdk.model.proximity.IndoorwayNotificationInfo;
import com.indoorway.android.common.sdk.model.proximity.IndoorwayProximityEvent;
import com.indoorway.android.common.sdk.model.proximity.IndoorwayProximityEventShape;
import com.indoorway.android.fragments.sdk.map.IndoorwayMapFragment;
import com.indoorway.android.fragments.sdk.map.MapFragment;
import com.indoorway.android.location.sdk.IndoorwayLocationSdk;
import com.indoorway.android.map.sdk.view.drawable.figures.DrawableText;
import com.indoorway.android.map.sdk.view.drawable.layers.MarkersLayer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapActivity extends AppCompatActivity implements IndoorwayMapFragment.OnMapFragmentReadyListener {
    private static final String TAG = MapActivity.class.getSimpleName();

    private IndoorwayMap currentMap;
    @BindView(R.id.add_event_button)
    Button addEventButton;
    @BindView(R.id.fetch_items_button)
    Button fetchItemsButton;
    @BindView(R.id.get_current_position_button)
    Button getLongLatButton;
    @BindView(R.id.current_coins)
    TextView currentCoins;
    private LinkedList<String> renderedVisitors;

    MarkersLayer visitorLayer;
    private VisitorSyncService syncVisitorSeviceHandle;
    private Action1<IndoorwayProximityEvent> eventListenter;
    private double mLat;
    private double mLon;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            VisitorBinder binder = (VisitorBinder) iBinder;
            syncVisitorSeviceHandle = binder.getService();
            syncVisitorSeviceHandle.registerListener(new SyncListener() {
                @Override
                public void onSyncCompleted(Map<RegisteredVisitor, VisitorLocation> visitorLocations) {
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

                    for (Map.Entry<RegisteredVisitor, VisitorLocation> visitor : visitorLocations.entrySet()) {
                        VisitorLocation location = visitor.getValue();
                        RegisteredVisitor v = visitor.getKey();
                        Log.d(TAG, "onSyncCompleted: " + location.toString());
                        IndoorwayPosition position = location.getPosition();
                        if (position != null && position.getMapUuid().equals(currentMap.getMapUuid())) {
                            renderedVisitors.add(location.getVisitorUuid());
                            visitorLayer.add(new DrawableText(location.getVisitorUuid(),
                                    position.getCoordinates(),
                                    v.getName(),
                                    2));
                        }
                    }
                }

                @Override
                public void onSyncError() {

                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        this.renderedVisitors = new LinkedList<>();

        fetchItemsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ApiService apiService = Api.getApi();
                Map<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + Utils.getToken());
                apiService.getItems(map).enqueue(new Callback<ItemsList>() {
                    @Override
                    public void onResponse(Call<ItemsList> call, Response<ItemsList> response) {
                        List<Item> items = response.body().list;
                        for (Item i : items) {
                            IndoorwayLocationSdk.instance().customProximityEvents()
                                    .add(new IndoorwayProximityEvent(
                                            i.name, // identifier
                                            IndoorwayProximityEvent.Trigger.ENTER, // trigger on enter or on exit?
                                            new IndoorwayProximityEventShape.Circle(
                                                    new Coordinates(i.localization.latitude,
                                                            i.localization.longitude),
                                                    3.0
                                            ),
                                            Utils.BUILDING_UUID, // building identifier
                                            Utils.SECOND_FLOOR_UUID, // map identifier
                                            0L, // (optional) timeout to show notification, will be passed as parapeter to listener
                                            new IndoorwayNotificationInfo("title", "description", "url", "image") // (optional) data to show in notification
                                    ));
                            visitorLayer.add(new DrawableText(i.name,
                                    new Coordinates(i.localization.latitude, i.localization.longitude),
                                    i.name,
                                    2));
                        }
                    }

                    @Override
                    public void onFailure(Call<ItemsList> call, Throwable t) {

                    }
                });
            }
        });

        getLongLatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double lat = IndoorwayLocationSdk.instance().position().latest().getCoordinates().getLatitude();
                double lon = IndoorwayLocationSdk.instance().position().latest().getCoordinates().getLongitude();
                Toast.makeText(syncVisitorSeviceHandle, lat + " " + lon, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onClick: " + lat + " " + lon);
                mLat = lat;
                mLon = lon;

                IndoorwaySdk.instance().map().details(Utils.BUILDING_UUID, Utils.SECOND_FLOOR_UUID)
                        .setOnCompletedListener(new Action1<IndoorwayMap>() {
                            @Override
                            public void onAction(IndoorwayMap indoorwayMap) {
                                // handle map objects
                                List<IndoorwayObjectParameters> objects = indoorwayMap.getObjects();

                                for (IndoorwayObjectParameters o : objects) {
                                    Log.d(TAG, "onAction: " + o.getName()
                                            + "lat " + o.getCenterPoint().getLatitude()
                                            + "lon " + o.getCenterPoint().getLongitude());
                                }
                            }
                        }).execute();
            }
        });

        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IndoorwayLocationSdk.instance().customProximityEvents()
                        .add(new IndoorwayProximityEvent(
                                "proximity-event-id", // identifier
                                IndoorwayProximityEvent.Trigger.ENTER, // trigger on enter or on exit?
                                new IndoorwayProximityEventShape.Circle(
                                        new Coordinates(52.22234245, 21.00675825),
                                        3.0
                                ),
                                Utils.BUILDING_UUID, // building identifier
                                Utils.SECOND_FLOOR_UUID, // map identifier
                                0L, // (optional) timeout to show notification, will be passed as parapeter to listener
                                new IndoorwayNotificationInfo("title", "description", "url", "image") // (optional) data to show in notification
                        ));
                visitorLayer.add(new DrawableText("raz",
                        new Coordinates(52.22234245, 21.00675825),
                        "Kitchen",
                        2));
            }
        });

        eventListenter = new Action1<IndoorwayProximityEvent>() {
            @Override
            public void onAction(IndoorwayProximityEvent indoorwayProximityEvent) {
                Toast.makeText(syncVisitorSeviceHandle, "action", Toast.LENGTH_SHORT).show();
                IndoorwayLocationSdk.instance().customProximityEvents().remove(indoorwayProximityEvent.getIdentifier());
                visitorLayer.remove(indoorwayProximityEvent.getIdentifier());
            }
        };

    }

    @Override
    protected void onPause() {
        super.onPause();
        IndoorwayLocationSdk.instance()
                .customProximityEvents()
                .onEvent()
                .unregister(eventListenter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApiService service = Api.getApi();
        Map<String, String> map = new HashMap<>();
        map.put("Authorization", "Bearer " + Utils.getToken());
        service.getCoins(map).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                currentCoins.setText(String.valueOf(response.body()));
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {

            }
        });
        IndoorwayLocationSdk.instance()
                .customProximityEvents()
                .onEvent()
                .register(eventListenter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, VisitorSyncService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        unbindService(serviceConnection);
        super.onStop();
    }

    @Override
    public void onMapFragmentReady(MapFragment mapFragment) {
        mapFragment.getMapView().load(Utils.BUILDING_UUID, Utils.SECOND_FLOOR_UUID);
        mapFragment.getMapView().setOnMapLoadCompletedListener(new Action1<IndoorwayMap>() {
            @Override
            public void onAction(IndoorwayMap indoorwayMap) {
                currentMap = indoorwayMap;
                if (currentMap != null) {
                    Log.d(TAG, "onMapFragmentReady: not null");
                }
            }
        });

        this.visitorLayer = mapFragment.getMapView().getMarker().addLayer(12f);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.quests_menu_item:
                startQuestsActivity();
                return true;
            case R.id.pritip_activity_menu:
                startProtipActivity();
                return true;
            case R.id.quict_navigation_menu_item:
                startQuickNavigation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startQuickNavigation() {
        Intent intent = new Intent(this, QuickNavigationActivity.class);
        startActivity(intent);
    }

    private void startProtipActivity() {
        Intent intent = new Intent(this, ProtipsActivity.class);
        startActivity(intent);
    }

    private void startQuestsActivity() {
        Intent intent = new Intent(this, SearchPhotoActivity.class);
        startActivity(intent);
    }
}

