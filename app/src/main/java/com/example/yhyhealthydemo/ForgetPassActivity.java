package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.yhyhealthydemo.module.ApiProxy;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthydemo.module.ApiProxy.COMP_CODE_REQUEST;
import static com.example.yhyhealthydemo.module.ApiProxy.FORGET_PASSWORD;

public class ForgetPassActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ForgetPassActivity";

    private Button btnSend, btnGetCompCode;
    private TextInputLayout verificationLayout, newPasswordLayout;
    private TextInputEditText accountInput, verificationInput, newPasswordInput;

    private ApiProxy proxy;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pass);

        proxy = ApiProxy.getInstance();

        initView();
    }

    private void initView() {
        accountInput = findViewById(R.id.edtForgetAccount);
        verificationInput = findViewById(R.id.edtNewCompCode);
        newPasswordInput = findViewById(R.id.edtNewPassword);

        verificationLayout = findViewById(R.id.verificationLayout);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);

        btnSend = findViewById(R.id.btnForgetSend);
        btnGetCompCode = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        btnGetCompCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnForgetSend:
                getCompCode();
                break;
            case R.id.btnSend:
                checkBeforeUpdate();
                break;
        }
    }

    //收到授權碼後更新到後端
    private void checkBeforeUpdate() {
        //檢查上傳的資料是否齊全
        if (TextUtils.isEmpty(verificationInput.getText().toString())) {
            Toasty.error(ForgetPassActivity.this, getString(R.string.compcode_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        String newPassword = newPasswordInput.getText().toString();
        if(TextUtils.isEmpty(newPassword) || (newPassword.length() < 6)) {
            Toasty.error(ForgetPassActivity.this, getString(R.string.number_less_six), Toast.LENGTH_SHORT, true).show();
            return;
        }

        //上傳給後端
        updateToApi();
    }

    //後端更新
    private void updateToApi() {
        JSONObject json = new JSONObject();
        try {
            json.put("account" , accountInput.getText().toString());
            json.put("password", newPasswordInput.getText().toString());
            json.put("verCode", verificationInput.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //忘記密碼Api
        proxy.buildPassWD(FORGET_PASSWORD, json.toString(), forgetListener);
    }

    private ApiProxy.OnApiListener forgetListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(ForgetPassActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            Log.d(TAG, "輸入新密碼及驗證碼後的解析: " + result.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            Toasty.success(ForgetPassActivity.this, getString(R.string.change_password_success), Toast.LENGTH_SHORT, true).show();
                            finish();
                        }else {
                            Toasty.error(ForgetPassActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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
            progressDialog.dismiss();
        }
    };


    //通知後台給授權碼
    private void getCompCode() {

        if(TextUtils.isEmpty(accountInput.getText().toString())){
            Toasty.error(ForgetPassActivity.this, getString(R.string.account_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        //手機語系
        String language = getResources().getConfiguration().locale.getLanguage();
        String country = getResources().getConfiguration().locale.getCountry();
        String defaultLan = language + "-" + country; //ex:zh-TW, zh-CN

        String account = accountInput.getText().toString();
        JSONObject json = new JSONObject();
        try {
            json.put("account", account);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //向後端要求發送驗證碼
        proxy.buildCompCode(COMP_CODE_REQUEST, json.toString(), defaultLan, requestListener);
    }

    private ApiProxy.OnApiListener requestListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(ForgetPassActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parser(result);
                }
            });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {
            progressDialog.dismiss();
        }
    };

    //解析後台回來的資料
    private void parser(JSONObject result) {
        try {
            JSONObject object = new JSONObject(result.toString());
            int errorCode = object.getInt("errorCode");
            if(errorCode == 0){ //要求驗證碼重發成功
                JSONObject success = object.getJSONObject("success");
                int code = success.getInt("statusCode");
                if (code == 1){
                    showCompCodeLayout(); //顯示可以輸入驗證碼及密碼的Layout
                }else if (code == 2){
                    finish();
                }
            }else {
                Toasty.error(ForgetPassActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //顯示輸入驗證碼及密碼的Layout
    private void showCompCodeLayout() {
        verificationLayout.setVisibility(View.VISIBLE);
        newPasswordLayout.setVisibility(View.VISIBLE);
        btnGetCompCode.setVisibility(View.VISIBLE);
        btnSend.setVisibility(View.GONE);
    }
}