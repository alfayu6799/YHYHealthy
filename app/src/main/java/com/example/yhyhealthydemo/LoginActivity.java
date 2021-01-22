package com.example.yhyhealthydemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthydemo.module.ApiProxy;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

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

        proxy = ApiProxy.getInstance();

        initView();
    }

    private void initView() {
        account = findViewById(R.id.et_account);
        password = findViewById(R.id.et_password);
        register = findViewById(R.id.tv_register);
        forget = findViewById(R.id.tv_forget);
        loginButton = findViewById(R.id.bt_login);

        loginButton.setOnClickListener(this);
        forget.setOnClickListener(this);
        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_login:
//                userLoginApi(); //跟後台要token
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.tv_register: //註冊
                startActivity(new Intent(getBaseContext(), PrivacyActivity.class)); //隱私權page
                break;
            case R.id.tv_forget:  //忘記密碼
                dialogForget();
                break;
        }
    }

    private void userLoginApi() {

        JSONObject json = new JSONObject();
        try {
            json.put("account", "demo05");
            json.put("password", "111111");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.build(LOGIN, json.toString(), loginListener);
    }

    private ApiProxy.OnApiListener loginListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            Log.d(TAG, "onSuccess: " + result.toString());
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

        EditText email = mView.findViewById(R.id.et_email);
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
                String emailStr = email.getText().toString();
                Boolean wantToCloseDialog = (email.getText().toString().trim().isEmpty());
                if (wantToCloseDialog){
                    Toast.makeText(LoginActivity.this, "信箱帳號不得為空", Toast.LENGTH_SHORT).show();
                }else{
//                    forgetPasswordApi(); //傳給後台去做發送確認信函給使用者
                    Toast.makeText(LoginActivity.this, "確認函已經發送至信箱 : " + emailStr, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }

    public static boolean isValidAccount(String account){
        String regEx = "[^a-zA-Z0-9]";  //只能輸入字母或數字
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regEx);
        java.util.regex.Matcher matcher = pattern.matcher(account);
        return  matcher.matches();
    }

    //check email is Valid
    public static boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
