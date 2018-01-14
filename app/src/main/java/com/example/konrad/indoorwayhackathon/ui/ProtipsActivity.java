package com.example.konrad.indoorwayhackathon.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Adapter;
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
        ArrayList<String> carL = new ArrayList<String>();
        carL.addAll(Arrays.asList(getProtips()));

        this.adapter = new ArrayAdapter<String>(this, R.layout.protip_list_item, carL);
        list.setAdapter(this.adapter);
    }

    String [] getProtips() {
        return new String[] {
                "Sala 01 jest gdzie indziej niż 1. Tej pierwszej szukaj w piwnicy",
                "Lepiej uważać na automat przy głównym wejściu - potrafi nie wydać produktu!",
                "Szukasz wygodnego i cichego miejsca do nauki? Sprawdź salę 102!",
                "Bądź miły dla szatniarzy - żeby prypadkiem nie zginęła Twoja kurtka!",
                "Najwięcej kebabów jest na ulicy Pokątnej!",
                "Wieża magów znajduje się na końcu korytarza w skrzydle C",
        };
    }
}
