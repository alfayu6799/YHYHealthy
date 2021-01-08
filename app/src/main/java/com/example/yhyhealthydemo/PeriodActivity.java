package com.example.yhyhealthydemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthydemo.adapter.ColorAdapter;
import com.example.yhyhealthydemo.adapter.GridViewAdapter;
import com.example.yhyhealthydemo.adapter.SecretionTypeAdapter;
import com.example.yhyhealthydemo.adapter.SymptomAdapter;
import com.example.yhyhealthydemo.adapter.TasteAdapter;
import com.example.yhyhealthydemo.datebase.MenstruationRecord;
import com.example.yhyhealthydemo.tools.MyGridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION_CODES.M;

/*********
 * 排卵紀錄資訊
 * 照相
 * 藍芽
 * 權限
 **********/
public class PeriodActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = PeriodActivity.class.getSimpleName();

    TextView  textRecordDate;
    Button    takePhoto, startMeasure, saveSetting;
    ImageView searchBLE;
    ImageView photoShow;
    TextView  textBleStatus;
    TextView  textBodyTemp;
    TextView  textAnalysis;

    private AlertDialog alertDialog;

    public static final int REQUEST_CODE = 100;
    private String[] neededPermissions = new String[]{CAMERA, WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};
    boolean result;
    String path;

    //藍芽
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattService gattService;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic characteristic;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int SCAN_TIME = 10000; //10秒
    private ArrayList<BluetoothDevice> mBluetoothDevices = new ArrayList<BluetoothDevice>();
    private ListView scanlist;
    private ArrayList<String> deviceName;
    private ListAdapter listAdapter;
    private boolean mScanning = false;
    private Handler mHandler;  //Handler用來搜尋Devices10秒後，自動停止搜尋
    private static final UUID TEMPERATURE_SERVICE_UUID = UUID
            .fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID TEMPERATURE_NOTIF_UUID = UUID
            .fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID TEMPERATURE_WRITE_DATA = UUID
            .fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");

    //使用者自行輸入區
    MyGridView gridViewColor, gridViewTaste, gridViewType, gridViewSymptom;
    EditText   weight;    //體重
    String ColorId = "0";
    String TasteId = "0";
    String TypeId  = "0";
    String SympId  = "0";

    private Switch bleeding, breastPain, intercourse;

    //Api
    private MenstruationRecord record;

    //時間
    SimpleDateFormat sdf;

    //
    private SymptomAdapter sAdapter;
    private SecretionTypeAdapter tAdapter;
    private TasteAdapter aAdapter;
    private ColorAdapter cAdapter;
    private String[] types;
    private String[] symps;
    private String[] taste;
    private String[] colors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_period);

        path = getIntent().getStringExtra("path");

        sdf = new SimpleDateFormat("yyyy-MM-dd");

        initView();

        //取的來自OvulationActivity的資料
        Intent intent = this.getIntent();
        String strDay = intent.getStringExtra("DAY");
        textRecordDate.setText(strDay);
        setRecordInfo(strDay);  //以使用者點擊的日期為key

        checkPermission(); //權限check
    }

    private void initView() {
        textRecordDate = findViewById(R.id.tvRecordDate);
        takePhoto = findViewById(R.id.btnPhoto);
        searchBLE = findViewById(R.id.ivBLESearch);
        startMeasure = findViewById(R.id.btnStartMeasure);
        saveSetting = findViewById(R.id.btnSaveSetting);
        photoShow = findViewById(R.id.ivPhoto);
        textBleStatus = findViewById(R.id.tvBLEConnectStatus);
        textBodyTemp = findViewById(R.id.tvBodyTemp);
        textAnalysis = findViewById(R.id.tvAnalysis);

        bleeding = findViewById(R.id.swBleeding);       //出血
        breastPain = findViewById(R.id.swBreastPain);   //脹痛
        intercourse = findViewById(R.id.swIntercourse); //行房

        //體重自行輸入
        weight = findViewById(R.id.edtWeight);
        weight.setInputType(InputType.TYPE_NULL); //hide keyboard

        //顏色
        gridViewColor = findViewById(R.id.gvColor);
        setColorData();

        //味道
        gridViewTaste = findViewById(R.id.gvTaste);
        setTasteData();

        //型態
        gridViewType = findViewById(R.id.gvType);
        setTypeData();

        //症狀
        gridViewSymptom = findViewById(R.id.gvSymptom);
        setSymptomData();

        if(path != null){
            photoShow.setImageURI(Uri.fromFile(new File(path)));
        }

        takePhoto.setOnClickListener(this);
        searchBLE.setOnClickListener(this);
        startMeasure.setOnClickListener(this);
        saveSetting.setOnClickListener(this);
        startMeasure.setOnClickListener(this);

        bleeding.setOnCheckedChangeListener(this);
        breastPain.setOnCheckedChangeListener(this);
        intercourse.setOnCheckedChangeListener(this);
    }

    //症狀
    private void setSymptomData() {
        symps = new String[]{ getString(R.string.normal), getString(R.string.allergy), getString(R.string.hot),
                getString(R.string.pain)};

        sAdapter = new SymptomAdapter(this);

        sAdapter.setData(symps, 0);     //導入資料並指定default position
        gridViewSymptom.setAdapter(sAdapter);
        gridViewSymptom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                sAdapter.setSelection(position);   //傳直更新
                sAdapter.notifyDataSetChanged();
                Log.d(TAG, "onItemClick: setSymptomData = " + position);
                SympId = "4" + position;
            }
        });
    }

    //型態
    private void setTypeData() {
        types = new String[]{ getString(R.string.normal), getString(R.string.liquid), getString(R.string.thick),
                        getString(R.string.liquid_milky)};

        tAdapter = new SecretionTypeAdapter(this);

        tAdapter.setData(types, 0);     //導入資料並指定default position
        gridViewType.setAdapter(tAdapter);
        gridViewType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                tAdapter.setSelection(position);   //傳值更新
                tAdapter.notifyDataSetChanged();
                Log.d(TAG, "onItemClick: setTypeData = " + position);
                TypeId = "3" + position;
            }
        });
    }

    private void setTasteData() {
        taste = new String[]{ getString(R.string.normal), getString(R.string.fishy), getString(R.string.stink)};

        aAdapter = new TasteAdapter(this);

        aAdapter.setData(taste, 0);     //導入資料並指定default position
        gridViewTaste.setAdapter(aAdapter);
        gridViewTaste.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                aAdapter.setSelection(position);   //傳直更新
                aAdapter.notifyDataSetChanged();
                Log.d(TAG, "onItemClick: setTasteDate = " + position);
                TasteId = "2" + position;
            }
        });
    }

    private void setColorData() {
        colors = new String[]{ getString(R.string.normal), getString(R.string.brown), getString(R.string.yellow),
                getString(R.string.milky), getString(R.string.white), getString(R.string.greenish_yellow)};

        cAdapter = new ColorAdapter(this);

        cAdapter.setData(colors, 0);     //導入資料並指定default position
        gridViewColor.setAdapter(cAdapter);
        gridViewColor.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                cAdapter.setSelection(position);   //傳直更新
                cAdapter.notifyDataSetChanged();
                Log.d(TAG, "onItemClick: setColorData = " + position);
                ColorId = "1" + position;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnPhoto:      //拍照
                result = checkPermission();
                if (result) {
                    Intent camera = new Intent(PeriodActivity.this, CameraActivity.class);
                    startActivity(camera);
                    finish();
                }
                break;
            case R.id.ivBLESearch:   //藍芽搜尋
                result = checkPermission(); //要求權限(BLE要求local位置權限)
                if(result){
                    initBle();
                }
                break;
            case R.id.btnStartMeasure: //開始量測
                if (characteristic != null) { //確保write uuid要有資料才能寫資料
                    characteristic = gattService.getCharacteristic(TEMPERATURE_WRITE_DATA); //AIDO寫入uuid : 64e00002
                    Log.d(TAG, "onClicked: characteristic");
                    String request = "AIDO,0"; //詢問溫度command
                    byte[] messageBytes = new byte[0];
                    try {
                        messageBytes = request.getBytes("UTF-8"); //Sting to byte
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Failed to convert message string to byte array");
                    }
                    characteristic.setValue(messageBytes);    //詢問溫度Command
                    gatt.writeCharacteristic(characteristic);
                }
                break;
            case R.id.btnSaveSetting: //將資料上傳至後台
                Toast.makeText(this, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "上傳的資料 = " + ColorId + "," + TasteId + "," + TypeId + "," + SympId);
                finish();
                break;
        }
    }

    //Switch button listener  2021/01/07 leona
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
        switch (compoundButton.getId()){
            case R.id.swBleeding:   //出血
                if (isCheck){
                    Log.d(TAG, "onCheckedChanged: Bleeding");
                }
                break;
            case R.id.swBreastPain: //脹痛
                if(isCheck){
                    Log.d(TAG, "onCheckedChanged: BreastPain");
                }
                break;
            case R.id.swIntercourse: //行房
                if (isCheck){
                    Log.d(TAG, "onCheckedChanged: Intercourse");
                }
                break;
        }
    }

    //ble init
    private void initBle() {
        //取得BluetoothAdapter，如果BluetoothAdapter==null，則該手機不支援Bluetooth
        //取得Adapter之前，需先使用BluetoothManager，此為系統層級需使用getSystemService
        mBluetoothManager = (BluetoothManager)this.getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if(mBluetoothAdapter == null){ //如果==null，利用finish()取消程式。
            Toast.makeText(getBaseContext(),R.string.No_sup_Bluetooth,Toast.LENGTH_SHORT).show();
            finish();
            return;
        }else if (!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable(); //啟動藍芽
        }

        //dialog or Activity for ble search
        dialogBleConnect();
    }

    //Ble search
    private void dialogBleConnect() {
        alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_bleconnect, null);
        alertDialog.setView(view);
        alertDialog.setCancelable(false);  //disable touch screen area only cancel's button can close dialog
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //dialog背景透明

        //search BLE
        scanlist = view.findViewById(R.id.listview);
        //ArrayList屬性為String，用來裝Devices Name
        deviceName = new ArrayList<String>();
        //ListView使用的Adapter
        listAdapter = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_expandable_list_item_1,deviceName);
        //將listView綁上Adapter
        scanlist.setAdapter(listAdapter);
        scanlist.setOnItemClickListener(new onItemClickListener()); //綁上OnItemClickListener，設定ListView點擊觸發事件
        mHandler = new Handler();
        ScanFunction(true); //使用ScanFunction(true) 開啟BLE搜尋功能

        Button bleSubmit = view.findViewById(R.id.btnBleSubmit);
        bleSubmit.setOnClickListener(new View.OnClickListener() {  //BLE搜尋button
            @Override
            public void onClick(View view) {
                ScanFunction(true);
                Toast.makeText(PeriodActivity.this, getString(R.string.ble_start_scanner), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "dialog : 使用者自行啟動搜尋功能");
            }
        });

        Button bleCancel = view.findViewById(R.id.btnBleCancel);
        bleCancel.setOnClickListener(new View.OnClickListener() { //停止搜尋BLE裝置並關閉此dialog
            @Override
            public void onClick(View view) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                Log.d(TAG, "dialog : 使用者自行取消搜尋功能");
                alertDialog.dismiss();  //關閉此dialog
            }
        });

        alertDialog.show();

    }

    //此為ScanFunction，輸入函數為boolean，如果true則開始搜尋，false則停止搜尋
    private void ScanFunction(boolean enable){
        if(enable){
            mHandler.postDelayed(new Runnable() { //啟動一個Handler，並使用postDelayed在10秒後自動執行此Runnable()
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);//停止搜尋
                    mScanning = false; //搜尋旗標設為false
                    Toast.makeText(PeriodActivity.this, getString(R.string.ble_stop_in_10), Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"ScanFunction() : Stop Scan");
                }
            },SCAN_TIME); //10後要執行此Runnable

            mScanning = true; //搜尋旗標設為true
            mBluetoothAdapter.startLeScan(mLeScanCallback);//開始搜尋BLE設備
            Toast.makeText(PeriodActivity.this, getString(R.string.ble_start_scanner), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "ScanFunction() : Start Scan");
        }
        else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    //建立一個BLAdapter的Callback，當使用startLeScan或stopLeScan時，每搜尋到一次設備都會跳到此callback
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() { //使用runOnUiThread方法，其功能等同於WorkThread透過Handler將資訊傳到MainThread(UiThread)中，
                @Override
                public void run() {
                    if (!mBluetoothDevices.contains(device)) { //利用contains判斷是否有搜尋到重複的device
                        Log.d(TAG, "run: " + mBluetoothDevices);
                        mBluetoothDevices.add(device);         //如沒重複則添加到bluetoothdevices中
                        if(Math.abs(rssi) <= 90) {             //過濾信號小於-90的設備
                            deviceName.add(device.getName() + " rssi:" + rssi + "\r\n" + device.getAddress()); //將device的Name、rssi、address裝到此ArrayList<Strin>中
                            ((BaseAdapter) listAdapter).notifyDataSetChanged();     //使用notifyDataSetChanger()更新listAdapter的內容
                        }
                    }
                }
            });
        }

    };

    //ListView ItemClick的Listener，當按下Item時，將該Item的BLE Name與Address包起來，將送到另一Activity中建立連線
    private class onItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            //mBluetoothDevices為一個陣列資料ArrayList<BluetoothDevices>，使用.get(positon)取得
            //Item位置上的BluetoothDevice
            //final BluetoothDevice mBluetoothDevice = mBluetoothDevices.get(position);
            Log.d(TAG, "onItemClick: You click ble device Name is : " + mBluetoothDevices.get(position).getName()); //獲取本機藍芽名

            String bleAddress = mBluetoothDevices.get(position).getAddress();
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bleAddress);
            Log.d(TAG, "onItemClick: " + bleAddress);

            //關閉藍芽搜尋
            mBluetoothAdapter.stopLeScan(mLeScanCallback);//停止搜尋

            //關閉dialog  20201208
            if (alertDialog.isShowing()){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();
                    }
                });
            }

            //連上藍芽設備後就開始動作了....
            gatt = device.connectGatt(PeriodActivity.this, false, gattcallback);
            //gatt = mBluetoothDevice.connectGatt(PeriodActivity.this, false, gattcallback);
            Log.d(TAG, "onItemClick: 點擊後就開始啟動gattcallback");
        }
    }

    //gattCallBack 服務啟動寫入讀取等等都在此設定
    private BluetoothGattCallback gattcallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "onConnectionStateChange: status " + status + " newStatus =" + newState);

            switch(status){
                case BluetoothGatt.GATT_SUCCESS:
                    Log.w(TAG,"BluetoothGatt.GATT_SUCCESS");
                    break;
                case BluetoothGatt.GATT_FAILURE:
                    Log.w(TAG,"BluetoothGatt.GATT_FAILURE");
                    break;
                case BluetoothGatt.GATT_CONNECTION_CONGESTED:
                    Log.w(TAG,"BluetoothGatt.GATT_CONNECTION_CONGESTED");
                    break;
                case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION:
                    Log.w(TAG,"BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION");
                    break;
                case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION:
                    Log.w(TAG,"BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION");
                    break;
                case BluetoothGatt.GATT_INVALID_OFFSET:
                    Log.w(TAG,"BluetoothGatt.GATT_INVALID_OFFSET");
                    break;
                case BluetoothGatt.GATT_READ_NOT_PERMITTED:
                    Log.w(TAG,"BluetoothGatt.GATT_READ_NOT_PERMITTED");
                    break;
                case BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED:
                    Log.w(TAG,"BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED");
                    break;
            }

            BluetoothDevice bluetoothDevice = gatt.getDevice();
            Log.d(TAG,"連接的設備名稱：" + bluetoothDevice.getName() + ",藍芽MAC = " + bluetoothDevice.getAddress());

            if (newState == BluetoothGatt.STATE_CONNECTED){
                Log.d(TAG, "連結成功 : 跑到discoverServices");
                gatt.discoverServices();  //啟動服務
            }else if (newState == BluetoothGatt.STATE_DISCONNECTED){
                Log.d(TAG, "斷開連結並釋放資源");
                gatt.close();
            }else if (newState == BluetoothGatt.STATE_CONNECTING){
                Log.d(TAG, "正在連結.." );

            }else if (newState == BluetoothGatt.STATE_DISCONNECTING){
                Log.d(TAG, "正在斷開..");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) { //發現服務
            Log.d(TAG, String.format("onServicesDiscovered:%s,%s", gatt.getDevice().getName(), status));
            if (status == gatt.GATT_SUCCESS) {  //發現BLE服務成功
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gattService = gatt.getService(TEMPERATURE_SERVICE_UUID);  //AIDO主要的服務 : 64e00001
                        if(gattService != null){
                            Log.d(TAG, "onServicesDiscovered get 6e400001 success !!");
                            characteristic = gattService.getCharacteristic(TEMPERATURE_NOTIF_UUID);  //AIDO啟動通知協定 : 64e00003
                            if (characteristic != null ){
                                Log.d(TAG, "onServicesDiscovered get 6e400003 success !!");
                                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()){
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);  //啟動notif通知
                                    boolean sucess = gatt.writeDescriptor(descriptor);
                                    Log.d(TAG, "onServicesDiscovered : writeDescriptor = " + sucess);
                                }
                                gatt.setCharacteristicNotification(characteristic, true); //notif listener
                                //連接顯示
                                textBleStatus.setTextColor(Color.RED);
                                textBleStatus.setText(getString(R.string.ble_connect_status));
                            }
                        }
                    }
                });
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicRead: 讀Characteristic");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicWrite: 寫Characteristic 成功");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (characteristic.getValue() != null){
                startMeasure.setText("再次量測");
                String result = new String(characteristic.getValue());
                String[] str = result.split(",");
                String temp = str[2];
                double bodytemp = Double.parseDouble(temp)/100;  //25.0
                textBodyTemp.setText(String.valueOf(bodytemp));
                Log.d(TAG, "onCharacteristicChanged: Characteristic get value : " + bodytemp);  //result : AIDO,0,2500,100
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorRead: DescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorWrite: DescriptorWrite");
        }

    };

    //權限check
    private boolean checkPermission() {
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
                return false;
            }
        }
        return true;
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
                        Toast.makeText(PeriodActivity.this, R.string.permission_warning, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                break;
        }
    }

    //向後台要求資料 2021/01/08 leona
    private void setRecordInfo(String selectDay) {
        String myJSONStr = loadJSONFromAsset("menstruation_record_0108.json");
        /*
        new Thread() {
            @Override
            public void run() {
                MediaType JSON = MediaType.parse("application/json;charset=utf-8");
                JSONObject json = new JSONObject();
                try {
                    json.put("type", "3");
                    json.put("userId", "H5E3q5MjA=");
//                    json.put("testDate",selectDay);
                    json.put("testDate","2019-10-01");  //有資料
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 建立OkHttpClient
                OkHttpClient okHttpClient = new OkHttpClient();

                RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));

                // 建立Request，設置連線資訊
                Request request = new Request.Builder()
                        .url("http://192.168.1.108:8080/allAiniita/aplus/RecordInfo")
                        .addHeader("Authorization","xxx")
                        .post(requestBody)
                        .build();

                // 執行Call連線到網址
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // 連線失敗
                        Log.d(TAG, "PeriodActivity onFailure: " + e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // 連線成功，自response取得連線結果
                        String result = response.body().string();  //字串
                        Log.d(TAG, "回給我的資料: " + result);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                parserJson(result); //解析後台資料
                            }
                        });
                    }
                });
            }
        }.start();
        */

        parserJson(myJSONStr);
    }

    //解析後台資料
    private void parserJson(String JsonResult) {
        record = MenstruationRecord.newInstance(JsonResult);
//        String BeastPain = record.getStatus().getBeastPain();        //脹痛
//        String Bleeding = record.getStatus().getBleeding();          //出血
//        String Intercourse = record.getStatus().getIntercourse();    //行房
        String secretionsColor = record.getSecretions().getColor();
        Log.d(TAG, "parserJson: " + secretionsColor);

//        if(BeastPain.equals("Y")){
//            breastPain.setChecked(true);
//        }
//
//        if (Bleeding.equals("Y")){
//            bleeding.setChecked(true);
//        }
//
//        if (Intercourse.equals("Y")){
//            intercourse.setChecked(true);
//        }

        if (secretionsColor.equals("milky")){
            cAdapter.setData(colors,3);
        }
    }

    //讀取local json file
    public String loadJSONFromAsset(String fileName)
    {
        String json;
        try
        {
            InputStream is = getApplicationContext().getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex)
        {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (gatt == null)
            return;
        gatt.close();
        gatt = null;
        Toast.makeText(PeriodActivity.this, getString(R.string.ble_is_not_connect), Toast.LENGTH_SHORT).show();
    }

}
