package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.yhyhealthydemo.adapter.ObserverViewAdapter;
import com.example.yhyhealthydemo.datebase.Observation;
import com.example.yhyhealthydemo.tools.SpacesItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class SysObservationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ObserverViewAdapter adapter;
    private List<Observation> observationList;
    private Observation observation1;  //假資料
    private Observation observation2;  //假資料

    private Button addObserver;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_observation);

        initView();

        initData();
    }

    private void initData() {
        int spacingInPixels = 10;  //設定item間距的距離
        observationList = new ArrayList<>();

        //塞入假資料
        observation1 = new Observation(R.mipmap.imageview3, "安·海瑟薇", "女性", "1982-11-12");
        observationList.add(observation1);

        adapter = new ObserverViewAdapter(this, observationList);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //設定item間距
    }

    private void initView() {
        addObserver = findViewById(R.id.btnObserveAdd);
        recyclerView = findViewById(R.id.rvObserverList);
        back = findViewById(R.id.ivBackSetting10);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}