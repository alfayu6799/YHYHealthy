package com.example.yhyhealthydemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yhyhealthydemo.adapter.TemperatureEditAdapter;
import com.example.yhyhealthydemo.datebase.TempDataApi;
import com.example.yhyhealthydemo.datebase.TemperatureData;
import com.example.yhyhealthydemo.module.ApiProxy;
import com.example.yhyhealthydemo.tools.ImageUtils;
import com.example.yhyhealthydemo.tools.SpacesItemDecoration;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private File tmpPhoto; //圖片路徑全域

    //
    private static final int EDIT_CODE = 1;

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
                        }else if (errorCode == 23){ //token失效
                            Toasty.error(TemperEditListActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperEditListActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(TemperEditListActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //圖檔存至本地端
    private void saveBitmap(Bitmap bitmap){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE); //放照片的目錄
        tmpPhoto = new File(directory, "takePicture" + ".jpg");
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(tmpPhoto);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                finish();//關閉此頁面
                break;
        }
    }

    //編輯
    @Override
    public void onEditClick(TempDataApi.SuccessBean data) {
        Intent intent = new Intent();
        intent.setClass(this, TemperEditActivity.class);
        Bundle bundle = new Bundle();

        //大頭貼轉成bitmap格式
        if (!data.getHeadShot().isEmpty()) { //大頭貼有資料
            Log.d(TAG, "onEditClick: " + data.getHeadShot());
            Bitmap bitmap = ImageUtils.bast64toBitmap(data.getHeadShot());
            saveBitmap(bitmap); //存到本機端記憶卡內
            bundle.putString("HeadShot", tmpPhoto.toString());
        }
//
//        if(tmpPhoto != null && !tmpPhoto.exists()){ //檔案有存在
//            bundle.putString("HeadShot", tmpPhoto.toString());
//        }
//
        bundle.putInt("targetId", data.getTargetId());
        bundle.putString("name", data.getUserName());
        bundle.putString("gender", data.getGender());
        bundle.putString("birthday", data.getTempBirthday());
        bundle.putString("height", String.valueOf(data.getTempHeight()));
        bundle.putString("weight", String.valueOf(data.getTempWeight()));
        intent.putExtras(bundle);
        startActivityForResult(intent, EDIT_CODE);
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
                        if (errorCode == 0) {
                            Toasty.success(TemperEditListActivity.this, getString(R.string.delete_success) + errorCode, Toast.LENGTH_SHORT, true).show();
                            //移除RecyclerView的Item項目
                            list.remove(pos);
                            adapter.notifyItemRemoved(pos);
                        }else if (errorCode == 23){ //token失效
                            Toasty.error(TemperEditListActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperEditListActivity.this, LoginActivity.class)); //重新登入
                            finish();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_CODE && resultCode == -1) {
            initData();
        }
    }
}