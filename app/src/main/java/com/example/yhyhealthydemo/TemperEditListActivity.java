package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yhyhealthydemo.adapter.TemperatureEditAdapter;
import com.example.yhyhealthydemo.datebase.TempDataApi;
import com.example.yhyhealthydemo.datebase.TemperatureData;
import com.example.yhyhealthydemo.module.ApiProxy;
import com.example.yhyhealthydemo.tools.SpacesItemDecoration;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthydemo.module.ApiProxy.BLE_USER_DELETE;
import static com.example.yhyhealthydemo.module.ApiProxy.BLE_USER_LIST;

/** ******* ***
 *  體溫列表編輯(多對象)
 *  配適器:TemperatureEditAdapter
 *  資料來源:TempDataApi.SuccessBean
 *  實做:
 *     刪除onRemoveClick
 *  Create date : 2021/03/22
 * * ***************/

public class TemperEditListActivity extends AppCompatActivity implements View.OnClickListener, TemperatureEditAdapter.TemperatureEditListener {

    private static final String TAG = "TemperEditListActivity";

    private ImageView back;
    private Button    addUser;

    private RecyclerView rv;
    private TemperatureEditAdapter adapter;
    private List<TempDataApi.SuccessBean> list;
    private int pos;

    //api
    private ApiProxy proxy;

    //進度條
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temper_edit_list);

        initView();

        initData();
    }

    private void initView() {
        rv = findViewById(R.id.rvDegreeEdit);
        addUser = findViewById(R.id.btnAddDegreeUser);
        addUser.setVisibility(View.INVISIBLE);
        back = findViewById(R.id.ivDegreeBack);
        back.setOnClickListener(this);
        addUser.setOnClickListener(this);
    }

    private void initData() {
        proxy = ApiProxy.getInstance();
        proxy.buildPOST(BLE_USER_LIST, "", UserListListener);
    }

    private ApiProxy.OnApiListener UserListListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(TemperEditListActivity.this, getString(R.string.title_process), getString(R.string.process), true);
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
                            parserJsonData(result);
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

    //解析後台回來的資料
    private void parserJsonData(JSONObject result) {
        TempDataApi tempDataApi = TempDataApi.newInstance(result.toString());
        list = tempDataApi.getSuccess();

        //將資料配置到Adapter並顯示出來
        adapter = new TemperatureEditAdapter(this, list, this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        rv.addItemDecoration(new SpacesItemDecoration(10));
   }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivDegreeBack: //回上一頁
                finish();
                break;
            case R.id.btnAddDegreeUser: //新增監控者
                startActivity(new Intent(this, TemperatureAddActivity.class));
                finish();//關閉此頁面,因為重新刷新後base64太耗時間...帶後台改成url再取消
                break;
        }
    }

    //刪除
    @Override
    public void onRemoveClick(TempDataApi.SuccessBean data, int position) {
        pos = position;  //取得使用者在RecyclerView Item的位置

        JSONObject json = new JSONObject();
        try {
            json.put("targetId", data.getTargetId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildPOST(BLE_USER_DELETE, json.toString(), deleteListener);
    }

    private ApiProxy.OnApiListener deleteListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(TemperEditListActivity.this, getString(R.string.title_process), getString(R.string.process), true);
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
                            Toasty.success(TemperEditListActivity.this, getString(R.string.delete_success) + errorCode , Toast.LENGTH_SHORT, true).show();
                            //移除RecyclerView的Item項目
                            list.remove(pos);
                            adapter.notifyItemRemoved(pos);
                        }else {
                            Toasty.error(TemperEditListActivity.this, getString(R.string.json_error_code) + errorCode , Toast.LENGTH_SHORT, true).show();
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