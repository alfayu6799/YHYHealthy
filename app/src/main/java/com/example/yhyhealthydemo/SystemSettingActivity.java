package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/*****
 * 系統設定 - 系統設定
 * 溫度單位
 * 藍芽
 */

public class SystemSettingActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView back;
    ImageView BleSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_setting);

        back = findViewById(R.id.ivBackSysSetting);
        BleSetting = findViewById(R.id.ivToBleSetting);
        back.setOnClickListener(this);
        BleSetting.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackSysSetting:
                finish();
                break;
            case R.id.ivToBleSetting:
                //藍芽設定
                startActivity(new Intent(this, SysBleActivity.class));
                break;
        }
    }
}