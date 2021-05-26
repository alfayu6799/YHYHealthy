package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ScrollView;

public class PrivacyActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, ViewTreeObserver.OnScrollChangedListener {

    private Button agree, notAgree;
    private ScrollView scrollView;
    private WebView webView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        agree = findViewById(R.id.btnAgree);
        notAgree = findViewById(R.id.btnNoAgree);
        agree.setOnClickListener(this);
        notAgree.setOnClickListener(this);

        scrollView = findViewById(R.id.scrollView);
        scrollView.setOnTouchListener(this);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(this);
        webView = findViewById(R.id.webView);
        webView.loadData(getResources().getString(R.string.privacy_content), "text/html", null);
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
