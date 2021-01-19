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

    //婚姻狀況
    Switch    marriageStatus;
    Switch    contraceptionStatus;
    Switch    childStatus;

    //api
    ApiProxy proxy;
    MarriageData marriageData;

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
        marriageData = MarriageData.newInstance(result.toString());
        Log.d(TAG, "parserJson: " + result.toString());

        //婚姻
        boolean married = marriageData.getSuccess().isMarried();
        marriageStatus.setChecked(married);

        //孩子
        boolean child = marriageData.getSuccess().isHasChild();
        childStatus.setChecked(child);

        //避孕
        boolean contraception = marriageData.getSuccess().isContraception();
        contraceptionStatus.setChecked(contraception);
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
        changeBasicInfo.setOnClickListener(this);
        changePassword.setOnClickListener(this);

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
            case R.id.ivPasswordChange:
                startActivity(new Intent(this, UserChangePassActivity.class)); //變更密碼
                break;
            case R.id.ivUserBasicInfo:
                startActivity(new Intent(this, UserBasicActivity.class));  //個人基本資料
                break;
            case R.id.ivUserVerificationStyle:
                dialogVerification();  //驗證方式 : email or SMS
                break;
            case R.id.ivUserAuthorizationCode:
                dialogAuthCode();   //遠端授權碼
                break;
            case R.id.ivMenstruationSetting:
                startActivity(new Intent(this, MenstruationSettingActivity.class)); //經期設定頁面
                break;
            case R.id.ivPregnancySetting:
                startActivity(new Intent(this, PregnancySettingActivity.class));  //懷孕設定頁面
                break;
            case R.id.ivUserDeviceNo:
                //裝置序號
                break;


        }
    }

    //遠端授權碼彈跳視窗
    private void dialogAuthCode() {
    }

    //驗證方式 : email or SMS 彈跳視窗
    private void dialogVerification() {
    }

    //Switch button onclick
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
        switch (compoundButton.getId()){
            case R.id.switchMarriage: //婚姻狀況
                if (isCheck){
                    marriageStatus.setChecked(true);
                }else{
                    marriageStatus.setChecked(false);
                }
                break;
            case R.id.switchContraception: //行房
                if (isCheck){
                    contraceptionStatus.setChecked(true);
                }else {
                    contraceptionStatus.setChecked(false);
                }
                break;
            case R.id.switchChild:   //小孩
                if (isCheck){
                    childStatus.setChecked(true);
                }else {
                    childStatus.setChecked(false);
                }
                break;
        }
    }
}