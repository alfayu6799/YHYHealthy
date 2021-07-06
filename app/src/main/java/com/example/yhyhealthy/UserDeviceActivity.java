package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthy.adapter.UserDeviceAdapter;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.PRODUCTS_BIND;
import static com.example.yhyhealthy.module.ApiProxy.PRODUCTS_BIND_REMOVE;
import static com.example.yhyhealthy.module.ApiProxy.PRODUCTS_NO;

/****
 * 裝置序號 (排卵儀)
 * 功能:
 *  查詢列表
 *  綁定新增
 *  解除綁定
 *  create 2021/03/29
 * */

public class UserDeviceActivity extends AppCompatActivity implements UserDeviceAdapter.UserDeviceListener {

    private static final String TAG = "UserDeviceActivity";

    private EditText deviceNo;
    private ImageView back;
    private RecyclerView deviceList;
    private UserDeviceAdapter adapter;

    //api
    private ApiProxy proxy;

    //進度條
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_device);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //禁止旋轉

        initView();

        initData();  //裝置列表初始化
    }

    private void initView() {
        deviceNo = findViewById(R.id.edtDeviceNo);
        deviceList = findViewById(R.id.rvDeviceList);

        back = findViewById(R.id.ivBackSetting20);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        deviceNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //上傳裝置序號到後台存
                    updateDeviceNoToApi();
                }
                return false;
            }
        });
    }

    //查詢裝置列表
    private void initData(){
        proxy = ApiProxy.getInstance();
        proxy.buildPOST(PRODUCTS_NO, "", deviceListListener);
    }

    private ApiProxy.OnApiListener deviceListListener = new ApiProxy.OnApiListener() {
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
                            parserJson(result);
                        }else if (errorCode == 23) { //token失效
                            Toasty.error(UserDeviceActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(UserDeviceActivity.this, LoginActivity.class));
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(UserDeviceActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(UserDeviceActivity.this, LoginActivity.class));
                            finish();
                        }else {
                            Toasty.error(UserDeviceActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //解析後台回復的裝置資料列表
    private void parserJson(JSONObject result) {
        List<String> dataList = new ArrayList<>();
        try {
            JSONObject object = new JSONObject(result.toString());
            JSONArray array = object.getJSONArray("success");
            for (int i = 0; i < array.length(); i++){
                dataList.add(array.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //將資料傳到Adapter
        adapter = new UserDeviceAdapter(this, dataList, this);
        deviceList.setAdapter(adapter);
        deviceList.setHasFixedSize(true);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        deviceList.addItemDecoration(new SpacesItemDecoration(20));
    }

    //上傳裝置序號到後台存
    private void updateDeviceNoToApi() {
        if (TextUtils.isEmpty(deviceNo.getText().toString()))
            return;

        JSONObject json = new JSONObject();
        try {
            json.put("serialNo", deviceNo.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(PRODUCTS_BIND, json.toString(), updateDeviceListener);
    }

    private ApiProxy.OnApiListener updateDeviceListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(UserDeviceActivity.this, getString(R.string.title_process), getString(R.string.process), true);
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
                        if (errorCode ==0) {
                            Toasty.success(UserDeviceActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                            initData(); //裝置列表重刷
                        }else if (errorCode == 33){ //裝置序號錯誤
                                Toasty.success(UserDeviceActivity.this, getString(R.string.device_no_error), Toast.LENGTH_SHORT, true).show();
                        }else if (errorCode == 23) {  //token 失效
                            Toasty.error(UserDeviceActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(UserDeviceActivity.this, LoginActivity.class));
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(UserDeviceActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(UserDeviceActivity.this, LoginActivity.class));
                            finish();
                        }else {
                            Toasty.error(UserDeviceActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    @Override  //刪除裝置
    public void onDelete(String deviceNo) {
        //Log.d(TAG, "onDelete DeviceNo: " + deviceNo);
        JSONObject json = new JSONObject();
        try {
            json.put("serialNo", deviceNo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "onDelete: "+ json.toString());
        proxy.buildPOST(PRODUCTS_BIND_REMOVE, json.toString(), deleteDeviceListener);
    }

    private ApiProxy.OnApiListener deleteDeviceListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(UserDeviceActivity.this, getString(R.string.title_process), getString(R.string.process), true);
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
                            Toasty.success(UserDeviceActivity.this, getString(R.string.delete_success), Toast.LENGTH_SHORT, true).show();
                            initData(); //裝置列表重刷
                        }else if (errorCode == 23 ) {
                            Toasty.error(UserDeviceActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(UserDeviceActivity.this, LoginActivity.class));
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(UserDeviceActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(UserDeviceActivity.this, LoginActivity.class));
                            finish();
                        }else {
                            Toasty.error(UserDeviceActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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
}