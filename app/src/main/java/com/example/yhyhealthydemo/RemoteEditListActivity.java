package com.example.yhyhealthydemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthydemo.adapter.RemoteEditListAdapter;
import com.example.yhyhealthydemo.module.ApiProxy;
import com.example.yhyhealthydemo.tools.SpacesItemDecoration;
import com.google.gson.internal.$Gson$Preconditions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthydemo.module.ApiProxy.MONITOR_CODE_UPDATE;
import static com.example.yhyhealthydemo.module.ApiProxy.REMOTE_USER_ADD;
import static com.example.yhyhealthydemo.module.ApiProxy.REMOTE_USER_DELETE;
import static com.example.yhyhealthydemo.module.ApiProxy.REMOTE_USER_LIST;

public class RemoteEditListActivity extends AppCompatActivity implements RemoteEditListAdapter.RemoteEditListListener {

    private static final String TAG = "RemoteEditListActivity";

    private ImageView btnBack;
    private TextView  addAccount;
    private RecyclerView rvRemoteView;
    private RemoteEditListAdapter adapter;

    private AlertDialog remoteDialog;
    private AlertDialog updateDialog;

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

        addAccount = findViewById(R.id.tvAddObserver);
        addAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogRemote(); //遠端帳號新增彈跳視窗
            }
        });

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
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(RemoteEditListActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
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
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {
            progressDialog.dismiss();
        }
    };

    //解析後台來的資料
    private void parserJson(JSONObject result) {
        List<String> dataList = new ArrayList<String>();
        try {
            JSONObject jsonObject = new JSONObject(result.toString());
            JSONArray array = jsonObject.getJSONArray("success");
            for (int i = 0; i < array.length(); i++){
                dataList.add(array.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //將資料傳到Adapter
        adapter = new RemoteEditListAdapter(this, dataList, this);
        rvRemoteView.setAdapter(adapter);
        rvRemoteView.setHasFixedSize(true);
        rvRemoteView.setLayoutManager(new LinearLayoutManager(this));
        rvRemoteView.addItemDecoration(new SpacesItemDecoration(30));
    }

    @Override  //更新
    public void onUpdateClick(String accountInfo, int position) {
        updateDialog = new AlertDialog.Builder(this).create();
        LayoutInflater layout = LayoutInflater.from(this);
        View view = layout.inflate(R.layout.dialog_remote_add, null);
        updateDialog.setView(view);
        updateDialog.setCancelable(false); //禁用非視窗區

        TextView title = view.findViewById(R.id.titleRemote);
        title.setText(getString(R.string.please_update_auth_code));
        EditText accountOther = view.findViewById(R.id.edtOtherAccount);
        accountOther.setFocusable(false);            //不可編輯
        accountOther.setFocusableInTouchMode(false); //不可編輯
        accountOther.setText(accountInfo);
        EditText otherAuthCode = view.findViewById(R.id.edtAuthorization);

        Button btnCancel = view.findViewById(R.id.btnRemoteCancel);
        Button btnSubmit = view.findViewById(R.id.btnRemoteSend);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDialog.dismiss();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(otherAuthCode.getText().toString()))
                    return;

                updateNewAuthCodeToApi(otherAuthCode.getText().toString(), accountOther.getText().toString());  //更新授權碼
            }
        });

        updateDialog.show();
    }

    //更新監控者授權碼 2021/03/26
    private void updateNewAuthCodeToApi(String code, String account) {
        JSONObject json = new JSONObject();
        try {
            json.put("account", account);
            json.put("monitorCode", code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildPOST(MONITOR_CODE_UPDATE, json.toString(), codeUpdateListener);
    }

    private ApiProxy.OnApiListener codeUpdateListener = new ApiProxy.OnApiListener() {
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
                            Toasty.success(RemoteEditListActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                            updateDialog.dismiss(); //關閉視窗
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

    @Override   //刪除
    public void onDeleteClick(String accountInfo, int position) {
        DeleteDateToApi(accountInfo);
    }

    //刪除遠端帳號
    private void DeleteDateToApi(String accountInfo) {
        JSONObject json = new JSONObject();
        try {
            json.put("account", accountInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(REMOTE_USER_DELETE, json.toString(), deleteAccountListener);
    }

    private ApiProxy.OnApiListener deleteAccountListener = new ApiProxy.OnApiListener() {
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
                            Toasty.success(RemoteEditListActivity.this, getString(R.string.delete_success), Toast.LENGTH_SHORT, true).show();
                            initDate(); //重新跟後台取資料並刷新RecyclerView的內容
                        }else {
                            Log.d(TAG, "刪除監控者結果後台錯誤回覆碼:" + errorCode);
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

    //遠端帳號新增彈跳視窗
    private void dialogRemote() {
        remoteDialog = new AlertDialog.Builder(this).create();
        LayoutInflater layout = LayoutInflater.from(this);
        View remoteView = layout.inflate(R.layout.dialog_remote_add, null);
        remoteDialog.setView(remoteView);
        remoteDialog.setCancelable(false); //禁用非視窗區

        TextView titleName = remoteView.findViewById(R.id.titleRemote);
        titleName.setText(getString(R.string.title_remote_account));
        EditText account = remoteView.findViewById(R.id.edtOtherAccount);
        EditText authCode = remoteView.findViewById(R.id.edtAuthorization);

        Button cancel = remoteView.findViewById(R.id.btnRemoteCancel);
        Button submit = remoteView.findViewById(R.id.btnRemoteSend);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remoteDialog.dismiss();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //檢查資料是否齊全
                if (TextUtils.isEmpty(account.getText().toString()))
                    return;
                if (TextUtils.isEmpty(authCode.getText().toString()))
                    return;

                //傳送到後台
                updateRemoteToApi(account, authCode);

            }
        });

        remoteDialog.show();
    }

    //遠端帳號新增的資料傳送到後台
    private void updateRemoteToApi(EditText account, EditText authCode) {
        JSONObject json = new JSONObject();
        try {
            json.put("account", account.getText().toString());
            json.put("monitorCode", authCode.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(REMOTE_USER_ADD, json.toString(), remoteAddListener);
    }

    private ApiProxy.OnApiListener remoteAddListener = new ApiProxy.OnApiListener() {
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
                            boolean success = object.getBoolean("success");
                            if (success)
                                Toasty.success(RemoteEditListActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                                initDate(); //重新跟後台取資料並刷新RecyclerView的內容
                                remoteDialog.dismiss(); //關閉彈跳視窗
                        }else {
                            Log.d(TAG, "新增觀測者結果後台錯誤回覆碼:" + errorCode);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}