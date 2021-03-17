package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/*****
 * 系統設定 - 系統設定
 * 溫度單位
 * 藍芽 dialog
 */

public class SystemSettingActivity extends DeviceBaseActivity implements View.OnClickListener {

    private ImageView back;
    private ImageView BleSetting;
    private RecyclerView rvBleListView;

    //藍芽
    private yhyBleService mBleService;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_setting);

        initView();
    }

    private void initView() {
        back = findViewById(R.id.ivBackSysSetting);
        BleSetting = findViewById(R.id.ivToBleSetting);
        rvBleListView = findViewById(R.id.rvSettingBleList);
        back.setOnClickListener(this);
        BleSetting.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackSysSetting:
                finish();
                break;
            case R.id.ivToBleSetting:
                //藍芽設定
                //startActivity(new Intent(this, SysBleActivity.class));
                if(ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    initBle();  //初始化藍芽
                }else {
                    requestPermission();
                }
                break;
        }
    }


    //初始化藍芽
    private void initBle() {
        //啟用藍芽配適器
        BluetoothManager mBluetoothManager = (BluetoothManager)this.getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if(mBluetoothAdapter == null){ //如果==null，利用finish()取消程式。
            Toast.makeText(getBaseContext(),R.string.No_sup_Bluetooth,Toast.LENGTH_SHORT).show();
            finish();
            return;
        }else if (!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable(); //自動啟動藍芽
        }

        //dialog or Activity for ble search 開始掃描
        dialogBleConnect();
    }

    //掃描藍芽設備
    private void dialogBleConnect() {


    }
}