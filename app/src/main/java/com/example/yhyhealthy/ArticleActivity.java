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
import android.widget.TextView;

import com.example.yhyhealthy.adapter.ArticleAdapter;
import com.example.yhyhealthy.datebase.ArticleData;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.json.JSONException;
import org.json.JSONObject;
import static com.example.yhyhealthy.module.ApiProxy.ARTICLE_LIST;

public class ArticleActivity extends AppCompatActivity {

    private static final String TAG = "ArticleActivity";

    private String attrID = "";
    private String serviceItemId ="";
    private String attrName = "";

    private ImageView back;
    private RecyclerView rvArt;
    private TextView articleTitle;

    //api
    ApiProxy proxy;
    ArticleData articleData;

    //進度條
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //禁止旋轉

        proxy = ApiProxy.getInstance();
        articleData = new ArticleData();

        initView();

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            attrID = bundle.getString("AttrID");
            serviceItemId = bundle.getString("ServiceItemId");
            attrName = bundle.getString("AttName");
            loadInfo(); //呼叫後端資料
        }
    }

    private void initView() {
        back = findViewById(R.id.imageBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        articleTitle = findViewById(R.id.tvArtTitle);

        int spacingInPixels = 20;  //設定item間距的距離
        rvArt = findViewById(R.id.rv_article);
        rvArt.setLayoutManager(new LinearLayoutManager(this));
        rvArt.setHasFixedSize(true);
        rvArt.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //設定item間距
    }

    private void loadInfo() {
        //取得手機語系
        String language = getResources().getConfiguration().locale.getLanguage();
        String country = getResources().getConfiguration().locale.getCountry();
        String defaultLan = language + "-" + country;

        JSONObject json = new JSONObject();
        try {
            json.put("serviceItemId", serviceItemId);
            json.put("attrId", attrID);
            json.put("offset",1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildEdu(ARTICLE_LIST, json.toString(), defaultLan,requestListener);
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

    //解析文章
    private void parserResult(JSONObject result) {
        articleData = ArticleData.newInstance(result.toString());
        ArticleAdapter adapter = new ArticleAdapter(this, articleData.getArticleList());
        rvArt.setAdapter(adapter);
        articleTitle.setText(attrName);
    }
}