package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthydemo.datebase.ChangeUserPeriodApi;
import com.example.yhyhealthydemo.datebase.PeriodData;
import com.example.yhyhealthydemo.module.ApiProxy;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthydemo.module.ApiProxy.MENSTRUAL_RECORD_INFO;
import static com.example.yhyhealthydemo.module.ApiProxy.MENSTRUAL_RECORD_UPDATE;

/**
 * 使用者設定 - 經期設定
 * */

public class UserPeriodActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UserPeriodActivity";

    ImageView back;
    TextView  periodLength;
    EditText  cycleLength;
    TextView  firstDay, endDay;
    Button    save;

    //api
    ApiProxy proxy;
    PeriodData period;

    //進度條
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menstruation_setting);

        initView();
        initData();
    }

    private void initView() {
        back = findViewById(R.id.ivBackUserSetting);
        cycleLength = findViewById(R.id.edtCycleLength);        //週期長度
        periodLength = findViewById(R.id.tvPeriodLength);       //經期長度
        firstDay = findViewById(R.id.tvDateStart);              //起始日
        firstDay.addTextChangedListener(lastWatch);
        endDay = findViewById(R.id.tvDateEnd);                   //結束日
        endDay.addTextChangedListener(endWatch);
        save = findViewById(R.id.btnSaveToApi3);

        firstDay.setOnClickListener(this);
        endDay.setOnClickListener(this);
        back.setOnClickListener(this);
        save.setOnClickListener(this);
    }

    private void initData() { //查詢
        proxy = ApiProxy.getInstance();

        proxy.buildPOST(MENSTRUAL_RECORD_INFO, "", periodListener);
    }

    private ApiProxy.OnApiListener periodListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            //顯示對話方塊
            if(progressDialog == null) {
                progressDialog = ProgressDialog.show(UserPeriodActivity.this, getString(R.string.title_process), getString(R.string.process), true);
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
                        if (errorCode == 0){
                            parserJson(result); //解析後台來的資料
                        }else if(errorCode == 6){  //新人沒有資料
                            setInit();
                        }else if (errorCode == 23){ //token失效
                            Toasty.error(UserPeriodActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(UserPeriodActivity.this, LoginActivity.class)); //重新登入
                            finish();
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

    private void setInit() {
        String today = String.valueOf(LocalDate.now()); //today
        cycleLength.setText("");
        periodLength.setText("");
        firstDay.setText(today);
        endDay.setText(today);
    }

    //解析後台來的資料
    private void parserJson(JSONObject result) {
        Log.d(TAG, "經期設定JSON解析: " + result.toString());

        period = PeriodData.newInstance(result.toString());

        //週期長度
        String periodSize = String.valueOf(period.getSuccess().getCycle());
        cycleLength.setText(periodSize);
        cycleLength.setSelection(periodSize.length()); //游標出現在字尾

        //經期長度
        String cycleSize = String.valueOf(period.getSuccess().getPeriod());
        periodLength.setText(cycleSize);

        //開始時間
        String startDay = period.getSuccess().getLastDate();
        firstDay.setText(startDay);

        //結束時間
        String endingDay = period.getSuccess().getEndDate();
        endDay.setText(endingDay);

    }

    //
    private TextWatcher lastWatch = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                //不得選擇未來日期
            checkDayRange(firstDay.getText().toString());

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    //檢查使用者輸入的日期是否是未來
    private void checkDayRange(String days) {
        Calendar mCalendar = Calendar.getInstance();
        CharSequence sequence = DateFormat.format("yyyy-MM-dd", mCalendar.getTime());
        DateTime today = new DateTime(sequence);        //今天
        DateTime last_day = new DateTime(days); //使用者選擇的日期
        if(today.isBefore(last_day)){
            Toasty.error(UserPeriodActivity.this, getString(R.string.days_is_not_allow_tomorrow), Toast.LENGTH_SHORT, true).show();
            periodLength.setText("");
        }
    }

    //
    private TextWatcher endWatch = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
            calculate();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackUserSetting:
                finish();
                break;
            case R.id.tvDateStart: //起始日
                dialogPickDate();
                break;
            case R.id.tvDateEnd:  //結束日
                dialogPikeDate2();
                break;
            case R.id.btnSaveToApi3://儲存
                checkBeforeUpdate();
                break;
        }
    }

    //計算經期長度
    private void calculate() {
        //不得空白
        if(TextUtils.isEmpty(firstDay.getText().toString()) || TextUtils.isEmpty(endDay.getText().toString()))
            return;

        Calendar mCalendar = Calendar.getInstance();
        CharSequence sequence = DateFormat.format("yyyy-MM-dd", mCalendar.getTime());
        DateTime today = new DateTime(sequence);

        //使用第三方套件:Joda-Time
        DateTime d1 = new DateTime(firstDay.getText().toString()); //起始日
        DateTime d2 = new DateTime(endDay.getText().toString());   //結束日

        if (d2.isBefore(d1)){ //結束日不得小於起始日
            Toasty.error(UserPeriodActivity.this, getString(R.string.endday_is_not_before_lastday), Toast.LENGTH_SHORT, true).show();
            return;
        }

        //不得選擇未來日期
        if(today.isBefore(d1) || today.isBefore(d2)){
            Toasty.error(UserPeriodActivity.this, getString(R.string.days_is_not_allow_tomorrow), Toast.LENGTH_SHORT, true).show();
            periodLength.setText("");
            return;
        }

        int days = Days.daysBetween(d1,d2).getDays() + 1 ;
        periodLength.setText(String.valueOf(days));
    }

    //上傳前檢查欄位是否都有填寫
    private void checkBeforeUpdate() {

        //判斷上次經期開始日是否有填寫
        if(TextUtils.isEmpty(firstDay.getText().toString())){
            Toasty.error(UserPeriodActivity.this, getString(R.string.start_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        //判斷上次經期結束日是否有填寫
        if(TextUtils.isEmpty(endDay.getText().toString())){
            Toasty.error(UserPeriodActivity.this, getString(R.string.end_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        //判斷週期是否有填寫
        if(TextUtils.isEmpty(cycleLength.getText().toString())){
            Toasty.error(UserPeriodActivity.this, getString(R.string.cycle_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        if(TextUtils.isEmpty(periodLength.getText().toString())){
            Toasty.error(UserPeriodActivity.this, getString(R.string.period_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        //計算
        calculate();

        //上傳更新資料
        updateToApi();
    }

    //後台更新資料
    private void updateToApi() {
        JSONObject json = new JSONObject();
        try {
            json.put("cycle", cycleLength.getText().toString());
            json.put("period",periodLength.getText().toString());
            json.put("lastDate",firstDay.getText().toString());
            json.put("endDate", endDay.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //執行後台更新
        proxy.buildPOST(MENSTRUAL_RECORD_UPDATE, json.toString(), changePeriodListener);
    }

    private ApiProxy.OnApiListener changePeriodListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            //顯示對話方塊
            if(progressDialog == null) {
                progressDialog = ProgressDialog.show(UserPeriodActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }
            if (!progressDialog.isShowing()) progressDialog.show();
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(result.toString());
                        int errorCode = jsonObject.getInt("errorCode");
                        if (errorCode == 0){
                            Toasty.success(UserPeriodActivity.this, getString(R.string.update_to_Api_is_success), Toast.LENGTH_SHORT, true).show();
                            writeToSharedPreferences();
                        }else if (errorCode == 23){ //token失效 2021/05/11
                            Toasty.error(UserPeriodActivity.this, getString(R.string.update_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(UserPeriodActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
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

    //經期設定寫到SharedPreferences
    private void writeToSharedPreferences() {
        SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
        pref.edit().putString("BEGIN", firstDay.getText().toString()).apply();
        pref.edit().putInt("PERIOD", Integer.parseInt(periodLength.getText().toString())).apply();
        pref.edit().putInt("CYCLE" , Integer.parseInt(cycleLength.getText().toString())).apply();
        pref.edit().putBoolean("MENSTRUAL", true).apply();
    }

    //日期彈跳視窗
    private void dialogPickDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        DatePickerDialog pickerDialog = new DatePickerDialog(UserPeriodActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                Calendar start = Calendar.getInstance();
                start.set(year, month, dayOfMonth);
                firstDay.setText(df.format(start.getTime()));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        pickerDialog.show();
    }

    //日期彈跳視窗
    private void dialogPikeDate2(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        DatePickerDialog pickerDialog = new DatePickerDialog(UserPeriodActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                Calendar start = Calendar.getInstance();
                start.set(year, month, dayOfMonth);
                endDay.setText(df.format(start.getTime()));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        pickerDialog.show();
    }
}