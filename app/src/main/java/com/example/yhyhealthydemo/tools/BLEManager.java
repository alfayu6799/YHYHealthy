package com.example.yhyhealthydemo.tools;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import java.util.ArrayList;

public class BLEManager {

    private static final String TAG = "BLEManager";

    private Activity activity;
    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;

    private ArrayList<BluetoothDevice> mBluetoothDevices = new ArrayList<>();
    private ArrayList<String> deviceName = new ArrayList<String>();
    private ListAdapter listAdapter;

    private boolean isScanning = false;
    private Handler mHandler = new Handler();

    public BLEManager(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }



    ////////////////////////////////藍芽搜尋////////////////////////////////////////////////

    /**
     * 搜索藍芽設備
     *
     * @param enable 開始搜索或停止搜索
     */
    public void scanBleDevice(final boolean enable) {
        if (enable) {
            //開始搜索設備，搜索到設備會執行回調接口mLeScanCallback
            bluetoothAdapter.startLeScan(mLeScanCallback);
            isScanning = true;
            //搜索設備十分耗電，為了避免長時間搜索，這裡設置10s超時停止搜索
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanBleDevice(false);
                    Toast.makeText(context, "搜索超時，請重試", Toast.LENGTH_SHORT).show();
                }
            }, 10 * 1000);
        } else {
            bluetoothAdapter.stopLeScan(mLeScanCallback);
            isScanning = false;
        }
    }

    /**
     * 設備搜索回調
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if(activity != null){
                    //回調不是在ui線程中執行的，但是ble設備的連接、斷開最好在ui線程中執行，否則可能會出現些奇奇怪怪的問題
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //判斷設備是否是我們要找的
//                                if (!mBluetoothDevices.contains(device)){
//                                    deviceName.add(device.getName() + " rssi " + rssi + "\r\n" + device.getAddress());
//                                }
//                                if (!TextUtils.isEmpty(device.getName()) && device.getName().equals(
//                                        "AIDO")) {
////                                //找到設備後停止搜索，並取消開始搜索時設置的超時取消搜索
//                                    mHandler.removeCallbacksAndMessages(null);
//                                    bluetoothAdapter.stopLeScan(mLeScanCallback);
//////                                if (isScanning) {
//////                                    //開始連接設備
////////                                    connect(device.getAddress());
//////                                    isScanning = false;
//////                                }
////
//                                }
                            } //end of run
                        });
                    }
                }
            };


    /////////////////////////////////藍芽初始化/////////////////////////////////////////////////////////

    /**
     * 初始化
     * @param context
     */
    public boolean initBle(Context context){
        if(!checkBle(context)){
            return false;
        }else{
            return true;
        }
    }

    /**
     * 檢測手機是否支持4.0藍牙
     * @param context  上下文
     * @return true--支持4.0 false--不支持4.0
     */
    private boolean checkBle(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {  //API 18 Android 4.3
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if(bluetoothManager == null){
                return false;
            }
            bluetoothAdapter = bluetoothManager.getAdapter();  //BLUETOOTH权限
            if(bluetoothAdapter == null){
                return false;
            }else{
                Log.d(TAG,"該設備支持藍牙4.0");
                return true;
            }
        }else{
            return false;
        }
    }

    /**
     * 獲取藍牙狀態
     */
    public boolean isEnable(){
        if(bluetoothAdapter == null){
            return false;
        }
        return bluetoothAdapter.isEnabled();
    }

    /**
     * 打開藍牙
     * @param isFast  true 直接打開藍牙 false 提示用戶打開
     */
    public void openBluetooth(Context context,boolean isFast){
        if(!isEnable()){
            if(isFast){
                Log.d(TAG,"直接打開手機藍牙");
                bluetoothAdapter.enable();  //BLUETOOTH_ADMIN权限
            }else{
                Log.d(TAG,"提示用戶去打開手機藍牙");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivity(enableBtIntent);
            }
        }else{
            Log.d(TAG,"手機藍牙狀態已開啟");
        }
    }

}
