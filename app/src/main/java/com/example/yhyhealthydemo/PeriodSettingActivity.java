package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
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
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.example.yhyhealthydemo.module.ApiProxy.MENSTRUAL_RECORD_INFO;
import static com.example.yhyhealthydemo.module.ApiProxy.MENSTRUAL_RECORD_UPDATE;

/**
 * 使用者設定 - 經期設定
 * */

public class PeriodSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PeriodSettingActivity";

    ImageView back;
    TextView  periodLength;
    EditText  cycleLength;
    TextView  firstDay, endDay;
    Button    save;

    //api
    ApiProxy proxy;
    PeriodData period;
    ChangeUserPeriodApi changeUserPeriodApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menstruation_setting);

        initView();
        initData();

    }

    private void initView() {
        back = findViewById(R.id.ivBackUserSetting);
        cycleLength = findViewById(R.id.edtCycleLength);     //週期長度
        periodLength = findViewById(R.id.tvPeriodLength);   //經期長度
        firstDay = findViewById(R.id.tvDateStart);     //起始日
        endDay = findViewById(R.id.tvDateEnd);         //結束日
        save = findViewById(R.id.btnSaveToApi3);

        firstDay.setOnClickListener(this);
        endDay.setOnClickListener(this);
        periodLength.setOnClickListener(this);
        back.setOnClickListener(this);
        save.setOnClickListener(this);
    }

    private void initData() {
        proxy = ApiProxy.getInstance();
        changeUserPeriodApi = new ChangeUserPeriodApi(); //變更經期設定Api

        proxy.buildPOST(MENSTRUAL_RECORD_INFO, "", periodListener);
    }

    private ApiProxy.OnApiListener periodListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int erCode = object.getInt("errorCode");
                        if (erCode == 0){
                            parserJson(result); //解析後台來的資料
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

        }
    };

    //解析後台來的資料
    private void parserJson(JSONObject result) {
        period = PeriodData.newInstance(result.toString());
        Log.d(TAG, "經期設定JSON解析: " + result.toString());
        //週期長度
        String periodSize = String.valueOf(period.getSuccess().getCycle());
        cycleLength.setText(periodSize);

        //經期長度
        String cycleSize = String.valueOf(period.getSuccess().getPeriod());
        periodLength.setText(cycleSize);

        //開始時間
        String startDay = period.getSuccess().getLastDate();
        firstDay.setText(startDay);
        changeUserPeriodApi.setLastDate(startDay); //存到JavaBean

        //結束時間
        String endingDay = period.getSuccess().getEndDate();
        endDay.setText(endingDay);
        changeUserPeriodApi.setEndDate(endingDay); //存到JavaBean
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackUserSetting:
                finish();
                break;
            case R.id.tvDateStart:
                dialogPickDate();
                break;
            case R.id.tvDateEnd:
                dialogPikeDate2();
                break;
            case R.id.tvPeriodLength:
                calculate();  //計算
                break;
            case R.id.btnSaveToApi3://儲存
                checkBeforeUpdate();
                break;
        }
    }

    //計算經期長度
    private void calculate() {
        if(TextUtils.isEmpty(firstDay.getText().toString()) || TextUtils.isEmpty(endDay.getText().toString()))
            return;

        //使用第三方套件:Joda-Time
        DateTime d1 = new DateTime(firstDay.getText().toString());
        DateTime d2 = new DateTime(endDay.getText().toString());
        int days = Days.daysBetween(d1,d2).getDays() + 1 ;
        periodLength.setText(String.valueOf(days));
        changeUserPeriodApi.setPeriod(days); //存到JavaBean
    }

    //上傳前檢查欄位是否都有填寫
    private void checkBeforeUpdate() {

        //判斷上次經期開始日是否有填寫
        if(TextUtils.isEmpty(firstDay.getText().toString())){
            Toast.makeText(getApplicationContext(), getString(R.string.start_is_not_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        //判斷上次經期結束日是否有填寫
        if(TextUtils.isEmpty(endDay.getText().toString())){
            Toast.makeText(getApplicationContext(), getString(R.string.end_is_not_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        //判斷週期是否有填寫
        if(TextUtils.isEmpty(cycleLength.getText().toString())){
            Toast.makeText(getApplicationContext(), getString(R.string.cycle_is_not_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(periodLength.getText().toString())){
            Toast.makeText(getApplicationContext(), getString(R.string.period_is_not_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        updateToApi();
    }

    //後台更新資料
    private void updateToApi() {
        //週期
        changeUserPeriodApi.setCycle(Integer.parseInt(cycleLength.getText().toString()));
        changeUserPeriodApi.setPeriod(Integer.parseInt(periodLength.getText().toString()));

        //執行後台更新
        proxy.buildPOST(MENSTRUAL_RECORD_UPDATE, changeUserPeriodApi.toJSONString(), changePeriodListener);
    }

    private ApiProxy.OnApiListener changePeriodListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(result.toString());
                        String str = jsonObject.getString("success");
                        if(str.equals("true")){
                            Toast.makeText(getApplicationContext(), getString(R.string.update_to_Api_is_success), Toast.LENGTH_SHORT).show();
                            writeToSharedPreferences();
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

        }
    };

    //經期設定寫到SharedPreferences
    private void writeToSharedPreferences() {
        SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
        pref.edit().putBoolean("MENSTRUAL", true).apply();
    }

    //日期彈跳視窗
    private void dialogPickDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        DatePickerDialog pickerDialog = new DatePickerDialog(PeriodSettingActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                Calendar start = Calendar.getInstance();
                start.set(year, month, dayOfMonth);
                firstDay.setText(df.format(start.getTime()));
                changeUserPeriodApi.setLastDate(firstDay.getText().toString());
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        pickerDialog.show();
    }

    //日期彈跳視窗
    private void dialogPikeDate2(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        DatePickerDialog pickerDialog = new DatePickerDialog(PeriodSettingActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                Calendar start = Calendar.getInstance();
                start.set(year, month, dayOfMonth);
                endDay.setText(df.format(start.getTime()));
                changeUserPeriodApi.setEndDate(endDay.getText().toString());
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        pickerDialog.show();
    }
}