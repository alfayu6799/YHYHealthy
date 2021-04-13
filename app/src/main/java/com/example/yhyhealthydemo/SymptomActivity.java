package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.example.yhyhealthydemo.adapter.CheckBoxAdapter;
import com.example.yhyhealthydemo.adapter.SwitchItemAdapter;
import com.example.yhyhealthydemo.datebase.SymptomData;
import com.example.yhyhealthydemo.module.ApiProxy;
import com.example.yhyhealthydemo.tools.SpacesItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.yhyhealthydemo.module.ApiProxy.SYMPTOM_LIST;

/****  ***********
 * 症狀
 * source data from Api
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom);

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
                        if (errorCode == 0){
                            initSymptom(result);  //症狀初始化
                        }else {
                            Log.d(TAG, "症狀初始化之後台傳過來的錯誤碼: " + errorCode);
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

        //初始化
        List<SymptomData.SwitchItemBean> switchItemBeanList = new ArrayList<>();
        List<SymptomData.CheckBoxGroup> checkBoxGroupList = new ArrayList<>();

        try {
            JSONObject object = new JSONObject(result.toString());
            JSONArray array = object.getJSONArray("success");
            for (int i = 0; i < array.length(); i++){
                JSONObject newObject = array.getJSONObject(i);
                String key = newObject.getString("key");
                Object value = newObject.get("value");

                //因為value有兩種不同的dataType型態
                if (value instanceof Boolean){
                    boolean booleanValue = newObject.getBoolean("value");
                    switchItemBeanList.add(new SymptomData.SwitchItemBean(key, booleanValue));

                }else if (value instanceof JSONArray){
                    JSONArray jsonValue = newObject.getJSONArray("value");
                    List<String> listData = new ArrayList<>();
                    for (int j =0; j < jsonValue.length(); j++){
                        listData.add(jsonValue.getString(j));
                    }
                    checkBoxGroupList.add(new SymptomData.CheckBoxGroup(key, listData));
                }
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

    //更新上傳到後台
    private void updateToApi(){

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
}