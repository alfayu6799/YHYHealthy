package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import pl.droidsonroids.gif.GifImageView;

/***** ****
 * 設定 - 條款
 * *****/

public class SystemProvisionActivity extends AppCompatActivity {

    private ImageView back;
    private TextView privacyContent;
    //背景動畫
    private GifImageView gifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_provision);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //禁止旋轉

        back = findViewById(R.id.imageViewBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //動畫background
        gifImageView = findViewById(R.id.game_gif);
        gifImageView.setBackgroundResource(R.mipmap.yhy_new_background);
        
        privacyContent = findViewById(R.id.tv_provision);
        privacyContent.setText(Html.fromHtml(readHTML()));
    }

    //讀取HTML文本 2021/05/28 修改
    private String readHTML(){
        InputStream inputStream;
        if (getResources().getConfiguration().locale.getCountry().contains("TW")) {
            inputStream = getResources().openRawResource(R.raw.policy);    //台灣
        }else if (getResources().getConfiguration().locale.getCountry().contains("CN")){
            inputStream = getResources().openRawResource(R.raw.policy_cn); //大陸
        }else { //歐美
            inputStream = getResources().openRawResource(R.raw.policy_en);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            int i = inputStream.read();
            while (i != -1){
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toString();
    }
}