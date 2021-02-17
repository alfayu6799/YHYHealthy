package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.example.yhyhealthydemo.module.ApiProxy;

public class ArticleDetailActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

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