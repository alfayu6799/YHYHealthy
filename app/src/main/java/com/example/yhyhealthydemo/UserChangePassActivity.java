package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * 使用者變更密碼
 * */

public class UserChangePassActivity extends AppCompatActivity implements View.OnClickListener {

    Button update;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userchange_passwd);

        initView();
    }

    private void initView() {
        update = findViewById(R.id.btnUpdatePassWD);
        back = findViewById(R.id.ivBackSetting5);
        update.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackSetting5: //回上頁
                finish();
                break;
            case R.id.btnUpdatePassWD:
                updateToApi();       //上傳到後台更新
                break;

        }
    }

    //後台更新
    private void updateToApi() {
    }
}