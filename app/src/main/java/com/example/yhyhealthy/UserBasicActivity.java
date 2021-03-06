package com.example.yhyhealthy;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.yhyhealthy.datebase.ChangeUserBasicInfoApi;
import com.example.yhyhealthy.datebase.UsersData;
import com.example.yhyhealthy.module.ApiProxy;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;
import es.dmoral.toasty.Toasty;
import pl.droidsonroids.gif.GifImageView;

import static com.example.yhyhealthy.module.ApiProxy.RENEW_TOKEN;
import static com.example.yhyhealthy.module.ApiProxy.USER_INFO;
import static com.example.yhyhealthy.module.ApiProxy.USER_UPDATE;
import static com.example.yhyhealthy.module.ApiProxy.maritalSetting;
import static com.example.yhyhealthy.module.ApiProxy.menstrualSetting;
import static com.example.yhyhealthy.module.ApiProxy.userSetting;

/***** ****
 * 設定 - 個人設定 - 基本資料
 * * ***** *****/
public class UserBasicActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UserBasicActivity";

    TextView accountInfo;
    TextView genderInfo, birthday, BMIValue, areaCode;
    EditText accountName , accountMail, phoneNo, bodyHeight, bodyWeight;
    ImageView back;
    Button buttonSave;

    //api
    UsersData usersData;
    ApiProxy proxy;
    //使用者的基本資料全塞入此物件
    ChangeUserBasicInfoApi changeUserBasicInfoApi;

    //
    private ProgressDialog progressDialog;

    //背景動畫
    private GifImageView gifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_basic);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //禁止旋轉
        //不讓虛擬鍵盤蓋文
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        usersData = new UsersData();

        initView();

        initData();
    }

    //get data from api
    private void initData() {
        proxy = ApiProxy.getInstance();
        //修改使用者基本資料API
        changeUserBasicInfoApi = new ChangeUserBasicInfoApi();
        //取得使用者基本資料API
        proxy.buildPOST(USER_INFO, "", userInfoListener);
    }

    private ApiProxy.OnApiListener userInfoListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(UserBasicActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            parserJson(result); //解析後台來的資料
                        }else if (errorCode == 23) { //token失效 2021/05/11
                            Toasty.error(UserBasicActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(UserBasicActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(UserBasicActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(UserBasicActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(UserBasicActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    private void parserJson(JSONObject result) {
        usersData = UsersData.newInstance(result.toString());
        Log.d(TAG, "parserJson:  " + result.toString());

        //帳號
        accountInfo.setText(usersData.getSuccess().getUserAccount());

        //名稱
        accountName.setText(usersData.getSuccess().getName());

        //性別
        if(usersData.getSuccess().getGender().equals("F")){
            genderInfo.setText(getString(R.string.female));  //女性
            changeUserBasicInfoApi.setGender("F");
        }else{
            genderInfo.setText(getString(R.string.male));   //男性
            changeUserBasicInfoApi.setGender("M");
        }

        //信箱
        accountMail.setText(usersData.getSuccess().getEmail());

        //生日
        birthday.setText(usersData.getSuccess().getBirthday());
        changeUserBasicInfoApi.setBirthday(usersData.getSuccess().getBirthday());

        //國際區碼 2021/07/08
        if (usersData.getSuccess().getTelCode().equals("CN")){
            areaCode.setText(getString(R.string.china));
            changeUserBasicInfoApi.setTelCode("CN");
        }else if (usersData.getSuccess().getTelCode().equals("TW")){
            areaCode.setText(getString(R.string.taiwan));
            changeUserBasicInfoApi.setTelCode("TW");
        }

        //電話號碼
        phoneNo.setText(usersData.getSuccess().getMobile());

        //身高
        bodyHeight.setText(String.valueOf(usersData.getSuccess().getHeight()));
        double height = usersData.getSuccess().getHeight();

        //體重
        bodyWeight.setText(String.valueOf(usersData.getSuccess().getWeight()));
        double weight = usersData.getSuccess().getWeight();
        if((weight == 0) || (height == 0)){
            BMIValue.setText(getString(R.string.please_input_need_height_and_weight));
        }else {
            calculate(height, weight);
        }
    }

    //BMI計算
    private void calculate(double height, double weight) {
        float h = (float)height/100;
        float bmiValue = (float) weight/(h*h);

        BMIValue.setText(String.valueOf(bmiValue));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        back = findViewById(R.id.ivBackSetting6);

        //動畫background
        gifImageView = findViewById(R.id.game_gif);
        gifImageView.setBackgroundResource(R.mipmap.yhy_new_background);

        buttonSave = findViewById(R.id.btnSaveToApi);
        accountInfo = findViewById(R.id.textUserAccount);   //帳號不得變更
        accountName = findViewById(R.id.edtChangeName);     //變更名稱
        accountName.setInputType(InputType.TYPE_NULL);      //hide keyboard
        accountName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                accountName.setInputType(InputType.TYPE_CLASS_TEXT);
                accountName.onTouchEvent(event);
                return false;
            }
        });

        genderInfo = findViewById(R.id.textGender);         //性別
        accountMail = findViewById(R.id.edtEmailAddress);   //信箱
        birthday = findViewById(R.id.textBornDay);          //生日
        areaCode = findViewById(R.id.tv_area_code);         //國際區碼
        phoneNo = findViewById(R.id.edtPhoneNumber);        //電話號碼
        bodyHeight = findViewById(R.id.edtHeight);          //身高
        bodyWeight = findViewById(R.id.editWeight);         //體重
        bodyWeight.addTextChangedListener(weightWatcher);   //體重Listener
        BMIValue = findViewById(R.id.textBMI);              //BMI

        back.setOnClickListener(this);
        buttonSave.setOnClickListener(this);
        genderInfo.setOnClickListener(this);              //性別onclick
        birthday.setOnClickListener(this);                //生日onclick
        areaCode.setOnClickListener(this);
    }

    //體重Listener
    private TextWatcher weightWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            if (!bodyWeight.getText().toString().isEmpty()) {
                double w = Double.parseDouble(bodyWeight.getText().toString()); //體重
                double h = Double.parseDouble(bodyHeight.getText().toString()); //身高
                calculate(h, w);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            Log.d(TAG, "afterTextChanged: " + editable);
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackSetting6:
                finish(); //回到上一頁
                break;
            case R.id.textGender:
                dialogGender(); //性別採用彈跳視窗
                break;
            case R.id.textBornDay:
                dialogPickBirthday();
                break;
            case R.id.tv_area_code:  //國碼選擇
                dialogAreaCode();    //彈跳視窗
                break;
            case R.id.btnSaveToApi:
                checkBeforeUpdate(); //儲存
                break;
        }
    }

    //國碼選擇
    private void dialogAreaCode() {
        AlertDialog.Builder alertBuild = new AlertDialog.Builder(this);
        alertBuild.setTitle(getString(R.string.please_chose_area_code));
        String[] areaCodeItems = { getString(R.string.china) , getString(R.string.taiwan)};
        int itemChecked = -1;
        alertBuild.setSingleChoiceItems(areaCodeItems, itemChecked, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch ((which)){
                    case 0:
                        areaCode.setText(getString(R.string.china));
                        changeUserBasicInfoApi.setTelCode("CN");
                        dialog.dismiss();
                        break;
                    case 1:
                        areaCode.setText(getString(R.string.taiwan));
                        changeUserBasicInfoApi.setTelCode("TW");
                        dialog.dismiss();
                        break;
                }
            }
        });
        AlertDialog alertDialog = alertBuild.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    //更新前要先檢查資料是否齊全
    private void checkBeforeUpdate() {
        //判斷名稱是否有填寫
        if(TextUtils.isEmpty(accountName.getText().toString())){
            Toasty.error(UserBasicActivity.this, getString(R.string.name_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        //判斷信箱是否有填寫
        if(TextUtils.isEmpty(accountMail.getText().toString())){
            Toasty.error(UserBasicActivity.this, getString(R.string.email_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        //判斷生日是否有填寫
        if(TextUtils.isEmpty(birthday.getText().toString())){
            Toasty.error(UserBasicActivity.this, getString(R.string.birthday_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        //判斷體重是否空白
        if(TextUtils.isEmpty(bodyWeight.getText().toString())){
            Toasty.error(UserBasicActivity.this, getString(R.string.body_weight_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        //判斷身高是否空白
        if (TextUtils.isEmpty(bodyHeight.getText().toString())){
            Toasty.error(UserBasicActivity.this, getString(R.string.body_height_is_not_empty), Toast.LENGTH_SHORT, true).show();
        }

        updateToApi(); //寫回後端
    }

    //傳至後台
    private void updateToApi() {
        //帳戶
        changeUserBasicInfoApi.setUserAccount(accountInfo.getText().toString());
        //用戶名稱
        changeUserBasicInfoApi.setName(accountName.getText().toString());
        //信箱
        changeUserBasicInfoApi.setEmail(accountMail.getText().toString());
        //國際區碼
        //changeUserBasicInfoApi.setTelCode(areaCode.getText().toString());
        //手機號碼
        changeUserBasicInfoApi.setMobile(phoneNo.getText().toString());
        //身高
        changeUserBasicInfoApi.setHeight(Double.parseDouble(bodyHeight.getText().toString()));
        //體重
        changeUserBasicInfoApi.setWeight(Double.parseDouble(bodyWeight.getText().toString()));

        //上傳到後台
        proxy.buildPOST(USER_UPDATE, changeUserBasicInfoApi.toJSONString(), changeInfoListener);
    }

    private ApiProxy.OnApiListener changeInfoListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(UserBasicActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if(errorCode == 0){
                            Toasty.success(UserBasicActivity.this, getString(R.string.update_to_Api_is_success), Toast.LENGTH_SHORT, true).show();
                            userSetting = true;    //使用者設定
                            //詢問使用者是否要填寫經期資料及婚姻資料 2021/07/08
                            if (!menstrualSetting || !maritalSetting){
                                dialogPeriod();
                            }else {
                                startActivity(new Intent(UserBasicActivity.this, MainActivity.class));
                                finish();  //結束這個頁面
                            }
                        }else if(errorCode == 23){ //token失效
                            Toasty.error(UserBasicActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(UserBasicActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(UserBasicActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(UserBasicActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {  //2021/05/11
                            Log.d(TAG, getString(R.string.json_error_code) + errorCode);
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

    //2021/07/08 增加詢問使用者是否需要使用排卵紀錄
    private void dialogPeriod() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.need_some_info_for_period);
        String[] choseItem = {getString(R.string.on) ,getString(R.string.off)};
        int choseItemClicked = -1;
        builder.setSingleChoiceItems(choseItem, choseItemClicked, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int witch) {
                switch (witch){
                    case 0: //否:主頁面
                        dialog.dismiss();
                        startActivity(new Intent(UserBasicActivity.this, MainActivity.class));
                        finish();
                        break;
                    case 1: //是:導引至經期設定頁面 2021/08/07
                        dialog.dismiss();
                        startActivity(new Intent(UserBasicActivity.this, UserPeriodActivity.class));
                        finish(); //結束這個頁面
                        break;
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    //出生年月日彈跳視窗選擇
    private void dialogPickBirthday() {
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog pickerDialog = new DatePickerDialog(UserBasicActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    birthday.setText(mDateTimeFormat(year) + "-" + mDateTimeFormat(monthOfYear + 1) + "-" + mDateTimeFormat(dayOfMonth));
                    //生日
                    changeUserBasicInfoApi.setBirthday(birthday.getText().toString());
            }
        },mYear, mMonth, mDay);
        pickerDialog.show();
    }

    private String mDateTimeFormat(int value) {
        String RValue = String.valueOf(value);
        if (RValue.length() == 1)
            RValue = "0" + RValue;
        return RValue;
    }

    //性別採用彈跳視窗選擇
    private void dialogGender() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.please_input_gender));
        String[] genderItems = {getString(R.string.female), getString(R.string.male)};
        int checkedItem = 0;
        builder.setSingleChoiceItems(genderItems, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: //女性
                        genderInfo.setText(getString(R.string.female));
                        //性別
                        changeUserBasicInfoApi.setGender("F");
                        dialog.dismiss();
                        break;
                    case 1: //男性
                        genderInfo.setText(getString(R.string.male));
                        //性別
                        changeUserBasicInfoApi.setGender("M");
                        dialog.dismiss();
                        break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    //禁用返回健
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}