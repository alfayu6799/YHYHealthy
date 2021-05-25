package com.example.yhyhealthydemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import com.example.yhyhealthydemo.tools.ProgressDialogUtil;
import org.json.JSONException;
import org.json.JSONObject;
import es.dmoral.toasty.Toasty;
import static com.example.yhyhealthydemo.module.ApiProxy.COMP;
import static com.example.yhyhealthydemo.module.ApiProxy.LOGIN;
import static com.example.yhyhealthydemo.module.ApiProxy.maritalSetting;
import static com.example.yhyhealthydemo.module.ApiProxy.menstrualSetting;
import static com.example.yhyhealthydemo.module.ApiProxy.userSetting;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    Button loginButton;
    EditText account, password;
    TextView register, forget;

    //api
    ApiProxy proxy;

    String regEx = "[^a-zA-Z0-9]";  //只能輸入字母或數字

    private ProgressDialog progressDialog;

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
        //暫時
        account.setText("demo21");
        password.setText("123456");
        
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

    @SuppressLint("NonConstantResourceId")
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
                startActivity(new Intent(getBaseContext(), ForgetPassActivity.class));
                break;
        }
    }

    //登入時與後台驗證並取得token
    private void userLoginApi() {

        //帳號和密碼不得空白
        if (TextUtils.isEmpty(account.getText().toString()) || TextUtils.isEmpty(password.getText().toString()))
            return;

        //登入時需傳給後台:帳戶&密碼
        JSONObject json = new JSONObject();
        try {
            json.put("account", account.getText().toString());
            json.put("password", password.getText().toString());
            json.put("sysId","6");
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
                    parser(result); //解析後台回傳的資訊
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

    //解析後台回傳的資訊
    private void parser(JSONObject result) {
        Log.d(TAG, "Login parser: " + result.toString());
        try {
            JSONObject object = new JSONObject(result.toString());
            int errorCode = object.getInt("errorCode");
            if(errorCode == 1) { //無此帳號或密碼錯誤...
                Toasty.error(LoginActivity.this, getString(R.string.account_is_error), Toast.LENGTH_SHORT, true).show();
            }else if(errorCode == 6){
                Toasty.error(LoginActivity.this, getString(R.string.account_is_no_data), Toast.LENGTH_SHORT, true).show();
            }else if (errorCode == 34) { //尚未開通帳戶
                showCompInfo();  //驗證碼輸入Dialog
            }else if (errorCode == 0){ //登入成功
                //因為success內容有三個重要資訊其排卵功能需要用到所以要解析json
                JSONObject success = object.getJSONObject("success");
                //boolean maritalSet = success.getBoolean("maritalSet");
                maritalSetting = success.getBoolean("maritalSet");
                //boolean menstrualSet = success.getBoolean("menstrualSet");
                menstrualSetting = success.getBoolean("menstrualSet");
                //boolean userSet = success.getBoolean("userSet");
                userSetting = success.getBoolean("userSet");
//                SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
//                pref.edit().putString("ACCOUNT", account.getText().toString())
//                        .putString("PASSWORD", password.getText().toString())
//                        .putBoolean("MARRIAGE", maritalSet)
//                        .putBoolean("MENSTRUAL", menstrualSet)
//                        .putBoolean("USERSET", userSet).apply();

                Toasty.success(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT, true).show();

                //導至首頁
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else {
                Toasty.error(LoginActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //驗證碼輸入
    private void showCompInfo() {
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
                    Toasty.error(LoginActivity.this, getString(R.string.compcode_is_not_empty), Toast.LENGTH_SHORT, true).show();
                    return;
                }
                //傳至後台驗證
                checkComp(editText);
            }
        });
        AlertDialog compDialog = builder.create();
        compDialog.show();
    }

    //傳至後台驗證
    private void checkComp(EditText editText) {
        JSONObject json = new JSONObject();
        try {
            json.put("account", account.getText().toString());
            json.put("verCode", editText.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildInit(COMP, json.toString(), verificationListener);
    }

    private ApiProxy.OnApiListener verificationListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(LoginActivity.this, getString(R.string.title_process), getString(R.string.process), true);
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
                        if(errorCode == 5){  //驗證碼不對
                            Toasty.error(LoginActivity.this, getString(R.string.comp_code_error), Toast.LENGTH_SHORT, true).show();
                        }else if (errorCode == 0){ //驗證成功
                            Toasty.success(LoginActivity.this, getString(R.string.access_success), Toast.LENGTH_SHORT, true).show();
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
            //隱藏對話方塊
            progressDialog.dismiss();
        }
    };
}
