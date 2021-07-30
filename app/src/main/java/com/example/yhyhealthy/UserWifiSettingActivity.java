package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class UserWifiSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UserWifiSettingActivity";

    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_wifi_setting);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //禁止旋轉

        back = findViewById(R.id.ivBackWifiSetting); //返回

        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackWifiSetting: //返回上一頁
                finish();
                break;
        }
    }
}