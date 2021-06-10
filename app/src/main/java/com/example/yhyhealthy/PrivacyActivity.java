package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *  使用者授權合約
 **/
public class PrivacyActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, ViewTreeObserver.OnScrollChangedListener {

    private Button agree, notAgree;
    private ScrollView scrollView;
    private TextView privacyContent;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        initView();
    }

    private void initView() {
        agree = findViewById(R.id.btnAgree);
        notAgree = findViewById(R.id.btnNoAgree);
        agree.setOnClickListener(this);
        notAgree.setOnClickListener(this);

        scrollView = findViewById(R.id.scrollView);
        scrollView.setOnTouchListener(this);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(this);
        privacyContent = findViewById(R.id.privacy_Content);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnAgree: //同意
                startActivity(new Intent(getBaseContext(), RegisterActivity.class)); //註冊頁面
                finish(); //結束此頁面
                break;
            case R.id.btnNoAgree: //不同意
                finish(); //返回login
                break;
        }
    }

    public void onScrollChanged(){
        View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
        int topDetector = scrollView.getScrollY();
        int bottomDetector = view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY());
        if(bottomDetector == 0 ){ //底
            agree.setVisibility(View.VISIBLE);
            notAgree.setVisibility(View.VISIBLE);
        }
        if(topDetector <= 0){  //頂
            //Toast.makeText(getBaseContext(),"Scroll View top reached",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }
}
