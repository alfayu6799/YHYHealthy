package com.example.yhyhealthydemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yhyhealthydemo.tools.ProgressDialogUtil;

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
            case R.id.ivCompCodeChange:   //驗證方式變更
                startActivity(new Intent(this, UserChangeVerifiActivity.class));
                break;
            case R.id.ivUserAuthorizationCode:  //授權碼
                dialogAuthCode();
                break;
            case R.id.ivUserDeviceNo:
                dialogDevice();             //裝置序號
                break;
        }
    }

    //裝置序號採用彈跳視窗
    private void dialogDevice() {
        Log.d(TAG, "dialogDevice: ");
    }

    //授權碼採用彈跳視窗
    private void dialogAuthCode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.edit_auth_code));
        builder.setMessage(getString(R.string.please_press_button_to_update));
        TextView authCode = new TextView(this);
        builder.setView(authCode);
        //需要跟後台要求授權碼直接顯示在EditText欄位內
        getAuthCodeFromApi();

        builder.setPositiveButton(getString(R.string.update_auth_code), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //當使用者按下更新Button時會執行這邊的邏輯
                updateToApi();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setView(authCode, 0, 0, 0, 0);
        dialog.show();
    }

    private void updateToApi() {
        Log.d(TAG, "updateToApi: wait for Api response");
         //ProgressDialogUtil.showProgressDialog(SystemAccountActivity.this);
    }

    //跟後台要求授權碼
    private void getAuthCodeFromApi() {
    }
}