package com.example.konrad.indoorwayhackathon.ui;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.konrad.indoorwayhackathon.R;
import com.example.konrad.indoorwayhackathon.Utils;
import com.example.konrad.indoorwayhackathon.net.login.Api;
import com.example.konrad.indoorwayhackathon.net.login.ApiService;
import com.example.konrad.indoorwayhackathon.net.login.Quest;
import com.indoorway.android.common.sdk.listeners.generic.Action1;
import com.indoorway.android.common.sdk.model.Coordinates;
import com.indoorway.android.common.sdk.model.IndoorwayMap;
import com.indoorway.android.common.sdk.model.IndoorwayPosition;
import com.indoorway.android.common.sdk.model.proximity.IndoorwayNotificationInfo;
import com.indoorway.android.common.sdk.model.proximity.IndoorwayProximityEvent;
import com.indoorway.android.common.sdk.model.proximity.IndoorwayProximityEventShape;
import com.indoorway.android.fragments.sdk.map.IndoorwayMapFragment;
import com.indoorway.android.fragments.sdk.map.MapFragment;
import com.indoorway.android.location.sdk.IndoorwayLocationSdk;
import com.indoorway.android.map.sdk.view.drawable.figures.DrawableText;
import com.indoorway.android.map.sdk.view.drawable.layers.MarkersLayer;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchPhotoActivity extends AppCompatActivity implements IndoorwayMapFragment.OnMapFragmentReadyListener
{
    @BindView(R.id.reward)
    public TextView mRewardTextView;
    @BindView(R.id.quest_icon)
    public ImageView mQuestIcon;

    private Action1<IndoorwayProximityEvent> eventListenter;
    private MarkersLayer visitorLayer;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_photo);
        ButterKnife.bind(this);

        Button btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                IndoorwayPosition pos = IndoorwayLocationSdk.instance().position().latest();
                String text;
                if (pos != null) {
                    Coordinates cor = pos.getCoordinates();
                    text = cor.getLatitude() + " " + cor.getLongitude();
                } else {
                    text = "position null";
                }
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });



        eventListenter = new Action1<IndoorwayProximityEvent>() {
            @Override
            public void onAction(IndoorwayProximityEvent indoorwayProximityEvent) {
                Toast.makeText(SearchPhotoActivity.this, "item collected", Toast.LENGTH_SHORT).show();
                visitorLayer.remove(indoorwayProximityEvent.getIdentifier());
                IndoorwayLocationSdk.instance().customProximityEvents().remove(indoorwayProximityEvent.getIdentifier());
                ApiService api = Api.getApi();
                Map<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + Utils.getToken());
                api.postAdditionalPoints(map, mRewardTextView.getText().toString()).enqueue(new Callback<Void>()
                {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response)
                    {

                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t)
                    {

                    }
                });
            }
        };

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                try {IndoorwayPosition pos = IndoorwayLocationSdk.instance().position().latest();
                    String text;
                    if (pos != null) {
                        Coordinates cor = pos.getCoordinates();
                        visitorLayer.add(new DrawableText("Me",
                                cor,
                                "me",
                                2));
                    }
                } catch (Exception e) {
                } finally {
                    handler.postDelayed(runnable, 100);
                }
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onMapFragmentReady(MapFragment mapFragment)
    {
        mapFragment.getMapView().load(Utils.BUILDING_UUID, Utils.SECOND_FLOOR_UUID);
        this.visitorLayer = mapFragment.getMapView().getMarker().addLayer(12f);

        mapFragment.getMapView().setOnMapLoadCompletedListener(new Action1<IndoorwayMap>()
        {
            @Override
            public void onAction(IndoorwayMap indoorwayMap)
            {
                ApiService apiService = Api.getApi();
                Map<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + Utils.getToken());
                apiService.getQuest(map).enqueue(new Callback<Quest>()
                {
                    @Override
                    public void onResponse(Call<Quest> call, Response<Quest> response)
                    {
                        Quest quest = response.body();
                        String path = "http://192.168.100.194:8080/resources/image?name=" + quest.imageName;
                        Picasso.with(getApplicationContext()).load(path).into(mQuestIcon);
                        mRewardTextView.setText(String.valueOf(quest.value));

                        IndoorwayLocationSdk.instance().customProximityEvents()
                                .add(new IndoorwayProximityEvent(
                                        quest.name, // identifier
                                        IndoorwayProximityEvent.Trigger.ENTER, // trigger on enter or on exit?
                                        new IndoorwayProximityEventShape.Circle(
                                                new Coordinates(quest.localization.latitude, quest.localization.longitude),
                                                3.0
                                        ),
                                        Utils.BUILDING_UUID, // building identifier
                                        Utils.SECOND_FLOOR_UUID, // map identifier
                                        0L, // (optional) timeout to show notification, will be passed as parapeter to listener
                                        new IndoorwayNotificationInfo("title", "description", "url", "image")
                                ));

                        visitorLayer.add(new DrawableText(quest.name,
                                new Coordinates(quest.localization.latitude, quest.localization.longitude),
                                quest.name,
                                2));
                        Toast.makeText(SearchPhotoActivity.this, "putting item in" +
                                quest.localization.latitude + " " + quest.localization.longitude, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<Quest> call, Throwable t)
                    {

                    }
                });
            }
        });
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
    protected void onResume()
    {
        super.onResume();
        IndoorwayLocationSdk.instance()
                .customProximityEvents()
                .onEvent()
                .register(eventListenter);
    }
}
