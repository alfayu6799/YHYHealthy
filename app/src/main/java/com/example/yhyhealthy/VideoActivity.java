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

import com.example.yhyhealthy.adapter.VideoAdapter;
import com.example.yhyhealthy.datebase.VideoData;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.json.JSONObject;

import static com.example.yhyhealthy.module.ApiProxy.EDU_VIDEO_CATALOG;

public class VideoActivity extends AppCompatActivity {

    private static final String TAG = "VideoActivity";

    private ImageView back;

    private RecyclerView rvVideo;

    //api
    private ApiProxy proxy;
    private VideoData videoData;

    //進度條
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //禁止旋轉

        proxy = ApiProxy.getInstance();
        videoData = new VideoData();

        initView();

        initDate();
    }

    private void initView() {

        back = findViewById(R.id.eduVideoBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        int spacingInPixels = 30;  //設定item間距的距離
        rvVideo = findViewById(R.id.rvEduVideo);
        rvVideo.setLayoutManager(new LinearLayoutManager(this));
        rvVideo.setHasFixedSize(true);
        rvVideo.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //設定item間距
    }

    private void initDate() {
        //取得手機語系
        String language = getResources().getConfiguration().locale.getLanguage();
        String country = getResources().getConfiguration().locale.getCountry();
        String defaultLan = language + "-" + country;

        proxy.buildEdu(EDU_VIDEO_CATALOG, "", defaultLan, requestListener);
    }

    private ApiProxy.OnApiListener requestListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(VideoActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parserResult(result);  //後台回傳影片分類
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

    //解析後台回傳的影片分類
    private void parserResult(JSONObject result) {
        videoData = VideoData.newInstance(result.toString());
        VideoAdapter adapter = new VideoAdapter(VideoActivity.this, videoData.getServiceItemList());
        rvVideo.setAdapter(adapter);
    }
}