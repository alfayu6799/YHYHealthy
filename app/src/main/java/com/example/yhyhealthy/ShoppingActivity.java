package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //禁止旋轉

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
            case "zh-TW": //2021/06/21 指定蝦皮商城
                shoppingWeb = "https://shopee.tw/AINIITA-A-%E5%94%BE%E6%B6%B2%E6%8E%92%E5%8D%B5%E6%AA%A2%E6%B8%AC%E5%84%80%E6%99%BA%E8%83%BD%E5%88%86%E6%9E%90-%E5%A5%B3%E6%80%A7%E5%82%99%E5%AD%95-%E6%97%A5%E5%B8%B8%E8%AD%B7%E7%90%86-%E5%A9%A6%E7%A7%91%E4%BF%9D%E5%81%A5-%E5%A5%B3%E6%80%A7%E7%94%9F%E7%90%86%E7%94%A8%E5%93%81-i.275569571.7650627573";
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