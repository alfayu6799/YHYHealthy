package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.yhyhealthy.adapter.EducationAdapter;
import com.example.yhyhealthy.datebase.EducationData;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.json.JSONObject;

import java.util.concurrent.BlockingDeque;

import static com.example.yhyhealthy.module.ApiProxy.EDU_ART_CATALOG;

/******** ************
 * 衛教 - 類別首頁 : 熱更新
 * *******  ***** ****/

public class CatalogActivity extends AppCompatActivity {

    private static final String TAG = "CatalogActivity";

    private ImageView back;

    private RecyclerView recyclerView;

    //api
    private ApiProxy proxy;
    private EducationData educationData;

    //進度條
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //禁止旋轉

        proxy = ApiProxy.getInstance();
        educationData = new EducationData();

        initView();

        initDate();
    }

    private void initView() {
        back = findViewById(R.id.eduBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        int spacingInPixels = 30;  //設定item間距的距離
        recyclerView = findViewById(R.id.rvEdu);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //設定item間距
    }

    private void initDate() {
        //取得手機語系
        String language = getResources().getConfiguration().locale.getLanguage();
        String country = getResources().getConfiguration().locale.getCountry();
        String defaultLan = language + "-" + country;
        Log.d(TAG, "initDate: " + EDU_ART_CATALOG);
        proxy.buildEdu(EDU_ART_CATALOG, "", defaultLan,requestListener);
    }

    private ApiProxy.OnApiListener requestListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(CatalogActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parserResult(result);
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

    //將需要的text&icon填入
    private void parserResult(JSONObject result) {
        Log.d(TAG, "parserResult: " + result.toString());
        educationData = EducationData.newInstance(result.toString());
        EducationAdapter adapter = new EducationAdapter(CatalogActivity.this, educationData.getServiceItemList());
        recyclerView.setAdapter(adapter);
    }

}