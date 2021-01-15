package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.example.yhyhealthydemo.module.ApiProxy;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.yhyhealthydemo.module.ApiProxy.MARRIAGE_INFO;
import static com.example.yhyhealthydemo.module.ApiProxy.MENSTRUAL_RECORD_INFO;

/**
 * 使用者設定:
 * 變更密碼
 * 基本資料
 * 驗證方式
 * 遠端授權碼
 * 婚姻狀況 (From api)
 * 經期設定
 * 懷孕設定
 * 裝置序號
 * */

public class SystemUserActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "SystemUserActivity";

    ImageView back;
    ImageView toPregnancy, toMenstruation;
    ImageView changePassword, changeBasicInfo, verificationStyle, changeDeviceNo;
    ImageView authorizationCode;

    Switch    marriageStatus;
    Switch    contraceptionStatus;
    Switch    childStatus;

    //api
    ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_user);

        initView();

        initData();
    }

    private void initData() {
        proxy = ApiProxy.getInstance();

        JSONObject json = new JSONObject();
        try {
            json.put("type", "3");
            json.put("userId", "H5E3q5MjA=");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(MARRIAGE_INFO, json.toString(), marriageListeren);
    }

    private ApiProxy.OnApiListener marriageListeren = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parserJson(result); //解析後台來的資料
                }
            });
        }

        @Override
        public void onFailure(String message) {

        }

        @Override
        public void onPostExecute() {

        }
    };

    //解析後台來的資料
    private void parserJson(JSONObject result) {

    }

    private void initView() {
        back = findViewById(R.id.ivBackSetting);
        changePassword = findViewById(R.id.ivPasswordChange);
        changeBasicInfo = findViewById(R.id.ivUserBasicInfo);
        verificationStyle = findViewById(R.id.ivUserVerificationStyle);
        authorizationCode = findViewById(R.id.ivUserAuthorizationCode);
        toMenstruation = findViewById(R.id.ivMenstruationSetting);
        toPregnancy = findViewById(R.id.ivPregnancySetting);
        changeDeviceNo = findViewById(R.id.ivUserDeviceNo);

        marriageStatus = findViewById(R.id.switchMarriage);
        contraceptionStatus = findViewById(R.id.switchContraception);
        childStatus = findViewById(R.id.switchChild);

        back.setOnClickListener(this);
        toPregnancy.setOnClickListener(this);
        toMenstruation.setOnClickListener(this);

        marriageStatus.setOnCheckedChangeListener(this);
        contraceptionStatus.setOnCheckedChangeListener(this);
        childStatus.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackSetting:
                finish();  //返回設定頁並關閉此Activity
                break;
            case R.id.ivMenstruationSetting:
                startActivity(new Intent(this, MenstruationSettingActivity.class)); //經期設定頁面
                break;
            case R.id.ivPregnancySetting:
                startActivity(new Intent(this, PregnancySettingActivity.class));  //懷孕設定頁面
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
        switch (compoundButton.getId()){
            case R.id.switchMarriage:
                if (isCheck){
                    Log.d(TAG, "onCheckedChanged: 婚姻Y" );
                }else{
                    Log.d(TAG, "onCheckedChanged: 婚姻N" );
                }
                break;
            case R.id.switchContraception:
                if (isCheck){
                    Log.d(TAG, "onCheckedChanged: 避孕Y" );
                }else {
                    Log.d(TAG, "onCheckedChanged: 避孕N" );
                }
                break;
            case R.id.switchChild:
                if (isCheck){
                    Log.d(TAG, "onCheckedChanged: 孩子Y" );
                }else {
                    Log.d(TAG, "onCheckedChanged: 孩子N" );
                }
                break;
        }
    }
}