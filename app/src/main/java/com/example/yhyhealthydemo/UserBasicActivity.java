package com.example.yhyhealthydemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthydemo.datebase.UsersData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

/***** ****
 * 設定 - 個人設定 - 基本資料
 * * ***** *****/
public class UserBasicActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UserBasicActivity";

    TextView accountInfo;
    TextView genderInfo, birthday, BMIValue;
    EditText accountName , accountMail, areaCode, phoneNo, bodyHeight, bodyWeight;
    ImageView back;
    Button buttonSave;

    //api
    UsersData usersData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_basic);

        usersData = new UsersData();

        initData();
        initView();
    }

    //get data from api
    private void initData() {
        String myJSONStr = loadJSONFromAsset("member.json");
        try {
            JSONObject obj = new JSONObject(myJSONStr);
            usersData = UsersData.newInstance(obj.toString());

//            Log.d(TAG, "initData: " + usersData.toJSONString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        back = findViewById(R.id.ivBackSetting6);
        buttonSave = findViewById(R.id.btnSaveToApi);
        accountInfo = findViewById(R.id.textUserAccount);   //帳號不得變更
        accountInfo.setText(usersData.getSuccess().getUserAccount());
        accountName = findViewById(R.id.edtChangeName);     //變更名稱
        accountName.setInputType(InputType.TYPE_NULL); //hide keyboard
        accountName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                accountName.setInputType(InputType.TYPE_CLASS_TEXT);
                accountName.onTouchEvent(event);
                return false;
            }
        });

        genderInfo = findViewById(R.id.textGender);        //性別
        accountMail = findViewById(R.id.edtEmailAddress);  //信箱
        birthday = findViewById(R.id.textBornDay);         //生日
        areaCode = findViewById(R.id.edtAreaCode);         //國際區碼
        phoneNo = findViewById(R.id.edtPhoneNumber);       //電話號碼
        bodyHeight = findViewById(R.id.edtHeight);         //身高
        bodyWeight = findViewById(R.id.editWeight);        //體重
        BMIValue = findViewById(R.id.textBMI);             //BMI

        back.setOnClickListener(this);
        buttonSave.setOnClickListener(this);
        genderInfo.setOnClickListener(this);              //性別onclick
        birthday.setOnClickListener(this);                //生日onclick
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackSetting6:
                finish();
                break;
            case R.id.textGender:
                dialogGender(); //性別採用彈跳視窗
                break;
            case R.id.textBornDay:
                dialogPickBirthday();
                break;
            case R.id.btnSaveToApi:
                updateToApi();
                break;
        }
    }

    //傳至後台
    private void updateToApi() {
        String nickName = accountName.getText().toString();
        usersData.getSuccess().setName(nickName);
        Log.d(TAG, "updateToApi: " + usersData.toJSONString());
        finish();
    }

    //出生年月日彈跳視窗選擇
    private void dialogPickBirthday() {
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR) - 12;
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog pickerDialog = new DatePickerDialog(UserBasicActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (year <= mYear) {
                    // 完成選擇，顯示日期
                    birthday.setText(mDateTimeFormat(year) + "-" + mDateTimeFormat(monthOfYear + 1) + "-" + mDateTimeFormat(dayOfMonth));
                    usersData.getSuccess().setBirthday(birthday.getText().toString());
                } else {
                    Toast.makeText(UserBasicActivity.this, getString(R.string.set_years_range), Toast.LENGTH_LONG).show();
                }
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
                        usersData.getSuccess().setGender("F");
                        dialog.dismiss();
                        break;
                    case 1: //男性
                        genderInfo.setText(getString(R.string.male));
                        usersData.getSuccess().setGender("M");
                        dialog.dismiss();
                        break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    //讀取local json file
    public String loadJSONFromAsset(String fileName)
    {
        String json;
        try
        {
            InputStream is = getApplicationContext().getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex)
        {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}