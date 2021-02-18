package com.example.yhyhealthydemo;

import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.example.yhyhealthydemo.adapter.SecretionTypeAdapter;
import com.example.yhyhealthydemo.adapter.SymptomAdapter;
import com.example.yhyhealthydemo.adapter.TasteAdapter;
import com.example.yhyhealthydemo.datebase.ChangeRecord;
import com.example.yhyhealthydemo.datebase.MenstruationRecord;
import com.example.yhyhealthydemo.module.RecordSymptom;
import com.example.yhyhealthydemo.module.RecordTaste;
import com.example.yhyhealthydemo.module.RecordType;
import com.example.yhyhealthydemo.module.ApiProxy;
import com.example.yhyhealthydemo.tools.MyGridView;
import com.example.yhyhealthydemo.module.RecordColor;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static com.example.yhyhealthydemo.module.ApiProxy.RECORD_INFO;

/*********
 * 排卵紀錄資訊
 * 照相 CameraActivity
 * 藍芽
 * 權限繼承DeviceBaseActivity
 **********/
public class PeriodRecordActivity extends DeviceBaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = PeriodRecordActivity.class.getSimpleName();

    TextView  textRecordDate;
    Button    takePhoto, startMeasure, saveSetting;
    ImageView searchBLE;
    ImageView photoShow;
    TextView  textBleStatus;
    TextView  textAnalysis;

    private AlertDialog alertDialog;

    String path;
    String strDay;

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
    EditText   editWeight;    //體重
    TextView   textBodyTemp; //體溫

    private Switch bleeding, breastPain, intercourse;

    //Api
    private ApiProxy proxy;
    private MenstruationRecord record;
    private ChangeRecord changeRecord;

    //日期格式
    SimpleDateFormat sdf;

    //顏色,氣味,症狀,型態 Adapter
    private SymptomAdapter sAdapter;
    private SecretionTypeAdapter tAdapter;
    private TasteAdapter aAdapter;
    private ColorAdapter cAdapter;
    private String[] types;  //型態
    private String[] symps;  //症狀
    private String[] taste;  //氣味
    private String[] colors; //顏色

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_period);

        path = getIntent().getStringExtra("path");      //照相回來的參數

        //日期格式
        sdf = new SimpleDateFormat("yyyy-MM-dd");

        changeRecord = new ChangeRecord();
        //init
        initView();

        //取的來自OvulationActivity的資料
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            strDay = bundle.getString("DAY");
            if (strDay == null){  //避免從相機拍照完回來後日期為空 2021/02/18
                DateTime day = new DateTime(new Date());
                String today = day.toString("yyyy-MM-dd");
                strDay = today;
            }else {
                textRecordDate.setText(strDay);
            }
            setRecordInfo(strDay);  //以使用者點擊的日期為key
        }
    }

    @SuppressLint("ClickableViewAccessibility")
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
        editWeight = findViewById(R.id.edtWeight);
        editWeight.setInputType(InputType.TYPE_NULL); //hide keyboard
        editWeight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                editWeight.setInputType(InputType.TYPE_CLASS_NUMBER);
                editWeight.onTouchEvent(event);
               return true;
            }
        });

        gridViewColor = findViewById(R.id.gvColor);
        gridViewTaste = findViewById(R.id.gvTaste);
        gridViewType = findViewById(R.id.gvType);
        gridViewSymptom = findViewById(R.id.gvSymptom);

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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnPhoto:      //拍照OnClick
                    checkIsToday();  //檢查日期
                break;
            case R.id.ivBLESearch:   //藍芽搜尋
                if(ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    initBle();
                }else {
                    requestPermission();
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
            case R.id.btnSaveSetting: //將資料收集完後上傳至後台
                checkBeforeUpdate();
                break;
        }
    }

    private void checkBeforeUpdate() {
        //體重
        if(!TextUtils.isEmpty(editWeight.getText().toString())){
            Log.d(TAG, "checkBeforeUpdate: " + editWeight.getText().toString());
            changeRecord.getMeasure().setTemperature(26);
            //record.getSuccess().getMeasure().setWeight(Double.parseDouble(editWeight.getText().toString()));
//            changeRecord.getMeasure().setWeight(Double.parseDouble(editWeight.getText().toString()));
        }

        //體溫
        //record.getSuccess().getMeasure().setTemperature(Double.parseDouble(textBodyTemp.getText().toString()));
//        changeRecord.getMeasure().setTemperature(Double.parseDouble(textBodyTemp.getText().toString()));
        UpdateApi();
    }

    //拍照辨識需檢查是否當日 2021/02/18
    private void checkIsToday() {
        DateTime today = new DateTime(new Date());
        String todayStr = today.toString("yyyy-MM-dd");
        if (strDay.equals(todayStr)){ //限當日可以拍照
            openCamera();
        }else {
            Toasty.info(PeriodRecordActivity.this,getString(R.string.camera_only_today), Toast.LENGTH_SHORT, true).show();
        }
    }

    //開啟相機功能
    private void openCamera() {
        if(ActivityCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED){
            Intent camera = new Intent(PeriodRecordActivity.this, CameraActivity.class);
            startActivity(camera);
            finish();
        }else {
            requestPermission();
        }
    }

    //上傳至後台儲存 2021/01/11 leona
    private void UpdateApi() {
        Log.d(TAG, "上傳到後台的資料 : " + changeRecord.toJSONString());
        //2021/02/19 will design

        Toast.makeText(this, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
        finish();
    }

    //Switch button listener  2021/01/07 leona
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
        switch (compoundButton.getId()){
            case R.id.swBleeding:   //出血
                if (isCheck){
                    record.getSuccess().getStatus().setBleeding(true);
                }else {
                    record.getSuccess().getStatus().setBleeding(false);
                }
                break;
            case R.id.swBreastPain: //脹痛
                if(isCheck){
                    record.getSuccess().getStatus().setBreastPain(true);
                }else {
                    record.getSuccess().getStatus().setBreastPain(false);
                }
                break;
            case R.id.swIntercourse: //行房
                if (isCheck){
                    record.getSuccess().getStatus().setIntercourse(true);
                }else {
                    record.getSuccess().getStatus().setIntercourse(false);
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
                Toast.makeText(PeriodRecordActivity.this, getString(R.string.ble_start_scanner), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(PeriodRecordActivity.this, getString(R.string.ble_stop_in_10), Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"ScanFunction() : Stop Scan");
                }
            },SCAN_TIME); //10後要執行此Runnable

            mScanning = true; //搜尋旗標設為true
            mBluetoothAdapter.startLeScan(mLeScanCallback);//開始搜尋BLE設備
            Toast.makeText(PeriodRecordActivity.this, getString(R.string.ble_start_scanner), Toast.LENGTH_SHORT).show();
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
//                        Log.d(TAG, "run: " + mBluetoothDevices);
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
            gatt = device.connectGatt(PeriodRecordActivity.this, false, gattcallback);
            //gatt = mBluetoothDevice.connectGatt(PeriodRecordActivity.this, false, gattcallback);
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
                double bodyDegree = Double.parseDouble(temp)/100;  //25.0
                textBodyTemp.setText(String.valueOf(bodyDegree));
                Log.d(TAG, "onCharacteristicChanged: Characteristic get value : " + bodyDegree);  //result : AIDO,0,2500,100
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

    //向後台要求資料 2021/01/08 leona
    private void setRecordInfo(String selectDay) {
        proxy = ApiProxy.getInstance(); //api實體化

        //取得日期資訊
        JSONObject json = new JSONObject();
        try {
            json.put("testDate",selectDay);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildPOST(RECORD_INFO, json.toString(), requestListener);
    }

    private ApiProxy.OnApiListener requestListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parserJson(result);
                }
            });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {

        }
    };

    //解析後台資料
    private void parserJson(JSONObject result) {
        record = MenstruationRecord.newInstance(result.toString());
        Log.d(TAG, "解析後台資料: " + record.toJSONString());
        //體重
        String userWeight = String.valueOf(record.getSuccess().getMeasure().getWeight());
        editWeight.setText(userWeight);

        //脹痛,出血,行房
        boolean BeastPain = record.getSuccess().getStatus().isBreastPain();
        breastPain.setChecked(BeastPain);
        boolean Bleeding = record.getSuccess().getStatus().isBleeding();
        bleeding.setChecked(Bleeding);
        boolean Intercourse = record.getSuccess().getStatus().isIntercourse();
        intercourse.setChecked(Intercourse);

        //顏色,狀態,氣味,症狀
        setSecretion();
    }

    private void setSecretion() {
        //顏色
        colors = new String[]{ getString(R.string.normal), getString(R.string.white), getString(R.string.yellow),
                getString(R.string.milky), getString(R.string.brown), getString(R.string.greenish_yellow)};

        cAdapter = new ColorAdapter(this);

        String secretionsColor = record.getSuccess().getSecretions().getColor();
        RecordColor recordColor = RecordColor.getColor(secretionsColor);
        int pos_color = recordColor.getIndex();
        cAdapter.setData(colors, pos_color);
        gridViewColor.setAdapter(cAdapter);

        gridViewColor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                cAdapter.setSelection(position);   //傳直更新
                cAdapter.notifyDataSetChanged();

                RecordColor recordColor = RecordColor.getEnName(position);
                String ColorName = recordColor.getName();
                record.getSuccess().getSecretions().setColor(ColorName); //寫回後台
            }
        });

        //味道
        taste = new String[]{ getString(R.string.normal), getString(R.string.fishy), getString(R.string.stink)};

        aAdapter = new TasteAdapter(this);

        String secretionsTaste = record.getSuccess().getSecretions().getSmell();
        RecordTaste recordTaste = RecordTaste.getTaste(secretionsTaste);
        int pos_taste = recordTaste.getIndex();
        aAdapter.setData(taste, pos_taste);

        gridViewTaste.setAdapter(aAdapter);
        gridViewTaste.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                aAdapter.setSelection(position);   //傳直更新
                aAdapter.notifyDataSetChanged();

                RecordTaste recordTaste = RecordTaste.getEnName(position);
                String TasteName = recordTaste.getName();
                record.getSuccess().getSecretions().setSmell(TasteName); //寫回後台
            }
        });

        //型態
        types = new String[]{ getString(R.string.normal), getString(R.string.thick),
                getString(R.string.liquid_milky), getString(R.string.liquid)};

        tAdapter = new SecretionTypeAdapter(this);

        String secretionsType = record.getSuccess().getSecretions().getSecretionType();
        RecordType recordType = RecordType.getType(secretionsType);
        int pos_type = recordType.getIndex();
        tAdapter.setData(types, pos_type);

        gridViewType.setAdapter(tAdapter);
        gridViewType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                tAdapter.setSelection(position);   //傳值更新
                tAdapter.notifyDataSetChanged();
                RecordType recordType = RecordType.getEnName(position);
                String TypeName = recordType.getName();
                record.getSuccess().getSecretions().setSecretionType(TypeName); //寫回後台
            }
        });

        //症狀
        symps = new String[]{ getString(R.string.normal), getString(R.string.hot),getString(R.string.allergy),
                getString(R.string.pain)};

        sAdapter = new SymptomAdapter(this);

        String secretionsSymptom = record.getSuccess().getSecretions().getSymptom();
        RecordSymptom recordSymptom = RecordSymptom.getSymptom(secretionsSymptom);
        int pos_symptom = recordSymptom.getIndex();
        sAdapter.setData(symps,pos_symptom);

        gridViewSymptom.setAdapter(sAdapter);
        gridViewSymptom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                sAdapter.setSelection(position);   //傳直更新
                sAdapter.notifyDataSetChanged();

                RecordSymptom recordSymptom = RecordSymptom.getEnName(position);
                String SymptomName = recordSymptom.getName();
                record.getSuccess().getSecretions().setSymptom(SymptomName);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (gatt == null)
            return;
        gatt.close();
        gatt = null;
        Toast.makeText(PeriodRecordActivity.this, getString(R.string.ble_is_not_connect), Toast.LENGTH_SHORT).show();
    }
}
