package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.example.yhyhealthydemo.module.ApiProxy;
import com.example.yhyhealthydemo.tools.ProgressDialogUtil;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthydemo.module.ApiProxy.COMP;
import static com.example.yhyhealthydemo.module.ApiProxy.REGISTER;

/**** ************
 * 註冊功能
 * api需要info:帳號,密碼,信箱,國際區碼,手機號碼
 * 流程 : 開通帳號需要有驗證碼
 * * * * * ** ***********/

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Button btnRegister;
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

        //使用信箱或簡訊的方法註冊(RadioButton)
        registerGroup = findViewById(R.id.rdGroupRegister);
        registerGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rdoBtnEmail){  //信箱是default(在xml設定// )
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

        //註冊Onclick
        btnRegister = findViewById(R.id.btnRegisterSend);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //帳號與密碼不得為空
                if (TextUtils.isEmpty(account.getText().toString()) || TextUtils.isEmpty(password.getText().toString()))
                         return;

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
                            Toasty.error(RegisterActivity.this, getString(R.string.please_input_vaild_email), Toast.LENGTH_SHORT, true).show();
                        }
                    }
                } //end of verificationStyle is email

                //採用簡訊驗證方式
                if(verificationStyle.equals("phone")){
                    if(TextUtils.isEmpty(edtTelCode.getText().toString()) || TextUtils.isEmpty(edtMobile.getText().toString())){
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

    //將資料寫回後台
    private void upDataToApi() {
        //取得手機語系
        String language = getResources().getConfiguration().locale.getLanguage(); //語系
        String country =  getResources().getConfiguration().locale.getCountry(); //國家代碼
        String defaultLen = language + "-" + country;  //ex:zh-TW, zh-CN

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

        //註冊專用(須帶手機語系defaultLen)
        proxy.buildRegister(REGISTER, json.toString(), defaultLen, registerListener);
    }

    private ApiProxy.OnApiListener registerListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            //顯示對話方塊
            ProgressDialogUtil.showProgressDialog(RegisterActivity.this);
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
            ProgressDialogUtil.dismiss();
//            if(progressDialog != null)
//                progressDialog.dismiss();
        }
    };

    //解析後台回的資料
    private void parserJson(JSONObject result) {
        Log.d(TAG, "註冊完成後台回覆: " + result.toString());
        try {
            JSONObject object = new JSONObject(result.toString());
            int errorCode = object.getInt("errorCode");
            if(errorCode == 0){ //註冊成功後台會回覆是否需開通code
                JSONObject success = object.getJSONObject("success");
                int code = success.getInt("statusCode");
                if(code == 1){ //未開通
                    Toasty.success(RegisterActivity.this, getString(R.string.register_success), Toast.LENGTH_SHORT, true).show();
                    finish();
                }else if (code == 2) { //已開通
                    finish(); //關閉並回到登入頁面
                }
            }else if (errorCode == 2){  //帳號已經存在
                Toasty.error(RegisterActivity.this, getString(R.string.account_has_already), Toast.LENGTH_SHORT, true).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
