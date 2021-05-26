package com.example.yhyhealthy.module;

import android.app.Application;

public class YHYHealthyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ApiProxy.initial(this);
    }
}
