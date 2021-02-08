package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.yhyhealthydemo.adapter.EducationAdapter;
import com.example.yhyhealthydemo.datebase.ArticleData;
import com.example.yhyhealthydemo.datebase.EducationData;
import com.example.yhyhealthydemo.module.ApiProxy;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.yhyhealthydemo.module.ApiProxy.EDU_ART_CATALOG;

public class ArticleActivity extends AppCompatActivity {

    private static final String TAG = "ArticleActivity";

    ImageView back;

    RecyclerView recyclerView;

    //api
    ApiProxy proxy;
    EducationData educationData;
    ArticleData articleData;
    List<ArticleData> artistsList = new ArrayList<>();

    //進度條
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        proxy = ApiProxy.getInstance();
        educationData = new EducationData();
        articleData = new ArticleData();

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

        recyclerView = findViewById(R.id.rvEdu);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
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
        educationData = EducationData.newInstance(result.toString());
        EducationAdapter adapter = new EducationAdapter(ArticleActivity.this, educationData.getServiceItemList());
        recyclerView.setAdapter(adapter);


        for(int i = 0; i < educationData.getServiceItemList().size(); i++){
            String id = educationData.getServiceItemList().get(i).getId();
            String name = educationData.getServiceItemList().get(i).getName();
            String titleIcon = educationData.getServiceItemList().get(i).getIconImg();




/*
            list = educationData.getServiceItemList().get(i).getAttrlist();
            if (id.equals("01")){
//                title1.setText(name);
//                Picasso.get().load(imgURL + titleIcon).into(titleIcon1);
                for(int j = 0; j < list.size(); j++){
                    String nameStr1 = list.get(j).getAttrName();
                    String iconStr1 = list.get(j).getIconImg();


                }

            }
            if(id.equals("02")){
//                title2.setText(name);
//                Picasso.get().load(imgURL + titleIcon).into(titleIcon2);
                for (int k = 0; k < list.size(); k++){
                    String nameStr2 = list.get(k).getAttrName();
                    String iconStr2 = list.get(k).getIconImg();
                    Log.d(TAG, "parserResult 02: name" + nameStr2 + " IconName:" + iconStr2);
                }
            }
            */

        }

    }
}