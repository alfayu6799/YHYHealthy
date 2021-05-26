package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.yhyhealthy.module.ApiProxy;

public class TempRecordActivity extends AppCompatActivity {

    private ImageView back;
    private RecyclerView rvTemperature;

    private ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_record);

        initView();
    }

    private void initView() {
        back = findViewById(R.id.ivBackTemp);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        rvTemperature = findViewById(R.id.rvTempRecord);
        proxy = ApiProxy.getInstance();
    }
}