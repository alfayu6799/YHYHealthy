package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.yhyhealthydemo.adapter.RemoteEditListAdapter;
import com.example.yhyhealthydemo.module.ApiProxy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.yhyhealthydemo.module.ApiProxy.REMOTE_USER_LIST;

public class RemoteEditListActivity extends AppCompatActivity {

    private static final String TAG = "RemoteEditListActivity";

    private ImageView btnBack;
    private RecyclerView rvRemoteView;
    private RemoteEditListAdapter adapter;

    //api
    private ApiProxy proxy;

    //進度條
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_edit_list);

        initView();

        initDate();
    }

    private void initView() {
        rvRemoteView = findViewById(R.id.rvRemoteEdit);

        btnBack = findViewById(R.id.ivRemoteBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();  //回到上一頁
            }
        });
    }

    private void initDate() {
        proxy = ApiProxy.getInstance();
        proxy.buildPOST(REMOTE_USER_LIST, "" , requestListener);
    }

    private ApiProxy.OnApiListener requestListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            try {
                JSONObject object = new JSONObject(result.toString());
                int errorCode = object.getInt("errorCode");
                if (errorCode == 0){
                   parserJson(result);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        try {
            JSONObject jsonObject = new JSONObject(result.toString());
            JSONArray array = jsonObject.getJSONArray("success");
            for (int i = 0; i < array.length(); i++){
                Log.d("account list:", array.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}