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

import com.example.yhyhealthy.adapter.VideoListAdapter;
import com.example.yhyhealthy.datebase.VideoListData;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.json.JSONException;
import org.json.JSONObject;

import pl.droidsonroids.gif.GifImageView;

import static com.example.yhyhealthy.module.ApiProxy.VIDEO_LIST;

/**  **********
 *  影片列表
 *  配適器 : VideoListAdapter
 * ** ***********/
public class VideoDetailActivity extends AppCompatActivity {

    private static final String TAG = "VideoDetailActivity";

    private TextView title;
    private ImageView back;
    private RecyclerView recyclerView;

    //api
    private ApiProxy proxy;
    private VideoListData listData;

    //進度條
    ProgressDialog progressDialog;

    //背景動畫
    private GifImageView gifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //禁止旋轉

        proxy = ApiProxy.getInstance();
        listData = new VideoListData();

        initView();

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            String attrId = bundle.getString("AttrID");
            String serviceItemId = bundle.getString("ServiceItemId");
            String videoName = bundle.getString("AttName");
            title.setText(videoName);
            initVideo(attrId, serviceItemId);
        }
    }

    private void initView() {
        title = findViewById(R.id.tvArtTitle);

        back = findViewById(R.id.imageBackVideo);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //動畫background
        gifImageView = findViewById(R.id.game_gif);
        gifImageView.setBackgroundResource(R.mipmap.yhy_new_background);

        recyclerView = findViewById(R.id.rv_video);
        int spacingInPixels = 10;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
    }

    private void initVideo(String attrId, String serviceItemId) {
        //取得手機語系
        String language = getResources().getConfiguration().locale.getLanguage();
        String country = getResources().getConfiguration().locale.getCountry();
        String defaultLan = language + "-" + country;

        JSONObject json = new JSONObject();
        try {
            json.put("serviceItemId", serviceItemId);
            json.put("attrId", attrId);
            json.put("offset",1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildEdu(VIDEO_LIST, json.toString(), defaultLan, requestListener);
    }

    private ApiProxy.OnApiListener requestListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(VideoDetailActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parserJson(result);
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

    //解析後台回來的資料並傳到adapter去顯示
    private void parserJson(JSONObject result) {
        listData = VideoListData.newInstance(result.toString()); //result物件化
        VideoListAdapter adapter = new VideoListAdapter(this, listData.getVideoList());
        recyclerView.setAdapter(adapter);
    }
}