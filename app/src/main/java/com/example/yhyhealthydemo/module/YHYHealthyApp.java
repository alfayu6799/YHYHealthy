package com.example.yhyhealthydemo.module;

import android.app.Application;

public class YHYHealthyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ApiProxy.initial(this);
    }
}
