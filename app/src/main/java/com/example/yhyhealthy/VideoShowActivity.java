package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.jetbrains.annotations.NotNull;

import pl.droidsonroids.gif.GifImageView;

/***
 * 影片播放
 * 第三方套件 YouTubePlayerView
 * */
public class VideoShowActivity extends AppCompatActivity {

    private static final String TAG = "VideoShowActivity";

    private YouTubePlayerView playerView;

    //背景動畫
    private GifImageView gifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_show);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //禁止旋轉

        initView();

        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null){
            String fileName = bundle.getString("FILE");
            loadVideo(fileName);
        }
    }

    private void initView() {
        playerView = findViewById(R.id.youtubePlayerView);

        //動畫background
        gifImageView = findViewById(R.id.game_gif);
        gifImageView.setBackgroundResource(R.mipmap.yhy_new_background);
    }

    private void loadVideo(String videoId) {
        playerView.enterFullScreen(); //全螢幕
        playerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NotNull YouTubePlayer youTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        playerView.exitFullScreen();
        playerView.release();
    }
}