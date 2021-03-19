package com.example.yhyhealthydemo;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yhyhealthydemo.adapter.DegreeAdapter;
import com.example.yhyhealthydemo.adapter.RecyclerViewAdapter;
import com.example.yhyhealthydemo.adapter.RemoteViewAdapter;
import com.example.yhyhealthydemo.adapter.TempViewAdapter;
import com.example.yhyhealthydemo.data.Remote;
import com.example.yhyhealthydemo.data.ScannedData;
import com.example.yhyhealthydemo.datebase.DegreeUserData;
import com.example.yhyhealthydemo.dialog.AddTemperatureDialog;
import com.example.yhyhealthydemo.dialog.ChartDialog;
import com.example.yhyhealthydemo.datebase.Member;
import com.example.yhyhealthydemo.module.ApiProxy;
import com.example.yhyhealthydemo.tools.ByteUtils;
import com.example.yhyhealthydemo.tools.RecyclerViewListener;
import com.example.yhyhealthydemo.tools.SpacesItemDecoration;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthydemo.module.ApiProxy.BLE_USER_LIST;
import static com.example.yhyhealthydemo.module.ApiProxy.REMOTE_USER_ADD;

public class TemperatureActivity extends DeviceBaseActivity implements View.OnClickListener, RecyclerViewListener {

    private final static String TAG = "TemperatureActivity";

    private Button supervise, remote;
    private Button addTemperatureUser, addRemoteUser;

    //使用者
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter;
    private List<Member> members;
    private String name;

    private DegreeAdapter degreeAdapter;
    private List<DegreeUserData.SuccessBean> degreeUserDataList;
    private Member user1; //假資料
    private Member user2; //假資料
    private Member user3; //假資料
    private Member user4; //假資料

    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm"); //日期格式

    //遠端
    private RecyclerView remoteRecycle;
    private RemoteViewAdapter remoteAdapter;
    private List<Remote> remotes;
    private Remote remote1;  //假資料
    private Remote remote2;  //假資料

    //藍芽相關
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private yhyBleService mBluetoothLeService;
    private BroadcastReceiver mBleReceiver;
    private BluetoothGatt mBluetoothGatt;
    private boolean isScanning = false;
    private ArrayList<ScannedData> findDevice = new ArrayList<>();
    private TempViewAdapter tempAdapter;
    private Handler mHandler;
    private AlertDialog alertDialog;
    private String deviceName = "";     //裝置名稱
    private String deviceAddress = "";  //裝置mac

    //圖表dialog
    private ChartDialog chartDialog;

    //將資料寫到sharePreferences
    private SharedPreferences temperatureInfo;

    //api
    ApiProxy proxy;

    //進度條
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        //休眠禁止
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //init sharepreferences
        temperatureInfo = getSharedPreferences("temperature", MODE_PRIVATE); //只允許本應用程式內存取

        proxy = ApiProxy.getInstance();

        initView();
    }

    private void initView(){
        supervise = (Button) findViewById(R.id.bt_select_supervise);
        remote = (Button) findViewById(R.id.bt_select_remote);
        addTemperatureUser = (Button) findViewById(R.id.bt_add_temp);
        addRemoteUser = (Button) findViewById(R.id.bt_add_remote);

        //init RecyclerView's data
        recyclerView = findViewById(R.id.rvTempUser);
        remoteRecycle = findViewById(R.id.rvRomoteUser);  //遠端

        setInfo();       //觀測者初始化資訊
        setRemote();     //監控者初始化資訊

        supervise.setOnClickListener(this);
        remote.setOnClickListener(this);
        addTemperatureUser.setOnClickListener(this);
        addRemoteUser.setOnClickListener(this);

        supervise.setBackgroundResource(R.drawable.rectangle_button); //先顯示觀測Button
    }

    private void setRemote() {
        int spacingInPixels = 10;  //設定item間距的距離
        remotes = new ArrayList<>();

        setRemoteUser();  //填入資料

        remoteAdapter = new RemoteViewAdapter(this, remotes);

        remoteRecycle.setAdapter(remoteAdapter);
        remoteRecycle.setHasFixedSize(true);
        remoteRecycle.setLayoutManager(new LinearLayoutManager(this));
        remoteRecycle.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //設定item間距
    }

    private void setRemoteUser() {
        //日後要從後台拿取資料
        remote1 = new Remote(R.mipmap.imageview, "Matt Bomer", 38.50);
        remote2 = new Remote(R.mipmap.imageview2, "Brad Pitt", 35.55);

        remotes.add(remote1);
        remotes.add(remote2);
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

    private void dialogBleConnect(){
        alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_bleconnect, null);
        RecyclerView bleRecyclerView = view.findViewById(R.id.rvBleScanView);

        /**設置Recyclerview列表*/
        tempAdapter = new TempViewAdapter();
        bleRecyclerView.setAdapter(tempAdapter);
        bleRecyclerView.setHasFixedSize(true);
        bleRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        alertDialog.setView(view);
        alertDialog.setCancelable(true);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        tempAdapter.OnItemClick(itemClick);

        isScanning = true;
        mHandler = new Handler();
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
    private TempViewAdapter.OnItemClick itemClick = new TempViewAdapter.OnItemClick() {
        @Override
        public void onItemClick(ScannedData selectedDevice) {

            mBluetoothAdapter.stopLeScan(mLeScanCallback); //停止搜尋

            deviceName = selectedDevice.getDeviceName();
            deviceAddress = selectedDevice.getAddress();

            //啟動ble server連線
            mBluetoothLeService.connect(mBluetoothAdapter, selectedDevice.getAddress());

            //關閉視窗
            if (alertDialog.isShowing())
                alertDialog.dismiss();
        }
    };


   ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //RecyclerView's Item 填入Data
    private void setInfo() {
        int spacingInPixels = 10;  //設定item間距的距離

        degreeUserDataList = new ArrayList<>();

        members = new ArrayList<>();

        setMemberDate(); //填入資料

        mAdapter = new RecyclerViewAdapter(this, members, this);
        //degreeAdapter = new DegreeAdapter(this, degreeUserDataList);
        recyclerView.setAdapter(mAdapter);
        //recyclerView.setAdapter(degreeAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //設定item間距
    }

    private void setMemberDate() {  //日後要從api拉回照片跟姓名等資料
        //proxy.buildPOST(BLE_USER_LIST, "", bleUserListListener);

        user1 = new Member(R.mipmap.imageview, "Matt Bomer", "未連線");
        user2 = new Member(R.mipmap.imageview2, "Brad Pitt", "未連線");
        user3 = new Member(R.mipmap.imageview3, "Anne Hathaway", "未連線");
        user4 = new Member(R.mipmap.image4, "Emma Watson", "未連線");

        members.add(user1);
        members.add(user2);
        members.add(user3);
        members.add(user4);
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
        Log.d(TAG, "解析後台回來的資料: " + result.toString());

        DegreeUserData degreeUserData = DegreeUserData.newInstance(result.toString());

        degreeUserDataList = degreeUserData.getSuccess();

        for (int i = 0; i < degreeUserDataList.size(); i++){

//            String name = degreeUserData.getSuccess().get(i).getName();
            degreeUserDataList.add(i, degreeUserData.getSuccess().get(i));
        }
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
                break;
            case R.id.bt_select_remote:    //遠端Button
                remote.setBackgroundResource(R.drawable.rectangle_button);
                supervise.setBackgroundResource(R.drawable.relative_shape);
                addTemperatureUser.setVisibility(View.GONE);
                addRemoteUser.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                remoteRecycle.setVisibility(View.VISIBLE);
                break;
            case R.id.bt_add_temp:       //新增觀測者onClick
//                dialogTemperature();
                startActivity(new Intent(this, TemperatureAddActivity.class));
                break;
            case R.id.bt_add_remote:     //新增遠端者onClick
                dialogRemote();
                break;
        }
    }

    private void updateStatus(String name, String deviceName, String deviceAddress, String bleStatus){
        Log.d(TAG, "updateStatus: 姓名:" + name + " 裝置名稱:" + deviceName + " 裝置狀態:" + bleStatus + "裝置Address:" + deviceAddress);
        //改更藍芽狀態的文字顯示
        if(this.name != null){
            for (int j = 0; j < members.size(); j++){
                if (members.get(j).getName().equals(name)){
                    Member user = members.get(j);
                    user.setStatus(deviceName + bleStatus);
                    user.setDeviceName(deviceName);
                    user.setMac(deviceAddress);
                    members.set(j, user);
                    mAdapter.updateItem(user, j);
                }
            }
        }
    }

    //更新收到體溫的訊息給RecyclerView的項目
    private void updateBleData(double degree, double battery) {
        String currentDateTime = sdf.format(new Date());  // 目前時間

        //姓名不為空
        if (name != null){
            for(int i = 0; i < members.size(); i++){
                if (members.get(i).getName().equals(name)){
                    Member user = members.get(i);
                    user.setDegree(degree, currentDateTime);
                    user.setBattery(String.valueOf(battery)+"%");
                    members.set(i, user);
                    mAdapter.updateItem(user, i);

                    //如果chart視窗存在就將使用者的資訊傳遞到ChartDialog 20201218
                    if (chartDialog != null && chartDialog.isShowing())
                    {
                        chartDialog.update(user);
                    }
                }
            }
        }
    }

    //command
    private void sendCommand() {
        String request = "AIDO,0"; //詢問溫度command/@3mins
        byte[] messageBytes = new byte[0];
        try {
            messageBytes = request.getBytes("UTF-8"); //Sting to byte
            mBluetoothLeService.sendData(messageBytes);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to convert message string to byte array");
        }
    }

    //每5分鐘執行一次
    private Runnable requestTemp = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "requestTemp start: @5mins");
            sendCommand();
            mHandler.postDelayed(this, 1000 * 60 * 3);    //5mins
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

        IntentFilter filter = new IntentFilter();
        filter.addAction(yhyBleService.ACTION_GATT_CONNECTED);
        filter.addAction(yhyBleService.ACTION_GATT_DISCONNECTED);
        filter.addAction(yhyBleService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(yhyBleService.ACTION_DATA_AVAILABLE);
        filter.addAction(yhyBleService.ACTION_NOTIFICATION_SUCCESS);
        filter.addAction(yhyBleService.ACTION_CONNECTING_FAIL);
        mBleReceiver = new BleReceiver();
        registerReceiver(mBleReceiver, filter);
    }

    /** ble背景服務 **/
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
            switch (action) {
                case yhyBleService.ACTION_GATT_CONNECTED:
                    Toasty.info(TemperatureActivity.this, "藍芽連接中...", Toast.LENGTH_SHORT, true).show();
                    break;

                case yhyBleService.ACTION_GATT_DISCONNECTED:
                    Toasty.info(TemperatureActivity.this, "藍芽已斷開並釋放資源", Toast.LENGTH_SHORT, true).show();
                    mBluetoothLeService.disconnect();
                    mBluetoothLeService.release();
                    updateStatus(name , deviceName , deviceAddress, getString(R.string.ble_unconnected));  //藍芽設備已斷開
                    break;

                case yhyBleService.ACTION_CONNECTING_FAIL:
                    Toasty.info(TemperatureActivity.this, "藍芽已斷開", Toast.LENGTH_SHORT, true).show();
                    mBluetoothLeService.disconnect();
                    updateStatus(name , deviceName , deviceAddress, getString(R.string.ble_unconnected));  //藍芽設備已斷開
                    break;

                case yhyBleService.ACTION_NOTIFICATION_SUCCESS:
                    Log.d(TAG, "onReceive: 收到BLE通知服務 啟動成功:");
                    updateStatus(name , deviceName , deviceAddress, getString(R.string.ble_connect_status)); //更新
                    break;
                    
                case yhyBleService.ACTION_DATA_AVAILABLE:
                    byte[] data = intent.getByteArrayExtra(yhyBleService.EXTRA_DATA);
                    Log.d(TAG, "onReceive: 體溫原始資料:" + ByteUtils.byteArrayToString(data));
                    String[] str = ByteUtils.byteArrayToString(data).split(","); //以,分割
                    String degreeStr = str[2];
                    String batteryStr = str[3];
                    double degree = Double.parseDouble(degreeStr)/100;
                    double battery = Double.parseDouble(batteryStr);
                    updateBleData(degree, battery); //更新體溫跟電量
                    break;

                default:
                    break;
            }
        }
    }

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
    }

    ////////////////////////////////////////////// Dialog fxn ////////////////////////////////////////////////////

    //新增觀測者Dialog
    private void dialogTemperature() {
        //自定義 一個TemperatureDialog繼承dialog
        AddTemperatureDialog addTemperatureDialog = new AddTemperatureDialog(this, R.style.Theme_AppCompat_Dialog, new AddTemperatureDialog.PriorityListener() {
            @Override
            public void setActivity(String username, String usergender, String userbirthday, String userweight, String userheight) {
                //data from dialog
                Log.d(TAG, "setActivity: " + username + "/" + usergender + "/" + userbirthday + "/" + userweight + "/" + userheight);

            }
        });
        addTemperatureDialog.setCancelable(false);
        addTemperatureDialog.show();
    }

    //遠端帳號新增彈跳視窗
    private void dialogRemote() {
        AlertDialog remoteDialog = new AlertDialog.Builder(this).create();
        LayoutInflater remotelayout = LayoutInflater.from(this);
        View remoteView = remotelayout.inflate(R.layout.dialog_remote_add, null);
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
                    boolean success = object.getBoolean("success");
                    if (success)
                        Toasty.success(TemperatureActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
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

    ///////////////////////////來自Adapter的callBack////////////////////////////////////

    //呼叫藍芽
    @Override
    public void onBleConnect(Member member) {
        name = member.getName();  //取得使用者名稱
        //初始化及相關搜尋
        initBle();
    }

    //啟動ble量測
    @Override
    public void onBleMeasuring(Member member) {
        requestTemp.run(); //5分鐘command一次
    }

    //刪除使用者
    @Override
    public void onDelUser(Member member) {

    }

    //呼叫圖表
    @Override
    public void onBleChart(Member member) {
        //客製Dialog圖表
        chartDialog = new ChartDialog(this, member);
        chartDialog.setCancelable(false); //點擊屏幕或物理返回鍵，dialog不消失
        chartDialog.show();
    }


}


