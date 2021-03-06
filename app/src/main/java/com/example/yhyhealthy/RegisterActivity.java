package com.example.yhyhealthy;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Region;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.example.yhyhealthy.module.ApiProxy;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;
import pl.droidsonroids.gif.GifImageView;

import static com.example.yhyhealthy.module.ApiProxy.COMP;
import static com.example.yhyhealthy.module.ApiProxy.REGISTER;

/**** ************
 * 註冊功能
 * api需要info:帳號,密碼,信箱,國際區碼(CN),手機號碼(CN)
 * 流程 : 開通帳號需要有驗證碼
 * 開通後會將帳號與密碼存入手機方便給自動登入用
 * create 2021/01/06
 * * * * * ** ***********/

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Button btnRegister;
    private EditText account, password, email;
    private TextView txtAreaCode;
    private EditText edtMobile;
    private TextInputLayout emailLayout, mobileLayout;

    private RadioGroup registerGroup;
    private String verificationStyle = "email";
    private String emailPattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
    private String InterCode = "";

    //api
    ApiProxy proxy;

    private ProgressDialog progressDialog;

    //背景動畫
    private GifImageView gifImageView;

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

        //動畫background
        gifImageView = findViewById(R.id.game_gif);
        gifImageView.setBackgroundResource(R.mipmap.yhy_new_background);

        emailLayout = findViewById(R.id.EmailLayout);
        mobileLayout = findViewById(R.id.MobileLayout);
        email = findViewById(R.id.edtEmailInput);
        edtMobile = findViewById(R.id.edMobile);

        txtAreaCode = findViewById(R.id.tvTelCode);
        txtAreaCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //國際編碼採用彈跳視窗
                showCodeDialog();
            }
        });

        //使用信箱或簡訊的方法註冊(RadioButton)
        registerGroup = findViewById(R.id.rdGroupRegister);
        registerGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rdoBtnEmail){          //信箱是default(在xml設定// )
                    emailLayout.setVisibility(View.VISIBLE);
                    txtAreaCode.setVisibility(View.GONE);
                    mobileLayout.setVisibility(View.GONE);
                    verificationStyle = "email";
                }else{
                    verificationStyle = "phone";
                    emailLayout.setVisibility(View.GONE);
                    txtAreaCode.setVisibility(View.VISIBLE);
                    mobileLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        //註冊Onclick
        btnRegister = findViewById(R.id.btnRegisterSend);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //帳號與密碼不得為空
                if (TextUtils.isEmpty(account.getText().toString()) || TextUtils.isEmpty(password.getText().toString())){
                    Toasty.error(RegisterActivity.this, getString(R.string.account_is_not_allow_empty), Toast.LENGTH_SHORT, true).show();
                    return;
                }

                //帳號和密碼輸入不得少於6
                if(account.getText().toString().trim().length() < 6 || password.getText().toString().trim().length() < 6) {
                    Toasty.error(RegisterActivity.this, getString(R.string.number_less_six), Toast.LENGTH_SHORT, true).show();
                    return;
                }

                //採用mail驗證方式
                if(verificationStyle.equals("email")){
                    if(TextUtils.isEmpty(email.getText().toString())){
                        Toasty.error(RegisterActivity.this, getString(R.string.please_input_email), Toast.LENGTH_SHORT, true).show();
                        return;
                    }else {
                        if (email.getText().toString().trim().matches(emailPattern)){ //有效的mail address
                            //寫回後台
                            upDataToApi();
                        }else {
                            Toasty.error(RegisterActivity.this, getString(R.string.please_input_valid_email), Toast.LENGTH_SHORT, true).show();
                        }
                    }
                } //end of verificationStyle is email

                //採用簡訊驗證方式
                if(verificationStyle.equals("phone")){
                    if(TextUtils.isEmpty(InterCode) || TextUtils.isEmpty(edtMobile.getText().toString())){
                        Toasty.error(RegisterActivity.this, getString(R.string.please_input_phone), Toast.LENGTH_SHORT, true).show();
                        return;
                    }else {
                        //寫回後台
                        upDataToApi();
                    }
                }
            }
        });
    }

    //國際編碼彈跳視窗 2021/07/23變更
    private void showCodeDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.chose_phone_code));
        String[] areaCodeItems = { getString(R.string.china) , getString(R.string.taiwan)};
        int itemChecked = -1; //default:都不選
        alertDialog.setSingleChoiceItems(areaCodeItems, itemChecked, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:  //中國
                        txtAreaCode.setText(R.string.china);
                        InterCode = "CN";
                        dialog.dismiss();
                        break;
                    case 1:  //台灣
                        txtAreaCode.setText(R.string.taiwan);
                        InterCode = "TW";
                        dialog.dismiss();
                        break;
                }
            }
        });
        AlertDialog dialog = alertDialog.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    //將資料寫回後台
    private void upDataToApi() {
        //取得手機語系
        String language = getResources().getConfiguration().locale.getLanguage(); //語系
        String country =  getResources().getConfiguration().locale.getCountry(); //國家代碼
        String defaultLen = language + "-" + country;  //ex:zh-TW, zh-CN

        String accountNo = account.getText().toString().trim();
        String passWD = password.getText().toString().trim();
        String mailAddress = email.getText().toString().trim();
        String phoneNo = edtMobile.getText().toString().trim();

        JSONObject json = new JSONObject();
        try {
            json.put("account", accountNo);
            json.put("password", passWD);
            json.put("email", mailAddress);
            json.put("telCode", InterCode);
            json.put("mobile", phoneNo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //註冊專用(須帶手機語系defaultLen)
        proxy.buildRegister(REGISTER, json.toString(), defaultLen, registerListener);
    }

    private ApiProxy.OnApiListener registerListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            //顯示對話方塊
            if(progressDialog == null) {
                progressDialog = ProgressDialog.show(RegisterActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }
            if (!progressDialog.isShowing()) progressDialog.show();
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parserJson(result); //需要解析後台回復的資訊
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

    //解析後台回的資料
    private void parserJson(JSONObject result) {
        try {
            JSONObject object = new JSONObject(result.toString());
            int errorCode = object.getInt("errorCode");
            if(errorCode == 0){ //註冊成功後台會回覆是否需開通code
                JSONObject success = object.getJSONObject("success");
                int code = success.getInt("statusCode");
                if(code == 1){ //尚未開通
                    Toasty.success(RegisterActivity.this, getString(R.string.register_success), Toast.LENGTH_SHORT, true).show();

                    //2021/07/08 授權碼dialog
                    dialogComp();

                }else if (code == 2) { //已開通
                    finish(); //關閉並回到登入頁面
                }
            }else if (errorCode == 2){  //帳號已經存在
                Toasty.error(RegisterActivity.this, getString(R.string.account_has_already), Toast.LENGTH_SHORT, true).show();
            }else if (errorCode == 28){ //信箱已存在
                Toasty.error(RegisterActivity.this, getString(R.string.email_already_existed), Toast.LENGTH_SHORT, true).show();
            }else {
                Toasty.error(RegisterActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //授權碼dialog
    private void dialogComp() {
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
                    Toasty.error(RegisterActivity.this, getString(R.string.compcode_is_not_empty), Toast.LENGTH_SHORT, true).show();
                    return;
                }
                //傳至後台驗證
                checkComp(editText);
            }
        });
        AlertDialog compDialog = builder.create();
        compDialog.show();
        compDialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);  //Button文字小寫顯示
    }

    //傳至後台驗證授權碼
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
            //顯示對話方塊
            if(progressDialog == null) {
                progressDialog = ProgressDialog.show(RegisterActivity.this, getString(R.string.title_process), getString(R.string.process), true);
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
                            Toasty.error(RegisterActivity.this, getString(R.string.comp_code_error), Toast.LENGTH_SHORT, true).show();

                            //再次輸入驗證碼
                            dialogComp();

                        }else if (errorCode == 0){ //驗證成功
                            Toasty.success(RegisterActivity.this, getString(R.string.access_success), Toast.LENGTH_SHORT, true).show();
                            //將帳號及密碼存到sharePref:自動登入使用 2021/07/08
                            writeUserAccount();

                        }else {
                            Toasty.error(RegisterActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //使用者帳號&密碼寫入手機
    private void writeUserAccount() {
        SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);

        pref.edit().putString("PASSWORD", password.getText().toString())
                .putString("ACCOUNT", account.getText().toString())
                .apply();

        //導引至登入頁面並自動登入
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
