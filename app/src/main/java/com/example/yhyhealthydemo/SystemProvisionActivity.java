package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ScrollView;

/***** ****
 * 設定 - 條款
 * *****/

public class SystemProvisionActivity extends AppCompatActivity {

    private ImageView back;
    private ScrollView scrollView;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_provision);

        back = findViewById(R.id.imageViewBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        scrollView = findViewById(R.id.scViewProvision);
        webView = findViewById(R.id.webViewProvision);
        webView.loadData(getResources().getString(R.string.privacy_content), "text/html", null);
    }
}