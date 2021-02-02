package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

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

public class UserChangeVerifiActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UserChangeVerifiActivit";

    private TextInputLayout mailStyleLayout, phoneStyleLayout, mobileStyleLayout, passwordLayout;
    private RadioGroup StyleGroup;
    private EditText   editMail, editTelCode, editMobile, editPassword;
    private Button     btnSave;
    private ImageView  imgExit;
    private String Style = "mail";

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
            ProgressDialogUtil.showProgressDialog(UserChangeVerifiActivity.this);
        }

        @Override
        public void onSuccess(JSONObject result) {
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
                                Toasty.success(UserChangeVerifiActivity.this, getString(R.string.change_verification_success), Toast.LENGTH_SHORT, true).show();
                                finish();
                            }else if(code == 2) { //已開通
                                finish();
                            }
                        }else {
                            Toasty.error(UserChangeVerifiActivity.this, getString(R.string.failure), Toast.LENGTH_SHORT, true).show();
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