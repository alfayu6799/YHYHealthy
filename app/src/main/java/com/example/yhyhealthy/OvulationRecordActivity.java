package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yhyhealthy.adapter.OvulationRecordAdapter;
import com.example.yhyhealthy.datebase.CycleRecord;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.CYCLE_RECORD;

public class OvulationRecordActivity extends AppCompatActivity {

    private static final String TAG = "OvulationRecordActivity";

    private ImageView back;
    private RecyclerView rvOvulation;

    private String startDay ="";
    private String endDay = "";

    private ApiProxy proxy;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ovulation_record);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //禁止旋轉

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            startDay = bundle.getString("startDay");
            endDay = bundle.getString("endDay");
        }

        initView();

        getOvulationData();
    }

    private void initView() {
        back = findViewById(R.id.imgBackOvu);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        rvOvulation = findViewById(R.id.rvOvulRecord);

        proxy = ApiProxy.getInstance();
    }

    //根據使用者給予的日期範圍跟後台取資料
    private void getOvulationData() {
        JSONObject json = new JSONObject();
        try {
            json.put("startDate", startDay);
            json.put("endDate", endDay);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildPOST(CYCLE_RECORD, json.toString(), cycleRecordListener);
    }

    private ApiProxy.OnApiListener cycleRecordListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(OvulationRecordActivity.this, getString(R.string.title_process), getString(R.string.process), true);
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
                        JSONObject jsonObject = new JSONObject(result.toString());
                        int errorCode = jsonObject.getInt("errorCode");
                        if(errorCode == 0){
                            parserJson(result);
                        }else if (errorCode == 23){  //token失效
                            Toasty.error(OvulationRecordActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(OvulationRecordActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(OvulationRecordActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT,true).show();
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

    //解析後台來的資料
    private void parserJson(JSONObject result) {
        CycleRecord cycleRecord = CycleRecord.newInstance(result.toString());
        List<CycleRecord.SuccessBean> dataList = cycleRecord.getSuccess();  //resource
        //將資料配置到adapter顯示
        OvulationRecordAdapter adapter = new OvulationRecordAdapter(this, dataList);
        rvOvulation.setAdapter(adapter);
        rvOvulation.setHasFixedSize(true);
        rvOvulation.setLayoutManager(new LinearLayoutManager(this));
        rvOvulation.addItemDecoration(new SpacesItemDecoration(30));
    }
}