package com.example.konrad.indoorwayhackathon.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.konrad.indoorwayhackathon.R;
import com.example.konrad.indoorwayhackathon.Utils;
import com.example.konrad.indoorwayhackathon.net.login.Api;
import com.example.konrad.indoorwayhackathon.net.login.ApiService;
import com.example.konrad.indoorwayhackathon.net.login.Quest;
import com.indoorway.android.fragments.sdk.map.IndoorwayMapFragment;
import com.indoorway.android.fragments.sdk.map.MapFragment;

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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_photo);
        ButterKnife.bind(this);
        //TODO get photo and it's location

        ApiService apiService = Api.getApi();
        Map<String, String> map = new HashMap<>();
        map.put("Authorization", "Bearer " + Utils.getToken());
        apiService.getQuest(map).enqueue(new Callback<Quest>()
        {
            @Override
            public void onResponse(Call<Quest> call, Response<Quest> response)
            {
                Toast.makeText(SearchPhotoActivity.this, response.body().imageName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Quest> call, Throwable t)
            {

            }
        });
    }

    @Override
    public void onMapFragmentReady(MapFragment mapFragment)
    {
        mapFragment.getMapView().load(Utils.BUILDING_UUID, Utils.SECOND_FLOOR_UUID);
    }
}
