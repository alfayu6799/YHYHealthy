package com.example.yhyhealthydemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthydemo.module.ApiProxy;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import static com.example.yhyhealthydemo.module.ApiProxy.FORGET_PASSWORD;
import static com.example.yhyhealthydemo.module.ApiProxy.LOGIN;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    Button loginButton;
    EditText account, password;
    TextView register, forget;

    //api
    ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //不讓虛擬鍵盤蓋文
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        proxy = ApiProxy.getInstance();

        initView();
    }

    private void initView() {
        account = findViewById(R.id.et_account);
        password = findViewById(R.id.et_password);
//        //暫時
//        account.setText("demo05");
//        password.setText("111111");
        
        //註冊
        register = findViewById(R.id.tv_register);
        
        //忘記密碼
        forget = findViewById(R.id.tv_forget);
        
        //登入Onclick
        loginButton = findViewById(R.id.bt_login);

        loginButton.setOnClickListener(this);
        forget.setOnClickListener(this);
        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_login:
                userLoginApi(); //登入時與後台驗證並取得token
//                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
//                startActivity(intent);
//                finish();
                break;
            case R.id.tv_register: //註冊
                startActivity(new Intent(getBaseContext(), PrivacyActivity.class)); //隱私權page
                break;
            case R.id.tv_forget:  //忘記密碼
                dialogForget();
                break;
        }
    }

    //登入時與後台驗證並取得token
    private void userLoginApi() {

        if (TextUtils.isEmpty(account.getText().toString()) || TextUtils.isEmpty(password.getText().toString()))
            return;

        String loginAccount = account.getText().toString();
        String loginPassword = password.getText().toString();

        JSONObject json = new JSONObject();
        try {
            json.put("account", loginAccount);
            json.put("password", loginPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildLogin(LOGIN, json.toString(), loginListener);
    }

    private ApiProxy.OnApiListener loginListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
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

    //忘記密碼fxn
    private void dialogForget() {
        AlertDialog.Builder alertDialogForget = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View mView = layoutInflater.inflate(R.layout.dialog_forget_password, null);
        alertDialogForget.setView(mView);

        EditText editAccount = mView.findViewById(R.id.etAccount);
        alertDialogForget.setCancelable(false)
                .setPositiveButton("送出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                            dialogInterface.dismiss();
                    }
                });

        AlertDialog dialog = alertDialogForget.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Boolean wantToCloseDialog = (editAccount.getText().toString().trim().isEmpty());
                if (wantToCloseDialog){
                    Toast.makeText(getApplicationContext(), getString(R.string.account_is_not_empty), Toast.LENGTH_SHORT).show();
                }else{
                    String accountStr = editAccount.getText().toString().trim();
                    forgetPasswordApi(accountStr); //傳給後台去處理
                    dialog.dismiss();
                }
            }
        });
    }

    private void forgetPasswordApi(String accountStr) {
        Log.d(TAG, "forgetPasswordApi: " + accountStr);
        JSONObject json = new JSONObject();
        try {
            json.put("account", accountStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //proxy.buildPOST(FORGET_PASSWORD, json.toString(), forgetListener);
    }

    private ApiProxy.OnApiListener forgetListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            Log.d(TAG, "onSuccess: " + result.toString());
        }

        @Override
        public void onFailure(String message) {

        }

        @Override
        public void onPostExecute() {

        }
    };


    public static boolean isValidAccount(String account){
        String regEx = "[^a-zA-Z0-9]";  //只能輸入字母或數字
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regEx);
        java.util.regex.Matcher matcher = pattern.matcher(account);
        return  matcher.matches();
    }

}
