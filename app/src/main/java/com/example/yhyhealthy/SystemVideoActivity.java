package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * 教學影片 - 系統操作的影片
 * create: 2021/05/21
 * **/

public class SystemVideoActivity extends AppCompatActivity {

    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_video);

        btnBack = findViewById(R.id.ivTrainBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}