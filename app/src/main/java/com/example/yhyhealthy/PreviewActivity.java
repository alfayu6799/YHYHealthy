package com.example.yhyhealthy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

/************************
* 排卵拍照後結果上傳page
* 後台辨識
* ***********************/

public class PreviewActivity extends AppPage {

    ImageView imageView;
    Button identify;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        imageView = findViewById(R.id.preview);
        identify = findViewById(R.id.btnIdentify);

        String path = getIntent().getStringExtra("path");
        if(path != null){
            imageView.setImageURI(Uri.fromFile(new File(path)));
        }

        identify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //照片上傳到後台後再回到編輯紀錄的頁面
                Intent intent = new Intent(PreviewActivity.this, PeriodRecordActivity.class);
                intent.putExtra("path", path);
                startActivity(intent);
                finish();
            }
        });
    }

}
