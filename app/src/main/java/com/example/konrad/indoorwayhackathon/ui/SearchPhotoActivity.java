package com.example.konrad.indoorwayhackathon.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.ThreadLocalRandom;

import com.example.konrad.indoorwayhackathon.R;
import com.example.konrad.indoorwayhackathon.Utils;
import com.example.konrad.indoorwayhackathon.net.Api;
import com.example.konrad.indoorwayhackathon.net.ApiService;
import com.example.konrad.indoorwayhackathon.model.Quest;
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
import com.indoorway.android.map.sdk.view.drawable.figures.DrawableIcon;
import com.indoorway.android.map.sdk.view.drawable.figures.DrawableText;
import com.indoorway.android.map.sdk.view.drawable.layers.MarkersLayer;
import com.indoorway.android.map.sdk.view.drawable.textures.BitmapTexture;
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
    @BindView(R.id.name)
    public TextView placeName;

    private Action1<IndoorwayProximityEvent> eventListenter;
    private MarkersLayer visitorLayer;
    private Handler handler;
    private Runnable runnable;
    private int randomNum;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_photo);
        ButterKnife.bind(this);

        setTitle("Quest");
        eventListenter = new Action1<IndoorwayProximityEvent>()
        {
            @Override
            public void onAction(IndoorwayProximityEvent indoorwayProximityEvent)
            {

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

                createDialog().show();
            }
        };

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

        randomNum = ThreadLocalRandom.current().nextInt(0, 2);
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
                        if (randomNum == 0)
                        {
                            String path = "http://192.168.100.194:8080/resources/image?name=" + quest.imageName;
                            Picasso.with(getApplicationContext()).load(path).into(mQuestIcon);
                        } else
                        {
                            mQuestIcon.setImageDrawable(getDrawable(R.drawable.q_mark));
                            placeName.setText(quest.name);
                        }
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
                    }

                    @Override
                    public void onFailure(Call<Quest> call, Throwable t)
                    {

                    }
                });

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.profile);
                visitorLayer.registerTexture(new BitmapTexture("me", bitmap));
            }
        });
    }

    @Override
    protected void onPause()
    {
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

    private Dialog createDialog()
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_collected_points)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                        startActivity(intent);
                    }
                });
        // Create the AlertDialog object and return it
        builder.setCancelable(false);
        return builder.create();
    }
}
