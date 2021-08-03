package com.example.yhyhealthy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yhyhealthy.adapter.TemperatureEditAdapter;
import com.example.yhyhealthy.datebase.TempDataApi;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.ImageUtils;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import es.dmoral.toasty.Toasty;
import pl.droidsonroids.gif.GifImageView;

import static com.example.yhyhealthy.module.ApiProxy.BLE_USER_DELETE;
import static com.example.yhyhealthy.module.ApiProxy.BLE_USER_LIST;

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

    //背景動畫
    private GifImageView gifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temper_edit_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //禁止旋轉

        initView();

        initData();
    }

    private void initView() {
        rv = findViewById(R.id.rvDegreeEdit);
        back = findViewById(R.id.ivDegreeBack);

        //動畫background
        gifImageView = findViewById(R.id.game_gif);
        gifImageView.setBackgroundResource(R.mipmap.yhy_new_background);

        back.setOnClickListener(this);
    }

    //初始化觀測者資料(from api)
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
                        }else if (errorCode == 31){
                            Toasty.error(TemperEditListActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
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

    //解析後台回來的觀測者資料
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
                setResult(RESULT_OK);  //帶參數回上一頁 2021/05/26
                finish();
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
            Bitmap bitmap = ImageUtils.bast64toBitmap(data.getHeadShot());
            saveBitmap(bitmap); //存到本機端記憶卡內
            bundle.putString("HeadShot", tmpPhoto.toString());
        }

        if(tmpPhoto != null && !tmpPhoto.exists()){ //檔案有存在
            bundle.putString("HeadShot", tmpPhoto.toString());
        }

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
                        }else if (errorCode == 31){
                            Toasty.error(TemperEditListActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
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