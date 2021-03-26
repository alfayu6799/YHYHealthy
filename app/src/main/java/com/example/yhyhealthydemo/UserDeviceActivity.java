package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.yhyhealthydemo.module.ApiProxy;

/****
 * 裝置序號 (排卵儀)
 * 功能:
 *  查詢列表
 *  綁定新增
 *  解除綁定
 * */

public class UserDeviceActivity extends AppCompatActivity {

    private static final String TAG = "UserDeviceActivity";

    //api
    private ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_device);
    }
}