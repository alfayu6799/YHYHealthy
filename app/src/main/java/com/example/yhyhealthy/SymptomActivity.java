package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yhyhealthy.adapter.CheckBoxAdapter;
import com.example.yhyhealthy.adapter.SwitchItemAdapter;
import com.example.yhyhealthy.datebase.SymptomData;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.SYMPTOM_ADD;
import static com.example.yhyhealthy.module.ApiProxy.SYMPTOM_LIST;

/****  ***********
 * 症狀
 * source data from Api (熱更新)
 * 配適器 switch - SwitchItemAdapter
 * 配適器 checkBox - CheckBoxAdapter
 * create 2021/04/07
 * *  *************/

public class SymptomActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SymptomActivity";

    private ImageView back;
    private RecyclerView viewSymptomSW, viewSymptomCH;

    private int targetId;
    private int position;

    private Button update;

    //api
    private ApiProxy proxy;

    //
   private List<SymptomData.SwitchItemBean> switchItemBeanList = new ArrayList<>();
   private List<SymptomData.CheckBoxGroup> checkBoxGroupList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //禁止旋轉

        //休眠禁止
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //接受來自TemperatureActivity的資料
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            targetId = bundle.getInt("targetId");  //使用全域變數
            position = bundle.getInt("position");  //使用全域變數
        }

        proxy = ApiProxy.getInstance();

        initView();

        initData();
    }

    private void initView() {
        update = findViewById(R.id.btnUpdate);
        back = findViewById(R.id.ivBackBlePage);

        viewSymptomSW = findViewById(R.id.rvSymptomUp);
        viewSymptomCH = findViewById(R.id.rvSymptomDown);

        update.setOnClickListener(this);
        back.setOnClickListener(this);

    }

    private void initData(){
        proxy.buildPOST(SYMPTOM_LIST, "", symptomListener);
    }

    private ApiProxy.OnApiListener symptomListener = new ApiProxy.OnApiListener() {
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
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0) {
                            initSymptom(result);
                        }else if (errorCode == 23){ //token失效
                            Toasty.error(SymptomActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(SymptomActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(SymptomActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

        }
    };

    //症狀初始化 2021/04/07
    private void initSymptom(JSONObject result) {
        Log.d(TAG, "initSymptom: " + result.toString());
        try {
            JSONObject jsonObject = new JSONObject(result.toString());
            JSONArray array = jsonObject.getJSONArray("success");
            for (int i = 0; i < array.length(); i++){
                JSONObject newObject = array.getJSONObject(i);
                String key = newObject.getString("key");
                Object value = newObject.get("value");
                if (value instanceof Boolean){
                    boolean booleanValue = newObject.getBoolean("value");
                    switchItemBeanList.add(new SymptomData.SwitchItemBean(key, booleanValue));
                }else if (value instanceof JSONArray){
                    JSONArray jsonValue = newObject.getJSONArray("value");
                    List<String> listData = new ArrayList<>();
                    for (int k = 0; k < jsonValue.length(); k++){
                        listData.add(jsonValue.getString(k));
                    }
                    checkBoxGroupList.add(new SymptomData.CheckBoxGroup(key,listData));
                }/*else if (value instanceof String){
                    String testStr = newObject.getString("value");
                }*/
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //解析出來的布林資料傳到Switch的Adapter
        SwitchItemAdapter switchItemAdapter = new SwitchItemAdapter(this, switchItemBeanList);
        viewSymptomSW.setAdapter(switchItemAdapter);
        viewSymptomSW.setHasFixedSize(true);
        viewSymptomSW.setLayoutManager(new LinearLayoutManager(this));
        viewSymptomSW.addItemDecoration(new SpacesItemDecoration(10));

        //解析出來的陣列資料傳到checkbox的Adapter
        CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(this, checkBoxGroupList);
        viewSymptomCH.setAdapter(checkBoxAdapter);
        viewSymptomCH.setHasFixedSize(true);
        viewSymptomCH.setLayoutManager(new LinearLayoutManager(this));
        viewSymptomCH.addItemDecoration(new SpacesItemDecoration(10));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackBlePage:
                finish();
                break;
            case R.id.btnUpdate:  //上傳到後台
                updateToApi();
                break;
        }
    }

    //更新上傳到後台
    private void updateToApi(){
        DateTime dt1 = new DateTime();
        String SymptomRecordTime = dt1.toString("yyyy-MM-dd,HH:mm:ss");

        JSONArray array = new JSONArray();

        //switch
        for(int i=0; i < switchItemBeanList.size(); i++){
            JSONObject objectSwitch = new JSONObject();
            try {
                objectSwitch.put("key", switchItemBeanList.get(i).getKey());
                objectSwitch.put("value",switchItemBeanList.get(i).isValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            array.put(objectSwitch);
        }

        //checkBox
        for(int j = 0; j < checkBoxGroupList.size(); j++){
            JSONObject objectCheckBox = new JSONObject();
            try {
                objectCheckBox.put("key", checkBoxGroupList.get(j).getKey());

                objectCheckBox.put("value", new JSONArray(checkBoxGroupList.get(j).getChecked()));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            array.put(objectCheckBox);
        }

        JSONObject finalObject = new JSONObject();
        try {
            finalObject.put("targetId", targetId);
            finalObject.put("createDate", SymptomRecordTime);
            finalObject.put("symptoms", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.d(TAG, "updateToApi: " + finalObject.toString());
        proxy.buildPOST(SYMPTOM_ADD, finalObject.toString(), addListener);
    }

    private ApiProxy.OnApiListener addListener = new ApiProxy.OnApiListener() {
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
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0) {
                            Toasty.success(SymptomActivity.this, R.string.update_success, Toast.LENGTH_SHORT, true).show();
                            finish(); //返回上一頁
                        }else if (errorCode == 23){ //token失效
                            Toasty.error(SymptomActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(SymptomActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(SymptomActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

        }
    };
}