package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.yhyhealthydemo.module.ApiProxy;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.concurrent.BlockingDeque;

import static com.example.yhyhealthydemo.module.ApiProxy.LOGIN;
import static com.example.yhyhealthydemo.module.ApiProxy.REGISTER;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Button register;
    private EditText account, password, email;
    private EditText edtTelCode, edtMobile;
    private TextInputLayout emailLayout, telCodeLayout, mobileLayout;

    private RadioGroup registerGroup;
    private String verificationStyle = "email";
    String emailPattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";

    //api
    ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        proxy = ApiProxy.getInstance();

        initView();
    }

    private void initView() {
        account = findViewById(R.id.edtAccountInput);
        password = findViewById(R.id.edtPasswordInput);

        emailLayout = findViewById(R.id.EmailLayout);
        telCodeLayout = findViewById(R.id.TelCodeLayout);
        mobileLayout = findViewById(R.id.MobileLayout);
        email = findViewById(R.id.edtEmailInput);
        edtTelCode = findViewById(R.id.edTelCode);
        edtTelCode.setText("CN"); //暫時
        edtMobile = findViewById(R.id.edMobile);

        registerGroup = findViewById(R.id.rdGroupRegister);
        registerGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rdoBtnEmail){
                    emailLayout.setVisibility(View.VISIBLE);
                    telCodeLayout.setVisibility(View.GONE);
                    mobileLayout.setVisibility(View.GONE);
                    verificationStyle = "email";
                }else{
                    verificationStyle = "phone";
                    emailLayout.setVisibility(View.GONE);
                    telCodeLayout.setVisibility(View.VISIBLE);
                    mobileLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        register = findViewById(R.id.btnRegisterSend);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //帳號與密碼不得為空
                if (TextUtils.isEmpty(account.getText().toString()) || TextUtils.isEmpty(password.getText().toString()))
                         return;

                //採用mail驗證方式
                if(verificationStyle.equals("email")){
                    if(TextUtils.isEmpty(email.getText().toString())){
                        Toast.makeText(RegisterActivity.this, getString(R.string.please_input_email), Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        if (email.getText().toString().trim().matches(emailPattern)){ //有效的mail address
                            //寫回後台
                            upDataToApi();
//                            Log.d(TAG, "onClick: account:" + account.getText().toString() + " password:" + password.getText().toString() + " mail:" + email.getText().toString());
                        }else {
                            Toast.makeText(RegisterActivity.this, getString(R.string.please_input_vaild_email), Toast.LENGTH_SHORT).show();
                        }
                    }
                } //end of verificationStyle is email

                //採用簡訊驗證方式
                if(verificationStyle.equals("phone")){
                    if(TextUtils.isEmpty(edtTelCode.getText().toString()) || TextUtils.isEmpty(edtMobile.getText().toString())){
                        Toast.makeText(RegisterActivity.this, getString(R.string.please_input_phone), Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        //寫回後台
                        upDataToApi();
                    }
                }
            }
        });
    }

    private void upDataToApi() {
        Log.d(TAG, "upDataToApi: account:" + account.getText().toString()
                + " password:" + password.getText().toString()
                + " Email" + email.getText().toString()
                + " telCode:"  + edtTelCode.getText().toString()
                + " mobile:"   + edtMobile.getText().toString());

        String accountNo = account.getText().toString().trim();
        String passWD = password.getText().toString().trim();
        String mailAddress = email.getText().toString().trim();
        String telCodeNo = edtTelCode.getText().toString().trim();
        String phoneNo = edtMobile.getText().toString().trim();

        JSONObject json = new JSONObject();
        try {
            json.put("account", accountNo);
            json.put("password", passWD);
            json.put("email", mailAddress);
            json.put("telCode", telCodeNo);
            json.put("mobile", phoneNo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //proxy.build(REGISTER, json.toString(), registerListener);
    }

    private ApiProxy.OnApiListener registerListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            Log.d(TAG, "onSuccess: " + result.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

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

}
