package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/***  ******
 * 教學影片
 * * *** *****/
public class TeachVideoActivity extends AppCompatActivity {

    private static final String VIDEO_SAMPLE =
            "http://192.168.1.108/health_education/video/video.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_video);

//        VideoView videoView =(VideoView)findViewById(R.id.videoView);
//
//        //Creating MediaController
//        MediaController mediaController= new MediaController(this);
//
//        // set anchor view for video view
//        mediaController.setAnchorView(videoView);
//
//        // set the media controller for video view
//        videoView.setMediaController(mediaController);
//
//        //set the uri for the video view
//        Uri uri = Uri.parse(VIDEO_SAMPLE);
//        videoView.setVideoURI(uri);
//
//        // start a video
//        videoView.start();

    }
}