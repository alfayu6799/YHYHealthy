package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.example.yhyhealthydemo.datebase.MarriageData;
import com.example.yhyhealthydemo.module.ApiProxy;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.yhyhealthydemo.module.ApiProxy.MARRIAGE_INFO;

/**
 * 使用者設定:
 * 基本資料
 * 婚姻狀況
 * 經期設定
 * 懷孕設定
 * */

public class SystemUserActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView back;
    ImageView PregnancyInfo, MenstruationInfo;
    ImageView basicInfo, marriageInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_user);

        initView();
    }

    private void initView() {
        back = findViewById(R.id.ivBackSetting);
        basicInfo = findViewById(R.id.ivUserBasicInfo);
        MenstruationInfo = findViewById(R.id.ivMenstruationSetting);
        PregnancyInfo = findViewById(R.id.ivPregnancySetting);
        marriageInfo = findViewById(R.id.ivMarriageSetting);

        back.setOnClickListener(this);
        PregnancyInfo.setOnClickListener(this);
        MenstruationInfo.setOnClickListener(this);
        basicInfo.setOnClickListener(this);
        marriageInfo.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackSetting:
                finish();  //返回設定頁並關閉此Activity
                break;
            case R.id.ivUserBasicInfo:
                startActivity(new Intent(this, UserBasicActivity.class));  //個人基本資料
                break;
            case R.id.ivMarriageSetting:
                startActivity(new Intent(this, MarriageSettingActivity.class));  //婚姻狀態
                break;
            case R.id.ivMenstruationSetting:
                startActivity(new Intent(this, MenstruationSettingActivity.class)); //經期設定頁面
                break;
            case R.id.ivPregnancySetting:
                startActivity(new Intent(this, PregnancySettingActivity.class));  //懷孕設定頁面
                break;
        }
    }

}