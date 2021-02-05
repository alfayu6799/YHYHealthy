package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yhyhealthydemo.datebase.EducationApi;
import com.example.yhyhealthydemo.module.ApiProxy;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.yhyhealthydemo.module.ApiProxy.EDU_ART_CATALOG;

public class ArticleActivity extends AppCompatActivity {

    private static final String TAG = "ArticleActivity";

    //返回
    ImageView back;
    TextView title1, title2;

    //api
    ApiProxy proxy;

    EducationApi educationApi;

    //
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        proxy = ApiProxy.getInstance();
        educationApi = new EducationApi();

        initView();

        initDate();
    }

    private void initView() {
        title1 = findViewById(R.id.article_1_name);
        title2 = findViewById(R.id.article_2_name);

        back = findViewById(R.id.eduBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initDate() {
        //取得手機語系
        String language = getResources().getConfiguration().locale.getLanguage();
        String country = getResources().getConfiguration().locale.getCountry();
        String defaultLan = language + "-" + country;

        JSONObject json = new JSONObject();
        try {
            json.put("sysId", "0");
            json.put("language", defaultLan);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildEdu(EDU_ART_CATALOG, json.toString(), requestListener);
    }

    private ApiProxy.OnApiListener requestListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(ArticleActivity.this, getString(R.string.title_process), getString(R.string.process), true);
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
        educationApi = EducationApi.newInstance(result.toString());
        for(int i = 0; i < educationApi.getServiceItemList().size(); i++){
            String id = educationApi.getServiceItemList().get(i).getId();
            String name = educationApi.getServiceItemList().get(i).getName();
            String titleIcon = educationApi.getServiceItemList().get(i).getIconImg();
            if (id.equals("01")){
                title1.setText(name);
            }
            if(id.equals("02")){
                title2.setText(name);
            }
        }

    }
}