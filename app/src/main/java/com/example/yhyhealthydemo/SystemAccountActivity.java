package com.example.yhyhealthydemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthydemo.module.ApiProxy;
import com.example.yhyhealthydemo.tools.ProgressDialogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthydemo.module.ApiProxy.MONITOR_CODE;
import static com.example.yhyhealthydemo.module.ApiProxy.MONITOR_CODE_RENEW;

/** **  *****
 *  設定 - 帳戶設定
 *  密碼變更   UserChangePassActivity
 *  遠端授權碼 dialog
 *  驗證方式  UserChangeVerifiActivity
 *  裝置序號  UserDeviceActivity
 *  create 2021/03/01
 * *** ****/

public class SystemAccountActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SystemAccountActivity";

    ImageView back;
    ImageView changePW, authorizationCode, changeDeviceNo, compCodeStyle;

    //
    private int authCode = 0;

    //api
    private ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_account);

        initView();

        initData();

    }

    private void initData() {
        proxy = ApiProxy.getInstance();

        //取得授權碼
        getAuthCodeFromApi();
    }

    private void initView() {
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
            case R.id.ivCompCodeChange:
                startActivity(new Intent(this, UserChangeVerifiActivity.class)); //驗證方式變更
                break;
            case R.id.ivUserAuthorizationCode:  //授權碼
                dialogAuthCode();
                break;
            case R.id.ivUserDeviceNo:  //裝置序號
                startActivity(new Intent(this, UserDeviceActivity.class));
                break;
        }
    }

    //授權碼採用彈跳視窗
    private void dialogAuthCode() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.edit_auth_code));
        builder.setMessage(getString(R.string.please_press_button_to_update));

        TextView textView = new TextView(this);
        textView.setText(String.valueOf(authCode));      //授權碼
        textView.setTextSize(25);                        //字體大小
        textView.setTextColor(Color.RED);                //字體顏色
        textView.setTypeface(null, Typeface.BOLD);    //粗字體

        builder.setView(textView);

        //確定=關閉視窗
        builder.setNeutralButton(getString(R.string.slycalendar_save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        //更新
        builder.setPositiveButton(getString(R.string.update_auth_code), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //當使用者按下更新Button時會執行這邊的邏輯
                updateToApi();
                //可以不關閉視窗的作法:https://www.jianshu.com/p/2278c0b90f0d
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setView(textView, 350, 0, 0, 0);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);  //Button文字小寫顯示
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setAllCaps(false);   //Button文字小寫顯示
    }

    private void updateToApi() {
        proxy.buildPOST(MONITOR_CODE_RENEW, "", codeUpdateListener);
    }

    private ApiProxy.OnApiListener codeUpdateListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0) {
                            Toasty.success(SystemAccountActivity.this, getString(R.string.auth_code_update_success), Toast.LENGTH_SHORT, true).show();
                            getAuthCodeFromApi(); //重新取得授權碼
                        }else if (errorCode == 23){
                            Toasty.error(SystemAccountActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(SystemAccountActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(SystemAccountActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

    //跟後台要求授權碼
    private void getAuthCodeFromApi() {
        proxy.buildPOST(MONITOR_CODE, "", authCodeListener);
    }

    private ApiProxy.OnApiListener authCodeListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(result.toString());
                        int errorCode = jsonObject.getInt("errorCode");
                        if (errorCode == 0){
                            authCode = jsonObject.getInt("success"); //將授權碼給予全域
                        }else if (errorCode == 23){
                            Toasty.error(SystemAccountActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(SystemAccountActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(SystemAccountActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {

        }
    };

}