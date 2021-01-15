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

public class MenstruationSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MenstruationSettingActi";

    ImageView back;
    EditText  cycleLength, periodLength;

    //api
    ApiProxy proxy;
    PeriodData period;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menstruation_setting);

        initData();

        initView();

    }

    private void initView() {
        back = findViewById(R.id.ivBackUserSetting);
        cycleLength = findViewById(R.id.edtCycleLength);     //週期長度
        periodLength = findViewById(R.id.edtPeriodLength);   //經期長度

        back.setOnClickListener(this);
    }

    private void initData() {
        proxy = ApiProxy.getInstance();

        JSONObject json = new JSONObject();
        try {
            json.put("type", "3");
            json.put("userId", "H5E3q5MjA=");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(MENSTRUAL_RECORD_INFO, json.toString(), periodListeren);
    }

    private ApiProxy.OnApiListener periodListeren = new ApiProxy.OnApiListener() {
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

        //週期長度
        String periodSize = String.valueOf(period.getCycle());
        cycleLength.setText(periodSize);

        //經期長度
        String cycleSize = String.valueOf(period.getPeriod());
        periodLength.setText(cycleSize);

        //開始時間


        //結束時間
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackUserSetting:
                finish();
                break;
        }
    }
}