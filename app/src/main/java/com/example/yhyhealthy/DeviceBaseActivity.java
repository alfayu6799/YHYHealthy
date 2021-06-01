package com.example.yhyhealthy;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.yhyhealthy.module.yhyBleService;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.os.Build.VERSION_CODES.M;

/***
 * 權限類別
 * */

public class DeviceBaseActivity extends AppCompatActivity {

    private static final String TAG = "DeviceBaseActivity";

    public static final int REQUEST_CODE = 100;
    private String[] neededPermissions = new String[]{CAMERA, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};

    /** BLE連線工具 **/
    private yhyBleService mBluetoothLeService;

    protected String deviceMac;

    private boolean serviceBound = false;

    @Override
    protected void onStart() {
        super.onStart();

        //取得上次連線成功的藍芽mac
        //deviceMac = readCatch(deviceAddress);

        requestPermission();
    }

    protected void requestPermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= M ){
            ArrayList<String> permissionsNotGranted = new ArrayList<>();
            for (String permission : neededPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNotGranted.add(permission);
                }
            }
            if (permissionsNotGranted.size() > 0) {
                boolean shouldShowAlert = false;
                for (String permission : permissionsNotGranted) {
                    shouldShowAlert = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                }
                if (shouldShowAlert) {
                    showPermissionAlert(permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]));
                } else {
                    requestPermissions(permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]));
                }
            }
            Log.d(TAG, "requestPermission: 取得權限");
//            initBleService();  //啟動BLE背景服務
        }
    }

    private void requestPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
    }

    //權限dialog
    private void showPermissionAlert(final String[] permissions) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(R.string.permission_required);
        alertBuilder.setMessage(R.string.permission_message);
        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(permissions);
            }
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    /**
     * 取得權限判斷
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CODE:
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        Toasty.error(DeviceBaseActivity.this, R.string.permission_warning, Toast.LENGTH_LONG, true).show();
                        return;
                    }
                }
                //BLE背景服務
//                initBleService();
                break;
        }
    }

    private void initBleService() {
        /** 綁定後台服務 ***/
        if (!serviceBound) {
            Log.d(TAG, "initBleService: OK");
            Intent intent = new Intent(this, yhyBleService.class);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /** ble Service 背景服務 **/
    public ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceBound = true;
            mBluetoothLeService = ((yhyBleService.LocalBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBound = false;
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if (serviceBound){ //假如service沒註冊之修正
            Log.d(TAG, "onStop: DeviceBase");
            unbindService(mServiceConnection);
            serviceBound = false;
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (serviceBound){ //假如service沒註冊之修正
//            Log.d(TAG, "onDestroy: DeviceBase");
//            unbindService(mServiceConnection);
//            serviceBound = false;
//        }
//    }
}
