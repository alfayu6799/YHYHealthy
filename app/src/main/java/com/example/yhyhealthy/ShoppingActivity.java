package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/***
 * 購物網站 webView
 *      台灣 - 蝦皮
 *      大陸 - 淘寶
 *      歐美 - Amazon
 * create 2021/05/18
 * */

public class ShoppingActivity extends AppCompatActivity {

    private String shoppingWeb;

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        webView = findViewById(R.id.web_view);

        //取得手機語系
        getLocalLanguage();

        initData();

    }

    //取得手機語系
    private void getLocalLanguage() {
        String language = getResources().getConfiguration().locale.getLanguage();
        String country = getResources().getConfiguration().locale.getCountry();
        String defaultLan = language + "-" + country;
        switch (defaultLan){
            case "zh-TW":
                shoppingWeb = "https://shopee.tw/";
                break;
            case "zh-CN":
                shoppingWeb = "https://world.taobao.com/";
                break;
            default:
                shoppingWeb = "https://www.amazon.com/";
                break;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initData() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient()); //不調用系統的瀏覽器
        webView.loadUrl(shoppingWeb);
    }

    @Override  //返回上一頁
    public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()){
            webView.goBack();
            return true;
         }
        return super.onKeyDown(keyCode, event);
    }
}