package com.example.yhyhealthydemo.tools;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class yhyBleService extends Service {

    private static final String TAG = "yhyBleService";

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }
}
