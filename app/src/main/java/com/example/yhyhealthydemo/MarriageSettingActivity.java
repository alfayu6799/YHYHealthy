package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.example.yhyhealthydemo.datebase.MarriageData;
import com.example.yhyhealthydemo.module.ApiProxy;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.yhyhealthydemo.module.ApiProxy.MARRIAGE_INFO;

/***  ****************
 * 設定 - 個人 - 婚姻狀態
 * * **********************/

public class MarriageSettingActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private static final String TAG = "MarriageSettingActivity";

    ImageView back;
    Button save;

    //婚姻狀況
    Switch    marriageStatus;
    Switch    contraceptionStatus;
    Switch    childStatus;

    //api
    ApiProxy proxy;
    MarriageData marriageData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marriage_setting);

        proxy = ApiProxy.getInstance();

        initView();

        initData();
    }

    private void initView() {
        back = findViewById(R.id.ivBackSetting9);
        save = findViewById(R.id.btnSaveToApi2);
        marriageStatus = findViewById(R.id.switchMarriage);
        contraceptionStatus = findViewById(R.id.switchContraception);
        childStatus = findViewById(R.id.switchChild);

        back.setOnClickListener(this);
        save.setOnClickListener(this);

        marriageStatus.setOnCheckedChangeListener(this);
        contraceptionStatus.setOnCheckedChangeListener(this);
        childStatus.setOnCheckedChangeListener(this);
    }

    //跟後台要資料
    private void initData() {
        proxy.buildPOST(MARRIAGE_INFO, "", marriageListener);
    }

    private ApiProxy.OnApiListener marriageListener = new ApiProxy.OnApiListener() {
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

    //解析後台來的資料並填入
    private void parserJson(JSONObject result) {
        marriageData = MarriageData.newInstance(result.toString());
        Log.d(TAG, "parserJson: " + result.toString());

        //婚姻
        boolean married = marriageData.getSuccess().isMarried();
        marriageStatus.setChecked(married);

        //孩子
        boolean child = marriageData.getSuccess().isHasChild();
        childStatus.setChecked(child);

        //避孕
        boolean contraception = marriageData.getSuccess().isContraception();
        contraceptionStatus.setChecked(contraception);
    }

    //Switch button onclick
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
        switch (compoundButton.getId()){
            case R.id.switchMarriage: //婚姻狀況
                if (isCheck){
                    marriageStatus.setChecked(true);
                }else{
                    marriageStatus.setChecked(false);
                }
                break;
            case R.id.switchContraception: //行房
                if (isCheck){
                    contraceptionStatus.setChecked(true);
                }else {
                    contraceptionStatus.setChecked(false);
                }
                break;
            case R.id.switchChild:   //小孩
                if (isCheck){
                    childStatus.setChecked(true);
                }else {
                    childStatus.setChecked(false);
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackSetting9: //返回上一頁
                finish();
                break;
            case R.id.btnSaveToApi2: //將修改過的資料更新
                updateToApi();
                break;
        }
    }

    //寫回後台
    private void updateToApi() {
        Log.d(TAG, "updateToApi: " + marriageData.toJSONString());
    }

    //禁用返回健
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}