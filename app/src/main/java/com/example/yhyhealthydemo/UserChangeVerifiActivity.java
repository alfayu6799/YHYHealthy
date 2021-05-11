package com.example.yhyhealthydemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.yhyhealthydemo.module.ApiProxy;
import com.example.yhyhealthydemo.tools.ProgressDialogUtil;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthydemo.module.ApiProxy.CHANGE_VERIFICATION_STYLE;
import static com.example.yhyhealthydemo.module.ApiProxy.COMP;

public class UserChangeVerifiActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UserChangeVerifiActivit";

    private TextInputLayout mailStyleLayout, phoneStyleLayout, mobileStyleLayout, passwordLayout;
    private RadioGroup StyleGroup;
    private EditText   editMail, editTelCode, editMobile, editPassword;
    private Button     btnSave;
    private ImageView  imgExit;
    private String Style = "mail";
    private ProgressDialog progressDialog;

    //api
    ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_change_verifi);

        proxy = ApiProxy.getInstance();

        initView();
    }

    private void initView() {
        editMail = findViewById(R.id.edtEmailStyle);    //信箱
        editTelCode = findViewById(R.id.edtTelCode);    //國際編碼
        editTelCode.setText("CN");                      //只有大陸才會用到
        editTelCode.setFocusable(false);                //不可編輯
        editTelCode.setFocusableInTouchMode(false);     //不可編輯
        
        editMobile = findViewById(R.id.edtMobile);      //手機號碼
        editPassword = findViewById(R.id.edtPassword);  //密瑪

        //信箱Layout
        mailStyleLayout = findViewById(R.id.emailStyleLayout);

        //密碼
        passwordLayout = findViewById(R.id.passwordLayout);

        //簡訊Layout
        phoneStyleLayout = findViewById(R.id.phoneStyleLayout);
        mobileStyleLayout = findViewById(R.id.mobileStyleLayout);

        btnSave = findViewById(R.id.btnSaveStyleToApi);
        imgExit = findViewById(R.id.igBackSetting15);

        btnSave.setOnClickListener(this);
        imgExit.setOnClickListener(this);

        StyleGroup = findViewById(R.id.rdoGroupStyle);
        StyleGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rtoBtnMailStyle){
                    mailStyleLayout.setVisibility(View.VISIBLE);
                    passwordLayout.setVisibility(View.VISIBLE);
                    phoneStyleLayout.setVisibility(View.GONE);
                    mobileStyleLayout.setVisibility(View.GONE);
                    Style = "mail";
                }else {
                    mailStyleLayout.setVisibility(View.GONE);
                    passwordLayout.setVisibility(View.VISIBLE);
                    phoneStyleLayout.setVisibility(View.VISIBLE);
                    mobileStyleLayout.setVisibility(View.VISIBLE);
                    Style = "mobile";
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.igBackSetting15:
                finish();  //結束
                break;
            case R.id.btnSaveStyleToApi:
                checkBeforeUpdate();  //檢查資料的完整性
                break;
        }
    }

    //送資料到後台前先檢查密碼是否有輸入
    private void checkBeforeUpdate() {

        //密碼
        if (TextUtils.isEmpty(editPassword.getText().toString())){
            Toasty.error(UserChangeVerifiActivity.this, getString(R.string.password_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        //上傳到後端
        updateToApi();
    }

    //上傳到後端
    private void updateToApi() {
        //取得手機語系
        String language = getResources().getConfiguration().locale.getLanguage();
        String country = getResources().getConfiguration().locale.getCountry();
        String defaultLan = language + "-" + country;

        JSONObject json = new JSONObject();
        try {
            json.put("password", editPassword.getText().toString());
            json.put("email", editMail.getText().toString());
            json.put("telCode", editTelCode.getText().toString());
            json.put("mobile",editMobile.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildVerification(CHANGE_VERIFICATION_STYLE, json.toString(), defaultLan, verificationChangeListener);
    }

    private ApiProxy.OnApiListener verificationChangeListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(UserChangeVerifiActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            Log.d(TAG, "onSuccess: " + result.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){ //驗證方式修改成功
                            JSONObject success = object.getJSONObject("success");
                            int code = success.getInt("statusCode");
                            if (code == 1){ //尚未開通
                                showCompCode();
                            }else if(code == 2) { //已開通
                                finish();
                            }
                        }else if (errorCode == 23){ //token失效 2021/05/11
                            Toasty.error(UserChangeVerifiActivity.this, getString(R.string.update_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(UserChangeVerifiActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(UserChangeVerifiActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //輸入驗證碼完成更新 2021/02/03
    private void showCompCode(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.please_input_compcode));
        builder.setMessage(getString(R.string.need_comp_code));

        //set custom layout
        View compLayout = getLayoutInflater().inflate(R.layout.dialog_comp, null);
        builder.setView(compLayout);
        builder.setCancelable(false);

        //add ok button
        builder.setPositiveButton(getString(R.string.slycalendar_save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText editText = compLayout.findViewById(R.id.edtCompCode);
                if(TextUtils.isEmpty(editText.getText().toString())){
                    Toasty.error(UserChangeVerifiActivity.this, getString(R.string.compcode_is_not_empty), Toast.LENGTH_SHORT, true).show();
                    return;
                }
                //傳至後台驗證比對
                checkComp(editText);
            }
        });
        AlertDialog compDialog = builder.create();
        compDialog.show();
    }

    private void checkComp(EditText editText){
        //去SharedPreferences取得使用者的帳號:ACCOUNT
        String account = getSharedPreferences("yhyHealthy", MODE_PRIVATE).getString("ACCOUNT", "");

        JSONObject json = new JSONObject();
        try {
            json.put("account", account);
            json.put("verCode", editText.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildInit(COMP, json.toString(), verificationListener);
    }

    private ApiProxy.OnApiListener verificationListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            //顯示對話方塊
            if(progressDialog == null) {
                progressDialog = ProgressDialog.show(UserChangeVerifiActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }
            if (!progressDialog.isShowing()) progressDialog.show();
        }

        @Override
        public void onSuccess(JSONObject result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject object = new JSONObject(result.toString());
                            int errorCode = object.getInt("errorCode");
                            if(errorCode == 5){  //驗證碼不對
                                Toasty.error(UserChangeVerifiActivity.this, getString(R.string.comp_code_error), Toast.LENGTH_SHORT, true).show();
                            }else if (errorCode == 0){ //驗證成功
                                Toasty.success(UserChangeVerifiActivity.this, getString(R.string.change_verification_success), Toast.LENGTH_SHORT, true).show();
                                finish();
                            }else if (errorCode == 23 ) {//token失效 2021/05/11
                                Toasty.error(UserChangeVerifiActivity.this, getString(R.string.update_failure), Toast.LENGTH_SHORT, true).show();
                                startActivity(new Intent(UserChangeVerifiActivity.this, LoginActivity.class)); //重新登入
                                finish();
                            }else {
                                Toasty.error(UserChangeVerifiActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure:  " + message);
        }

        @Override
        public void onPostExecute() {
            progressDialog.dismiss();
        }
    };
}