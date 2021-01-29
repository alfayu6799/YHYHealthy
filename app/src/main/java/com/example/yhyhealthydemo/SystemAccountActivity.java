package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/** **  *****
 *  設定 - 帳戶設定
 * *** ****/

public class SystemAccountActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SystemAccountActivity";

    ImageView back;
    ImageView changePW, authorizationCode, changeDeviceNo, compCodeStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_account);

        back = findViewById(R.id.ivBackSetting8);
        changePW = findViewById(R.id.ivPasswordChange);
        authorizationCode = findViewById(R.id.ivUserAuthorizationCode);
        changeDeviceNo = findViewById(R.id.ivUserDeviceNo);
        compCodeStyle = findViewById(R.id.ivCompCodeChange);

        back.setOnClickListener(this);
        changePW.setOnClickListener(this);
        authorizationCode.setOnClickListener(this);
        changeDeviceNo.setOnClickListener(this);
        compCodeStyle.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackSetting8: //回上一頁
                finish();
                break;
            case R.id.ivPasswordChange:
                startActivity(new Intent(this, UserChangePassActivity.class)); //變更密碼
                break;
            case R.id.ivUserAuthorizationCode:  //授權碼
                dialogAuthCode();
                break;
            case R.id.ivUserDeviceNo:
                dialogDevice();             //裝置序號
                break;
            case R.id.ivCompCodeChange:   //驗證方式變更
                dialogCompCode();
                break;
        }
    }

    //驗證方式變更採用彈跳視窗
    private void dialogCompCode() {
        Log.d(TAG, "dialogCompCode: ");
    }

    //裝置序號採用彈跳視窗
    private void dialogDevice() {
        Log.d(TAG, "dialogDevice: ");
    }

    //授權碼採用彈跳視窗
    private void dialogAuthCode() {
        Log.d(TAG, "dialogAuthCode: ");
    }
}