package com.example.konrad.indoorwayhackathon.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.konrad.indoorwayhackathon.R;
import com.example.konrad.indoorwayhackathon.Utils;
import com.example.konrad.indoorwayhackathon.net.Api;
import com.example.konrad.indoorwayhackathon.net.ApiService;
import com.example.konrad.indoorwayhackathon.model.Item;
import com.example.konrad.indoorwayhackathon.model.ItemsList;
import com.example.konrad.indoorwayhackathon.model.Subject;
import com.example.konrad.indoorwayhackathon.model.Subjects;
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
import com.indoorway.android.map.sdk.view.drawable.figures.DrawableCircle;
import com.indoorway.android.map.sdk.view.drawable.figures.DrawableIcon;
import com.indoorway.android.map.sdk.view.drawable.figures.DrawableText;
import com.indoorway.android.map.sdk.view.drawable.layers.MarkersLayer;
import com.indoorway.android.map.sdk.view.drawable.textures.BitmapTexture;

import org.billthefarmer.mididriver.MidiDriver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapActivity extends AppCompatActivity implements IndoorwayMapFragment.OnMapFragmentReadyListener
{
    private static final String TAG = MapActivity.class.getSimpleName();

    @BindView(R.id.current_coins)
    TextView mCurrentCoins;
    private IndoorwayMap mCurrentMap;
    private Map<String, Integer> mRewards;

    MarkersLayer mVisitorLayer;
    private Action1<IndoorwayProximityEvent> mEventListenter;

    private Handler handler;
    private Runnable mRunnable;
    private MidiDriver mMidiDriver;
    private Map<String, Action1<IndoorwayPosition>> mRegistredHiddenPointsAlarms;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        setTitle("Map");
        mMidiDriver = new MidiDriver();

        handler = new Handler();
        mRunnable = new Runnable()
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
                        mVisitorLayer.add(
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
                    handler.postDelayed(mRunnable, 100);
                }
            }
        };
        handler.post(mRunnable);

        mEventListenter = new Action1<IndoorwayProximityEvent>()
        {
            @Override
            public void onAction(IndoorwayProximityEvent indoorwayProximityEvent)
            {
                int reward = mRewards.get(indoorwayProximityEvent.getIdentifier());
                Action1<IndoorwayPosition> positionAction1 = mRegistredHiddenPointsAlarms.get(indoorwayProximityEvent.getIdentifier());
                createDialog(reward).show();
                IndoorwayLocationSdk.instance().position().onChange().unregister(positionAction1);
                IndoorwayLocationSdk.instance().customProximityEvents().remove(indoorwayProximityEvent.getIdentifier());
                int cur = Integer.valueOf(mCurrentCoins.getText().toString());
                cur += reward;
                mCurrentCoins.setText(String.valueOf(cur));
                ApiService api = Api.getApi();
                Map<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + Utils.getToken());
                api.postAdditionalPoints(map, String.valueOf(cur)).enqueue(new Callback<Void>()
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

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        IndoorwayLocationSdk.instance()
                .customProximityEvents()
                .onEvent()
                .unregister(mEventListenter);
        mMidiDriver.stop();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        ApiService service = Api.getApi();
        Map<String, String> map = new HashMap<>();
        map.put("Authorization", "Bearer " + Utils.getToken());
        service.getCoins(map).enqueue(new Callback<Integer>()
        {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response)
            {
                mCurrentCoins.setText(String.valueOf(response.body()));
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t)
            {

            }
        });
        IndoorwayLocationSdk.instance()
                .customProximityEvents()
                .onEvent()
                .register(mEventListenter);
        mMidiDriver.start();
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
                mCurrentMap = indoorwayMap;
                if (mCurrentMap != null)
                {
                    Log.d(TAG, "onMapFragmentReady: not null");
                }
                prepareHiddenPoints();
                prepareSubjects();
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.profile);
                mVisitorLayer.registerTexture(new BitmapTexture("me", bitmap));
            }
        });

        this.mVisitorLayer = mapFragment.getMapView().getMarker().addLayer(12f);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.quests_menu_item:
                startQuestsActivity();
                return true;
            case R.id.pritip_activity_menu:
                startProtipActivity();
                return true;
            case R.id.quict_navigation_menu_item:
                startQuickNavigation();
                return true;
            case R.id.quict_action_menu_item:
                startActionsActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startQuickNavigation()
    {
        Intent intent = new Intent(this, QuickNavigationActivity.class);
        startActivity(intent);
    }

    private void startProtipActivity()
    {
        Intent intent = new Intent(this, ProtipsActivity.class);
        startActivity(intent);
    }

    private void startQuestsActivity()
    {
        for (String k : mRewards.keySet())
        {
            IndoorwayLocationSdk.instance().customProximityEvents().remove(k);
        }
        Intent intent = new Intent(this, SearchPhotoActivity.class);
        startActivity(intent);
    }

    private void startActionsActivity()
    {
        Intent intent = new Intent(this, QuickActions.class);
        startActivity(intent);
    }

    void playNoteDuringTime(final byte pitch, final long miliseconds)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                for (int i = 0; i < miliseconds; i++)
                {
                    byte[] playNote;
                    playNote = new byte[3];
                    playNote[0] = (byte) (0x90 | 0x00);  // 0x90 = note On, 0x00 = channel 1
                    playNote[1] = (byte) pitch;  // 0x3C = middle C
                    playNote[2] = (byte) 0x7F;  // 0x7F = the maximum velocity (127)
                    mMidiDriver.queueEvent(playNote);
                    try
                    {
                        Thread.sleep(10);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    byte[] stopNote = new byte[3];
                    stopNote[0] = (byte) (0x80 | 0x00);  // 0x80 = note Off, 0x00 = channel 1
                    stopNote[1] = (byte) pitch;  // 0x3C = middle C
                    stopNote[2] = (byte) 0x00;  // 0x00 = the minimum velocity (0)
                    mMidiDriver.queueEvent(stopNote);
                }
            }
        }).start();
    }

    private void prepareHiddenPoints()
    {
        ApiService apiService = Api.getApi();
        Map<String, String> map = new HashMap<>();
        map.put("Authorization", "Bearer " + Utils.getToken());
        apiService.getItems(map).enqueue(new Callback<ItemsList>()
        {
            @Override
            public void onResponse(Call<ItemsList> call, Response<ItemsList> response)
            {
                mRewards = new HashMap<>();
                List<Item> items = response.body().list;

                // Send the MIDI event to the synthesizer.
                mRegistredHiddenPointsAlarms = new HashMap<>();

                for (final Item i : items)
                {
                    Action1<IndoorwayPosition> action1 = new Action1<IndoorwayPosition>()
                    {
                        @Override
                        public void onAction(IndoorwayPosition indoorwayPosition)
                        {
                            double distance = indoorwayPosition.getCoordinates().getDistanceTo(new Coordinates(i.localization.latitude, i.localization.longitude));
                            if (distance < 10)
                            {
                                playNoteDuringTime((byte) 0x54, (long) (10 - distance) * 10);
                                return;
                            }

                        }
                    };
                    mRegistredHiddenPointsAlarms.put(i.name, action1);
                    IndoorwayLocationSdk.instance().position().onChange().register(action1);
                    mRewards.put(i.name, i.value);
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
                }
            }

            @Override
            public void onFailure(Call<ItemsList> call, Throwable t)
            {

            }
        });
    }

    private Dialog createDialog(int coins)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Congratulations! You have found " + coins + " hidden coins!")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                    }
                });
        // Create the AlertDialog object and return it
        builder.setCancelable(false);
        return builder.create();
    }

    private void prepareSubjects()
    {
        ApiService apiService = Api.getApi();
        Map<String, String> map = new HashMap<>();
        map.put("Authorization", "Bearer " + Utils.getToken());
        apiService.getSubjects(map).enqueue(new Callback<Subjects>()
        {
            @Override
            public void onResponse(Call<Subjects> call, Response<Subjects> response)
            {
                List<Subject> subjects = response.body().list;
                for (Subject s : subjects)
                {
                    mVisitorLayer.add(new DrawableCircle(
                            s.name, 3.5f, Utils.getRandomColor(), new Coordinates(s.localization.latitude,
                            s.localization.longitude)));
                    mVisitorLayer.add(new DrawableText(s.name, new Coordinates(
                            s.localization.latitude, s.localization.longitude), s.name, 1.5f));
                }
            }

            @Override
            public void onFailure(Call<Subjects> call, Throwable t)
            {

            }
        });
    }
}

