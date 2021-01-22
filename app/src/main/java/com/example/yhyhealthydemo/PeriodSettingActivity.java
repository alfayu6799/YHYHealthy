package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.yhyhealthydemo.datebase.PeriodData;
import com.example.yhyhealthydemo.module.ApiProxy;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.yhyhealthydemo.module.ApiProxy.MENSTRUAL_RECORD_INFO;

/**
 * 使用者設定 - 經期設定
 * */

public class PeriodSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PeriodSettingActivity";

    ImageView back;
    EditText  cycleLength, periodLength;
    EditText  firstDay, endDay;

    //api
    ApiProxy proxy;
    PeriodData period;

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
        periodLength = findViewById(R.id.edtPeriodLength);   //經期長度
        firstDay = findViewById(R.id.editTextDateStart);     //起始日
        endDay = findViewById(R.id.editTextDateEnd);         //結束日

        back.setOnClickListener(this);
    }

    private void initData() {
        proxy = ApiProxy.getInstance();

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
                    parserJson(result); //解析後台來的資料
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

    private void parserJson(JSONObject result) {
        period = PeriodData.newInstance(result.toString());
        Log.d(TAG, "parserJson: " + period);
        //週期長度
        String periodSize = String.valueOf(period.getSuccess().getCycle());
        cycleLength.setText(periodSize);
//
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackUserSetting:
                finish();
                break;
            case R.id.editTextDateStart:
                dialogPickDate();
            case R.id.editTextDateEnd:

                break;
        }
    }

    //日期彈跳視窗
    private void dialogPickDate() {

    }
}