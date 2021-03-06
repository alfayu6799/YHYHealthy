package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import pl.droidsonroids.gif.GifImageView;

public class ArticleDetailActivity extends AppCompatActivity {

    private WebView webView;
    //背景動畫
    private GifImageView gifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //禁止旋轉

        //動畫background
        gifImageView = findViewById(R.id.game_gif);
        gifImageView.setBackgroundResource(R.mipmap.yhy_new_background);

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            String html = bundle.getString("HTML");
            webView.loadUrl(html);
        }
        
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}