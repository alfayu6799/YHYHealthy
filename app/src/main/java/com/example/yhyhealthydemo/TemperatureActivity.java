 package com.example.yhyhealthydemo;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.yhyhealthydemo.adapter.BluetoothLeAdapter;
import com.example.yhyhealthydemo.adapter.RemoteViewAdapter;
import com.example.yhyhealthydemo.adapter.TemperMainAdapter;
import com.example.yhyhealthydemo.data.ScannedData;
import com.example.yhyhealthydemo.datebase.RemoteAccountApi;
import com.example.yhyhealthydemo.datebase.TempDataApi;
import com.example.yhyhealthydemo.dialog.ChartDialog;
import com.example.yhyhealthydemo.module.ApiProxy;
import com.example.yhyhealthydemo.tools.ByteUtils;
import com.example.yhyhealthydemo.tools.SpacesItemDecoration;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import es.dmoral.toasty.Toasty;
import static com.example.yhyhealthydemo.module.ApiProxy.BLE_USER_ADD_VALUE;
import static com.example.yhyhealthydemo.module.ApiProxy.BLE_USER_LIST;
import static com.example.yhyhealthydemo.module.ApiProxy.REMOTE_USER_ADD;
import static com.example.yhyhealthydemo.module.ApiProxy.REMOTE_USER_LIST;
import static com.example.yhyhealthydemo.module.ApiProxy.REMOTE_USER_UNDER_LIST;

public class TemperatureActivity extends DeviceBaseActivity implements View.OnClickListener, TemperMainAdapter.TemperMainListener {

    private final static String TAG = "TemperatureActivity";

    private Button supervise, remote;
    private Button addTemperatureUser, addRemoteUser;
    private TextView txtUserInfoEdit;
    private Button   selectedAccount;
    private TextView accountSelected;

    //觀測者
    private RecyclerView recyclerView;
    private List<TempDataApi.SuccessBean> dataList;
    private TemperMainAdapter tAdapter;

    //
    private TempDataApi.SuccessBean statusMemberBean;   //for ble連線狀態用
    private int statusPos;                              //for ble連線狀態用位置
    private String bleUserName;
    //private ArrayMap<String, Integer> userMap = new ArrayMap<>();

    //遠端
    private RecyclerView remoteRecycle;
    private RemoteViewAdapter remoteAdapter;
    private List<RemoteAccountApi.SuccessBean> remoteList;
    private ArrayAdapter<String> arrayAdapter;
    private String accountInfoClicked;

    //藍芽相關
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private yhyBleService mBluetoothLeService;
    private BroadcastReceiver mBleReceiver;
    private boolean isScanning = false;
    private ArrayList<ScannedData> findDevice = new ArrayList<>();
    private BluetoothLeAdapter tempAdapter;
    private Handler mHandler = new Handler();
    private AlertDialog alertDialog;
    private List<String> bleOnClickList = new ArrayList<>();

    //圖表dialog
    private ChartDialog chartDialog;

    //api
    ApiProxy proxy;

    //進度條
    private ProgressDialog progressDialog;

    //Other
    boolean isBleList = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        //休眠禁止
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        proxy = ApiProxy.getInstance();

        initView();

        requestTemp.run(); //體溫定時器

    }

    private void initView(){
        supervise = (Button) findViewById(R.id.bt_select_supervise);
        remote = (Button) findViewById(R.id.bt_select_remote);
        addTemperatureUser = (Button) findViewById(R.id.bt_add_temp);
        addRemoteUser = (Button) findViewById(R.id.bt_add_remote);
        txtUserInfoEdit = findViewById(R.id.tvEdit);
        selectedAccount = findViewById(R.id.btnChoseAccount);
        accountSelected = findViewById(R.id.tvAccountShow);

        //init RecyclerView's data
        recyclerView = findViewById(R.id.rvTempUser);
        remoteRecycle = findViewById(R.id.rvRomoteUser);  //遠端

        setInfo();        //觀測者初始化資訊

        supervise.setOnClickListener(this);
        remote.setOnClickListener(this);
        addTemperatureUser.setOnClickListener(this);
        addRemoteUser.setOnClickListener(this);
        txtUserInfoEdit.setOnClickListener(this);
        selectedAccount.setOnClickListener(this);

        supervise.setBackgroundResource(R.drawable.rectangle_button); //先顯示觀測Button
    }

    /**** 藍芽 2021/03/18 *****/
    private void initBle() {
        /**啟用藍牙適配器*/
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) return;

        if (!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();   //自動啟動藍芽

        /**開始掃描*/
        dialogBleConnect();
    }

    /**BLE開始掃描*/
    private void dialogBleConnect(){
        alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_bleconnect, null);
        RecyclerView bleRecyclerView = view.findViewById(R.id.rvBleScanView);

        /**設置Recyclerview列表*/
        tempAdapter = new BluetoothLeAdapter();
        bleRecyclerView.setAdapter(tempAdapter);
        bleRecyclerView.setHasFixedSize(true);
        bleRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        alertDialog.setView(view);
        alertDialog.setCancelable(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        tempAdapter.OnItemClick(itemClick);

        isScanning = true;
        if (isScanning){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    Log.d(TAG, "5秒停止搜尋: ");
                }
            }, 5000);
            isScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            findDevice.clear();
            tempAdapter.clearDevice();
        }else {
            isScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

        Button btnCancel = view.findViewById(R.id.btnBleCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                Log.d(TAG, "使用者自行取消搜尋功能 ");
                alertDialog.dismiss(); //關閉視窗
            }
        });

        alertDialog.show();
    }

    /**顯示掃描到物件*/
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            new Thread(()->{
                /**如果裝置沒有名字，就不顯示*/
                if (device.getName()!= null){
                    /**將搜尋到的裝置加入陣列*/
                    findDevice.add(new ScannedData(device.getName()
                            , String.valueOf(rssi)
                            , device.getAddress()));
                    /**將陣列中重複Address的裝置濾除，並使之成為最新數據*/
                    ArrayList newList = getSingle(findDevice);
                    runOnUiThread(()->{
                        /**將陣列送到RecyclerView列表中*/
                        tempAdapter.addDevice(newList);
                    });
                }
            }).start();
        }
    };

    /**濾除重複的藍牙裝置(以Address判定)*/
    private ArrayList getSingle(ArrayList list) {
        ArrayList tempList = new ArrayList<>();
        try {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                if (!tempList.contains(obj)) {
                    tempList.add(obj);
                } else {
                    tempList.set(getIndex(tempList, obj), obj);
                }
            }
            return tempList;
        } catch (ConcurrentModificationException e) {
            return tempList;
        }
    }

    /**
     * 以Address篩選陣列->抓出該值在陣列的哪處
     */
    private int getIndex(ArrayList temp, Object obj) {
        for (int i = 0; i < temp.size(); i++) {
            if (temp.get(i).toString().contains(obj.toString())) {
                return i;
            }
        }
        return -1;
    }

    /**取得欲連線之裝置後跳轉頁面*/
    private BluetoothLeAdapter.OnItemClick itemClick = new BluetoothLeAdapter.OnItemClick() {
        @Override
        public void onItemClick(ScannedData selectedDevice) {

            mBluetoothAdapter.stopLeScan(mLeScanCallback); //停止搜尋

            //啟動ble server連線
            mBluetoothLeService.connect(selectedDevice.getAddress());  //2021/03/30

            //關閉視窗
            if (alertDialog.isShowing())
                alertDialog.dismiss();
        }
    };


   ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //觀測者 Item 填入Data
    private void setInfo() {
        proxy.buildPOST(BLE_USER_LIST, "", bleUserListListener);
    }

    private ApiProxy.OnApiListener bleUserListListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(TemperatureActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            parserJson(result);
                        }else if (errorCode == 6){
                            Toasty.error(TemperatureActivity.this, getString(R.string.no_date), Toast.LENGTH_SHORT, true).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {
            progressDialog.dismiss();
        }
    };

    //解析後台回來的資料
    private void parserJson(JSONObject result) {
        TempDataApi tempDataApi = TempDataApi.newInstance(result.toString());
        dataList = tempDataApi.getSuccess();

        //將資料配置到Adapter並顯示出來
        tAdapter = new TemperMainAdapter(this, dataList, this);

        //設定item間距的距離
        int spacingInPixels = 10;
        recyclerView.setAdapter(tAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //設定item間距
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.bt_select_supervise:  //觀測Button
                supervise.setBackgroundResource(R.drawable.rectangle_button);
                remote.setBackgroundResource(R.drawable.relative_shape);
                addTemperatureUser.setVisibility(View.VISIBLE);
                addRemoteUser.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                remoteRecycle.setVisibility(View.GONE);
                selectedAccount.setVisibility(View.GONE);
                accountSelected.setVisibility(View.GONE);
                isBleList = true;
                setInfo();
                break;
            case R.id.bt_select_remote:    //遠端Button
                remote.setBackgroundResource(R.drawable.rectangle_button);
                supervise.setBackgroundResource(R.drawable.relative_shape);
                addTemperatureUser.setVisibility(View.GONE);
                addRemoteUser.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                remoteRecycle.setVisibility(View.VISIBLE);
                selectedAccount.setVisibility(View.VISIBLE);
                isBleList = false;
                setAccountInfo();           //選擇帳號初始化  2021/03/25
                break;
            case R.id.bt_add_temp:       //新增觀測者onClick
                Intent intent = new Intent(this, TemperatureAddActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.bt_add_remote:     //新增遠端者onClick
                dialogRemote();
                break;
            case R.id.tvEdit:           //編輯使用者資訊
                if(isBleList) {
                    startActivity(new Intent(this, TemperEditListActivity.class));
                }else {
                    startActivity(new Intent(this, RemoteEditListActivity.class));
                }
                break;
            case R.id.btnChoseAccount:   //遠端監控者-選擇帳號
                setAccountInfo();        //先呼叫
                showAccountDialog();
                break;
        }
    }

    //查詢遠端帳號-選擇帳號
    private void setAccountInfo() {
        proxy.buildPOST(REMOTE_USER_LIST, "" , requestListener);
    }
    private ApiProxy.OnApiListener requestListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            new Thread(){
                @Override
                public void run() {
                    arrayAdapter = new ArrayAdapter<String>(TemperatureActivity.this, android.R.layout.select_dialog_item);
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            JSONArray array = object.getJSONArray("success");
                            for (int i = 0; i < array.length(); i++){
                                arrayAdapter.add(array.getString(i));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {

        }
    };

    //
    private void showAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.please_select_one_account));

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                getAccountInfoFromApi(arrayAdapter.getItem(position));
                accountInfoClicked = arrayAdapter.getItem(position);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    //取得監控帳戶底下觀測者的資料列表　　2021/03/26
    private void getAccountInfoFromApi(String accountNo) {
        JSONObject json = new JSONObject();
        try {
            json.put("account", accountNo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildPOST(REMOTE_USER_UNDER_LIST, json.toString(), remoteUnderListener);
    }

    private ApiProxy.OnApiListener remoteUnderListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(TemperatureActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            parserRemoteData(result);
                        }else if (errorCode == 6){
                            Toasty.error(TemperatureActivity.this, getString(R.string.you_select_account_is_no_data), Toast.LENGTH_SHORT, true).show();
                        }else if (errorCode == 32){
                            Toasty.error(TemperatureActivity.this, getString(R.string.remote_account_auth_code_error), Toast.LENGTH_SHORT, true).show();
                        }else {
                            Log.d(TAG, "後台回復之錯誤代碼:" + errorCode);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {
            progressDialog.dismiss();
        }
    };

    //顯示監控者底下觀測者量測的資料  2021/03/26
    private void parserRemoteData(JSONObject result) {
        RemoteAccountApi remoteData = RemoteAccountApi.newInstance(result.toString());
        remoteList = remoteData.getSuccess();

        //將資料配置到RecyclerView並顯示出來
        remoteAdapter = new RemoteViewAdapter(this, remoteList);
        remoteRecycle.setAdapter(remoteAdapter);
        remoteRecycle.setHasFixedSize(true);
        remoteRecycle.setLayoutManager(new LinearLayoutManager(this));

        //設定item間距的距離
        int spacingInPixels = 10;
        remoteRecycle.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //設定item間距

        //顯示選擇的帳號
        accountSelected.setVisibility(View.VISIBLE);
        accountSelected.setTextColor(Color.RED);
        accountSelected.setText(accountInfoClicked);
    }

    //更新藍芽連線狀態
    private void updateStatus(String name,String deviceName, String deviceAddress, String bleStatus){

        Log.d(TAG, "updateStatus: 裝置名稱:" + deviceName + " ,裝置狀態:" + bleStatus + " ,mac:" + deviceAddress
                + " ,position:" + statusPos + " 使用者:" + name);
        
        if (deviceName != null){
            statusMemberBean.setMac(deviceAddress);
            statusMemberBean.setStatus(deviceName+bleStatus);
            statusMemberBean.setUserName(name);
            statusMemberBean.setDeviceName(deviceName);
            tAdapter.updateItem(statusMemberBean, statusPos);
        }
    }

    //更新收到體溫的訊息給RecyclerView的項目
    private void updateBleData(double degree, double battery, String macAddress) {


        //溫度不為空
        if (degree != 0){
            Log.d(TAG, "updateItemByMac: 溫度:" + degree + " 裝置MAC:" + macAddress);
                //將溫度電量及mac傳到Adapter
                tAdapter.updateItemByMac(degree, battery, macAddress);

            //如果chart視窗存在就將使用者的資訊傳遞到ChartDialog
           // if (chartDialog != null && chartDialog.isShowing())
           //     chartDialog.update(memberBean);  //更新Dialog內的溫度圖表
        }


//        //上傳後端 2021/03/26
        //updateDegreeValueToApi(degree);
    }

    //command
    private void sendCommand(String deviceAddress) {
        String request = "AIDO,0"; //詢問溫度command/@3mins
        byte[] messageBytes = new byte[0];
        try {
            messageBytes = request.getBytes("UTF-8"); //Sting to byte

            mBluetoothLeService.writeDataToDevice(messageBytes, deviceAddress);  //2021/03/30
            //Log.d(TAG, "sendCommand: " + messageBytes + " device:" + deviceAddress);

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to convert message string to byte array");
        }
    }

    //每2分鐘執行一次
    private Runnable requestTemp = new Runnable() {
        @Override
        public void run() {

            if (!bleOnClickList.isEmpty()) {
                for (int i = 0 ; i < bleOnClickList.size(); i++){
                    sendCommand(bleOnClickList.get(i));
                    Log.d(TAG, "每2分鐘執行一次:" + bleOnClickList.get(i));
                }
            }
            mHandler.postDelayed(this, 1000 * 60 *2);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        //註冊藍芽接受器
        registerBleReceiver();
    }

    //註冊藍芽接受器
    private void registerBleReceiver() {
        Log.d(TAG, "註冊藍芽接受器");

        /** 綁定後台服務 ***/
        Intent intent = new Intent(this, yhyBleService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        mBleReceiver = new BleReceiver();
        registerReceiver(mBleReceiver, makeGattUpdateIntentFilter());
    }

    public static IntentFilter makeGattUpdateIntentFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(yhyBleService.ACTION_GATT_CONNECTED);
        filter.addAction(yhyBleService.ACTION_GATT_DISCONNECTED);  //斷開
        filter.addAction(yhyBleService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(yhyBleService.ACTION_DATA_AVAILABLE);
        filter.addAction(yhyBleService.ACTION_NOTIFY_ON);
        filter.addAction(yhyBleService.ACTION_CONNECTING_FAIL);
        filter.addAction(yhyBleService.EXTRA_MAC);
        filter.addAction(yhyBleService.EXTRA_DEVICE_NAME);
        return filter;
    }

    /** ble Service 背景服務 **/
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBluetoothLeService = ((yhyBleService.LocalBinder) iBinder).getService();

            //auto connect to the device upon successful start-up init
//            mBluetoothLeService.connect(mBluetoothAdapter, deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
//            mBluetoothLeService.connect(mBluetoothAdapter, deviceAddress);
        }
    };

    /**
     * 藍牙信息接收器
     */
    private class BleReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }

            String deviceName = intent.getStringExtra(yhyBleService.EXTRA_DEVICE_NAME);
            String macAddress = intent.getStringExtra(yhyBleService.EXTRA_MAC);
            byte[] data = intent.getByteArrayExtra(yhyBleService.EXTRA_DATA);

            switch (action) {

                case yhyBleService.ACTION_GATT_CONNECTED:
                    Toasty.info(TemperatureActivity.this, "藍芽連接中...", Toast.LENGTH_SHORT, true).show();
                    break;

                case yhyBleService.ACTION_GATT_DISCONNECTED:
                    Toasty.info(TemperatureActivity.this, "藍芽已斷開並釋放資源", Toast.LENGTH_SHORT, true).show();
                    mBluetoothLeService.disconnect();
                    mBluetoothLeService.release();
                    updateStatus(bleUserName,deviceName , macAddress,getString(R.string.ble_unconnected));  //藍芽設備已斷開
                    mHandler.removeCallbacks(requestTemp);
                    break;

                case yhyBleService.ACTION_CONNECTING_FAIL:
                    Toasty.info(TemperatureActivity.this, "藍芽已斷開", Toast.LENGTH_SHORT, true).show();
                    mBluetoothLeService.disconnect();
                    updateStatus(bleUserName,deviceName , macAddress,getString(R.string.ble_unconnected));  //藍芽設備已斷開
                    mHandler.removeCallbacks(requestTemp);
                    break;

                case yhyBleService.ACTION_NOTIFY_ON:  //03/30
                    Log.d(TAG, "onReceive: 收到BLE通知服務 啟動成功: " + macAddress + "裝置名稱:" + deviceName);
                    updateStatus(bleUserName,deviceName, macAddress, getString(R.string.ble_connect_status)); //更新
                    break;
                    
                case yhyBleService.ACTION_DATA_AVAILABLE:
                    Log.d(TAG, "onReceive: 體溫原始資料:" + ByteUtils.byteArrayToString(data) + " mac:" + macAddress);

                    String[] str = ByteUtils.byteArrayToString(data).split(","); //以,分割
                    String degreeStr = str[2];
                    String batteryStr = str[3];
                    double degree = Double.parseDouble(degreeStr)/100;
                    double battery = Double.parseDouble(batteryStr);
                    updateBleData(degree, battery, macAddress); //更新體溫跟電量
                    break;

                default:
                    break;
            }
        }
    }

    //2021/03/25 update觀測者之體溫量測資料給後端
    private void updateDegreeValueToApi(double degree, int targetId){
        DateTime dt1 = new DateTime();
        String degreeMeasureStr = dt1.toString("yyyy-MM-dd,HH:mm:ss");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("targetId",targetId);
            jsonObject.put("celsius", degree);
            jsonObject.put("measuredTime",degreeMeasureStr);
            jsonObject.put("first", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonObject);

        JSONObject object = new JSONObject();
        try {
            object.put("infos", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "updateDegreeValueToApi: " + object.toString());
        proxy.buildPOST(BLE_USER_ADD_VALUE, object.toString(), addBleValueListener);
    }

    private ApiProxy.OnApiListener addBleValueListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            Toasty.success(TemperatureActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                        }else {
                            Toasty.error(TemperatureActivity.this, getString(R.string.json_error_code) + errorCode , Toast.LENGTH_SHORT, true).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy:");
        if (mBluetoothLeService != null){
            unregisterReceiver(mBleReceiver);
            mBleReceiver = null;
            mBluetoothLeService.disconnect();
            mBluetoothLeService.release();
        }
        unbindService(mServiceConnection);
        mBluetoothLeService = null;

        if(mHandler != null)
            mHandler.removeCallbacks(requestTemp);
    }

    //遠端帳號新增彈跳視窗
    private void dialogRemote() {
        AlertDialog remoteDialog = new AlertDialog.Builder(this).create();
        LayoutInflater remoteLayout = LayoutInflater.from(this);
        View remoteView = remoteLayout.inflate(R.layout.dialog_remote_add, null);
        remoteDialog.setView(remoteView);
        remoteDialog.setCancelable(false); //禁用非視窗區

        EditText account = remoteView.findViewById(R.id.edtOtherAccount);
        EditText authCode = remoteView.findViewById(R.id.edtAuthorization);

        Button cancel = remoteView.findViewById(R.id.btnRemoteCancel);
        Button submit = remoteView.findViewById(R.id.btnRemoteSend);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remoteDialog.dismiss();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //檢查資料是否齊全
               if (TextUtils.isEmpty(account.getText().toString()))
                   return;
               if (TextUtils.isEmpty(authCode.getText().toString()))
                   return;

               //傳送到後台
                updateRemoteToApi(account, authCode);
            }
        });

        remoteDialog.show();
    }

    //後台新增遠端觀測者資料 2021/03/19
    private void updateRemoteToApi(EditText account, EditText authCode) {
        JSONObject json = new JSONObject();
        try {
            json.put("account", account.getText().toString());
            json.put("monitorCode", authCode.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(REMOTE_USER_ADD, json.toString(), remoteAddListener);
    }

    private ApiProxy.OnApiListener remoteAddListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            try {
                JSONObject object = new JSONObject(result.toString());
                int errorCode = object.getInt("errorCode");
                if (errorCode == 0){
                    Toasty.success(TemperatureActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                    setAccountInfo();
                }else {
                    Log.d(TAG, "新增觀測者結果後台回覆碼:" + errorCode);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {

        }
    };

    @Override   //藍芽連線interface
    public void onBleConnect(TempDataApi.SuccessBean data, int position) {
        //呼叫藍芽
        bleUserName = data.getUserName();
        statusPos = position;           //取得使用者在RecyclerView項目位置
        statusMemberBean = data;        //在把data內的資料丟給memberBean;
        initBle();
    }

    @Override  //啟動量測 interface 2021/03/30
    public void onBleMeasuring(TempDataApi.SuccessBean data) {

        bleOnClickList.add(data.getMac());

        sendCommand(data.getMac());

    }

    @Override  //症狀input
    public void onSymptomRecord(TempDataApi.SuccessBean data, int position) {
        Log.d(TAG, "onSymptomRecord: clicked " + data.getMac() + ",position:" + position );

    }

    @Override  //更新數據到後台
    public void passTarget(int targetId, double degree) {
        //Log.d(TAG, "passTarget: " + targetId + ",degree:" + degree);
        updateDegreeValueToApi(degree, targetId);
    }

    @Override   //呼叫圖表interface
    public void onBleChart(TempDataApi.SuccessBean data, int position) {
        //客製Dialog圖表
        chartDialog = new ChartDialog(this, data);
        chartDialog.setCancelable(false); //點擊屏幕或物理返回鍵，dialog不消失
        chartDialog.show();
    }

    @Override //新增觀測者資料返回 2021/03/24
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            if (resultCode == RESULT_OK){  //新增觀測者資料成功
                setInfo(); //跟後台要資料刷新RecyclerView
            }else if (resultCode == RESULT_CANCELED){  //取消新增觀測者資料
                Toasty.info(this, getString(R.string.nothing), Toast.LENGTH_SHORT, true).show();
            }
        }
    }
}


