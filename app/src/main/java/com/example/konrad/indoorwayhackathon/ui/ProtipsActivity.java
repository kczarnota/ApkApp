package com.example.konrad.indoorwayhackathon.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.konrad.indoorwayhackathon.R;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProtipsActivity extends AppCompatActivity {

    @BindView(R.id.list_of_tips)
    ListView list;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protips);
        ButterKnife.bind(this);

        String cars[] = {"Mercedes", "Fiat", "Ferrari", "Aston Martin", "Lamborghini", "Skoda", "Volkswagen", "Audi", "Citroen"};

        ArrayList<String> carL = new ArrayList<String>();
        carL.addAll(Arrays.asList(cars));

        this.adapter = new ArrayAdapter<String>(this, R.layout.pritip_list_item, carL);

        list.setAdapter(this.adapter);
    }
}
