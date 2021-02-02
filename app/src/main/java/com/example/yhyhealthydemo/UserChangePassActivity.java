package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yhyhealthydemo.module.ApiProxy;
import com.example.yhyhealthydemo.tools.ProgressDialogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthydemo.module.ApiProxy.CHANGE_PASSWORD;
import static com.example.yhyhealthydemo.module.ApiProxy.MARRIAGE_INFO;

/**
 * 使用者變更密碼
 * */

public class UserChangePassActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UserChangePassActivity";

    Button update;
    ImageView back;
    EditText oldPassword, newPassword;

    //api
    ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userchange_passwd);

        proxy = ApiProxy.getInstance();

        initView();
    }

    private void initView() {
        update = findViewById(R.id.btnUpdatePassWD);
        back = findViewById(R.id.ivBackSetting5);
        oldPassword = findViewById(R.id.edtOldPassword);
        newPassword = findViewById(R.id.edtNewPassword);
        update.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackSetting5: //回上頁
                finish();
                break;
            case R.id.btnUpdatePassWD:
                updateToApi();       //上傳到後台更新
                break;

        }
    }

    //後台更新
    private void updateToApi() {
        String oldPW = oldPassword.getText().toString();
        String newPW = newPassword.getText().toString();

        if(TextUtils.isEmpty(oldPW) || (TextUtils.isEmpty(newPW))){
            Toasty.error(UserChangePassActivity.this, getString(R.string.not_allow_empty), Toast.LENGTH_SHORT, true).show();
        }else {
            JSONObject json = new JSONObject();
            try {
                json.put("password", oldPW);
                json.put("newPassword", newPW);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            proxy.buildPOST(CHANGE_PASSWORD, json.toString(), changePasswordListener);
        }
    }

    private ApiProxy.OnApiListener changePasswordListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            ProgressDialogUtil.showProgressDialog(UserChangePassActivity.this);
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        boolean success = object.getBoolean("success");
                        if(success){
                            Toasty.success(UserChangePassActivity.this, getString(R.string.change_password_success), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(UserChangePassActivity.this, LoginActivity.class));
                            finish();
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
            ProgressDialogUtil.dismiss();
        }
    };
}