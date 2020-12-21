package com.example.yhyhealthydemo.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.example.yhyhealthydemo.R;

import java.util.ArrayList;

import static android.content.Context.BLUETOOTH_SERVICE;

public class BodyTemperatureDialog extends Dialog implements View.OnClickListener {

    private final static String TAG = "BodyTemperatureDialog";
    private Context context;
    private Activity activity;

    private Button bleStartScan;
    private Button bleStopScan;
    private ListView listView;
    private ArrayList<String> deviceName;
    private ListAdapter listAdapter;

    //ble相關宣告
    private BluetoothManager bleManager;
    private BluetoothAdapter bleAdapter;
    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<BluetoothDevice>();

    private Handler handler;

    /**
     * 自定義 Dialog listener
     * **/
    public interface TemperatureListener{
        void setActivity(String name, String gender, String birthday);
    }

    private TemperatureListener listener;

    public BodyTemperatureDialog(Context context, int theme, TemperatureListener listener){
        super(context, theme);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_bleconnect, null);
        setContentView(view);

        //設置dialog大小
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics(); //獲取螢幕的寬跟高

        layoutParams.width = (int) (displayMetrics.widthPixels * 0.8); //寬度設置為螢幕的0.8
        window.setAttributes(layoutParams);

        //init ble
        bleManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        bleAdapter = bleManager.getAdapter();

        //init view
        listView = view.findViewById(R.id.listview);

        bleStartScan = view.findViewById(R.id.btnBleSubmit);
        bleStopScan = view.findViewById(R.id.btnBleCancel);
        bleStartScan.setOnClickListener(this);
        bleStopScan.setOnClickListener(this);

        deviceName = new ArrayList<String>();
        listAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_expandable_list_item_1, deviceName);
        listView.setAdapter(listAdapter);
//        listView.setOnItemClickListener(new OnItemClickListener());
        handler = new Handler();
        ScanDevice(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnBleSubmit:  //開始搜尋Ble
                //權限跟位置假設都有開啟了,先做搜尋功能
                ScanDevice(true);
                Log.d(TAG, "onClick: btnBleSubmit");
                break;
            case R.id.btnBleCancel:  //停止搜尋Ble

                dismiss();
                break;
        }
    }

    private void ScanDevice(boolean enable) {
        if(enable){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bleAdapter.stopLeScan(mleScanCallback);
                    Log.d(TAG, "ScanDevice: stop search in 10mins");
                }
            }, 10*1000);

            bleAdapter.stopLeScan(mleScanCallback);
            Log.d(TAG, "ScanDevice: start search");
        }else {
            bleAdapter.stopLeScan(mleScanCallback);
            Log.d(TAG, "ScanDevice: stop search");
        }
    }

    private BluetoothAdapter.LeScanCallback mleScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] bytes) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!bluetoothDevices.contains(device)){
                        Log.d(TAG, "run: " + bluetoothDevices);
                        bluetoothDevices.add(device);
                        deviceName.add(device.getName() + "rssi" + rssi + "\r\n" + device.getAddress());
                        ((BaseAdapter)listAdapter).notifyDataSetChanged();
                    }
                }
            });
        }
    };
}
