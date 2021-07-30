 package com.example.yhyhealthy;


import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.yhyhealthy.adapter.BluetoothLeAdapter;
import com.example.yhyhealthy.adapter.RemoteViewAdapter;
import com.example.yhyhealthy.adapter.TemperMainAdapter;
import com.example.yhyhealthy.data.ScannedData;
import com.example.yhyhealthy.datebase.RemoteAccountApi;
import com.example.yhyhealthy.datebase.TempDataApi;
import com.example.yhyhealthy.dialog.ChartDialog;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.module.yhyBleService;
import com.example.yhyhealthy.tools.ByteUtils;
import com.example.yhyhealthy.tools.SpacesItemDecoration;
import com.rahman.dialog.Activity.SmartDialog;
import com.rahman.dialog.ListenerCallBack.SmartDialogClickListener;
import com.rahman.dialog.Utilities.SmartDialogBuilder;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import es.dmoral.toasty.Toasty;
import pl.droidsonroids.gif.GifImageView;

import static com.example.yhyhealthy.module.ApiProxy.BLE_USER_ADD_VALUE;
import static com.example.yhyhealthy.module.ApiProxy.BLE_USER_LIST;
import static com.example.yhyhealthy.module.ApiProxy.FEVER_RECORD;
import static com.example.yhyhealthy.module.ApiProxy.REMOTE_USER_ADD;
import static com.example.yhyhealthy.module.ApiProxy.REMOTE_USER_LIST;
import static com.example.yhyhealthy.module.ApiProxy.REMOTE_USER_UNDER_LIST;

/**
 * 藍芽體溫量測首頁
 * 觀測者配適器 TemperMainAdapter
 * 遠端者配適器 RemoteViewAdapter
 * 藍芽配適器  BluetoothLeAdapter
 * */

 public class TemperatureActivity extends DeviceBaseActivity implements View.OnClickListener, TemperMainAdapter.TemperMainListener {

    private final static String TAG = "TemperatureActivity";

    private Button   supervise, remote;
    private Button   addTemperatureUser, addRemoteUser;
    private Button   selectedAccount;
    private TextView txtUserInfoEdit;

    //觀測者
    private RecyclerView recyclerView;
    private TemperMainAdapter tAdapter;

    //遠端
    private RecyclerView remoteRecycle;
    private ArrayAdapter<String> arrayAdapter;
    private String accountInfoClicked;
    private AlertDialog remoteDialog;

    //藍芽相關
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private yhyBleService mBluetoothLeService;
    private BroadcastReceiver mBleReceiver;
    private boolean isScanning = false;
    private final ArrayList<ScannedData> findDevice = new ArrayList<>();
    private BluetoothLeAdapter tempAdapter;
    private final Handler mHandler = new Handler();
    private AlertDialog alertDialog;
    //2021/07/06
    private Button searchPairBle;
    private Button btnSearch;

     //藍芽連線
     private TempDataApi.SuccessBean statusMemberBean = new TempDataApi.SuccessBean();   //for ble連線狀態用
     private int statusPosition;

     //藍芽定時相關宣告
     private final ArrayMap<String, Runnable> userMap = new ArrayMap<>();
     private final ArrayMap<String, Runnable> countDownTimerArrayMap = new ArrayMap<>();

    //圖表dialog
    private ChartDialog chartDialog;

    //api
    private ApiProxy proxy;

    //進度條
    private ProgressDialog progressDialog;

    //Other
    private boolean isBleList = true;
    private MediaPlayer mediaPlayer;

    //背景動畫
    private GifImageView gifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //禁止旋轉

        //休眠禁止
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        proxy = ApiProxy.getInstance();

        initView();
    }

    private void initView(){
        supervise = (Button) findViewById(R.id.bt_select_supervise);
        remote = (Button) findViewById(R.id.bt_select_remote);
        addTemperatureUser = (Button) findViewById(R.id.bt_add_temp);
        addRemoteUser = (Button) findViewById(R.id.bt_add_remote);
        txtUserInfoEdit = findViewById(R.id.tvEdit);
        selectedAccount = findViewById(R.id.btnChoseAccount);

        searchPairBle = findViewById(R.id.btn_pair_bluetooth);

        //動畫background
        gifImageView = findViewById(R.id.game_gif);
        gifImageView.setBackgroundResource(R.mipmap.yhy_new_background);

        //init RecyclerView's data
        recyclerView = findViewById(R.id.rvTempUser);
        remoteRecycle = findViewById(R.id.rvRemoteUser);  //遠端

        setInfo();        //觀測者初始化資訊

        supervise.setOnClickListener(this);
        remote.setOnClickListener(this);
        addTemperatureUser.setOnClickListener(this);
        addRemoteUser.setOnClickListener(this);
        txtUserInfoEdit.setOnClickListener(this);
        selectedAccount.setOnClickListener(this);
        searchPairBle.setOnClickListener(this);

        supervise.setBackgroundResource(R.drawable.rectangle_button); //先顯示觀測Button
        supervise.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
    }

    /**** 藍芽 2021/03/18 *****/
    @SuppressLint("NewApi")
    private void initBle() {
        /**啟用藍牙適配器*/
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) return;

        if (!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();   //自動啟動藍芽

        /** 開始掃描 ***/
//        dialogBleConnect();

        /** 開始掃描 新版 2021/07/30 ***/
        showConnectDialog();
    }

    //2021/07/30
    private void showConnectDialog() {
          AlertDialog.Builder builder = new AlertDialog.Builder(this);

          //標題背景
          TextView textView = new TextView(this);
          textView.setText(getString(R.string.please_chose_wifiless_type));
          textView.setPadding(20, 30, 20, 30);
          textView.setTextSize(20F);
          textView.setBackgroundResource(R.color.colorPrimaryDark);
          textView.setTextColor(Color.WHITE);
          builder.setCustomTitle(textView);

          //資料源
          String[] wirelessType = {getString(R.string.ble_devide), getString(R.string.wifi_device)};
          int itemChecked = -1;
          builder.setSingleChoiceItems(wirelessType, itemChecked, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case 0: /** 開始掃描BLE:本機 ***/
                            dialogBleConnect();
                            dialog.dismiss();
                            break;
                        case 1: /** 直接取得後台wifi貼片 **/
                            boolean isExists = getWifiSerialNumber();
                            if (isExists){
                                dialog.dismiss();
                            }else {
                                wifiDialog();
                                dialog.dismiss();
                            }
                            break;
                    }
              }
          });


          AlertDialog dialog = builder.create();
          dialog.show();

//        RecyclerView bleList = view.findViewById(R.id.rv_ble_scan_view);       //搜尋ble
//        RecyclerView pairedList = view.findViewById(R.id.rv_ble_paired_view);  //配對ble
    }


    /**BLE開始掃描*/
    @SuppressLint("NewApi")
    private void dialogBleConnect(){
        alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_bleconnect, null);
        RecyclerView bleRecyclerView = view.findViewById(R.id.rvBleScanView);

//        View view = inflater.inflate(R.layout.dialog_ble_list, null);
//        RecyclerView bleRecyclerView = view.findViewById(R.id.rv_ble_scan_view);

        /**設置Recyclerview列表*/
        tempAdapter = new BluetoothLeAdapter();
        bleRecyclerView.setAdapter(tempAdapter);
        bleRecyclerView.setHasFixedSize(true);
        bleRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        alertDialog.setView(view);
        //alertDialog.setCancelable(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        tempAdapter.OnItemClick(itemClick);

        searchBleDevices(); //搜尋藍芽

        //取消搜尋按鈕
        Button btnCancel = view.findViewById(R.id.btnBleCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                //Toasty.info(TemperatureActivity.this, getString(R.string.user_cancel_search), Toast.LENGTH_SHORT, true).show();
                alertDialog.dismiss(); //關閉視窗
            }
        });

        //重新搜尋
        btnSearch = view.findViewById(R.id.btnBleSubmit);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBleDevices();
            }
        });

        alertDialog.show();
    }

    //藍芽搜尋 2021/07/06
    private void searchBleDevices() {
        isScanning = true;
        if (isScanning){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    btnSearch.setVisibility(View.VISIBLE);
                    Toasty.info(TemperatureActivity.this, getString(R.string.search_in_5_min), Toast.LENGTH_SHORT, true).show();
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
        @SuppressLint("NewApi")
        @Override
        public void onItemClick(ScannedData selectedDevice) {

            mBluetoothAdapter.stopLeScan(mLeScanCallback); //停止搜尋
//            mBluetoothLeScanner.stopScan(mScanCallback);  //停止搜尋

            //啟動ble server連線
//            Log.d(TAG, "onItemClick: " + selectedDevice.getAddress());
            mBluetoothLeService.connect(selectedDevice.getAddress());  //2021/03/30

            //關閉視窗
            if (alertDialog.isShowing())
                alertDialog.dismiss();
        }
    };

    //檢查是否有綁定wifi貼片編號  2021/07/30
    private boolean getWifiSerialNumber(){

        return false;
    }

    //2021/07/30
    private void wifiDialog(){
        new SmartDialogBuilder(TemperatureActivity.this)
                .setTitle(getString(R.string.txt_alart_dialog))
                .setSubTitle(getString(R.string.txt_alert_messages))
                .setCancalable(false)
                .setNegativeButtonHide(false)
                .setPositiveButton(getString(R.string.dialog_ok), new SmartDialogClickListener() {
                    @Override
                    public void onClick(SmartDialog smartDialog) {
                        smartDialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_cancel), new SmartDialogClickListener() {
                    @Override
                    public void onClick(SmartDialog smartDialog) {
                        Toasty.info(TemperatureActivity.this, getString(R.string.you_are_do_nothing), Toast.LENGTH_SHORT,true).show();
                        smartDialog.dismiss();
                    }
                })
                .build().show();
    }


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
                        }else if (errorCode == 23) { //token失效
                            Toasty.error(TemperatureActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(TemperatureActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(TemperatureActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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
        List<TempDataApi.SuccessBean> dataList = tempDataApi.getSuccess();

        //將資料配置到Adapter並顯示出來
        tAdapter = new TemperMainAdapter(this, dataList, this);

        //設定item間距的距離
        int spacingInPixels = 20;
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
                supervise.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
                remote.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.color_font));
                addTemperatureUser.setVisibility(View.VISIBLE);
                addRemoteUser.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                remoteRecycle.setVisibility(View.GONE);
                selectedAccount.setVisibility(View.GONE);
                isBleList = true;
                setInfo();           //觀測者列表初始化
                break;
            case R.id.bt_select_remote:    //遠端Button
                remote.setBackgroundResource(R.drawable.rectangle_button);
                supervise.setBackgroundResource(R.drawable.relative_shape);
                remote.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
                supervise.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.color_font));
                addTemperatureUser.setVisibility(View.GONE);
                addRemoteUser.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                remoteRecycle.setVisibility(View.VISIBLE);
                selectedAccount.setVisibility(View.VISIBLE);
                isBleList = false;
                setAccountInfo();           //遠端監控帳號初始化  2021/03/25
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
                    Intent intent1 = new Intent(this,TemperEditListActivity.class);
                    startActivityForResult(intent1, 1);
                }else {
                    startActivity(new Intent(this, RemoteEditListActivity.class));
                }
                break;
            case R.id.btnChoseAccount:   //遠端監控者-選擇帳號
                setAccountInfo();        //先呼叫
                showAccountDialog();
                break;
            case R.id.btn_pair_bluetooth:
                initNewBle();
                break;
        }
    }

    //2021/07/26
    private void initNewBle() {
        AlertDialog mAlertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.dialog_ble_list, null);



    }

    //查詢遠端帳號-選擇帳號api
    private void setAccountInfo() {
        proxy.buildPOST(REMOTE_USER_LIST, "" , requestListener);
    }
    private ApiProxy.OnApiListener requestListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            arrayAdapter = new ArrayAdapter<String>(TemperatureActivity.this, android.R.layout.select_dialog_item);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0) { //帳號資料在success內
                            JSONArray array = object.getJSONArray("success");
                            for (int i = 0; i < array.length(); i++){
                                arrayAdapter.add(array.getString(i));
                            }
                        }else if (errorCode == 23) { //token失效
                            Toasty.error(TemperatureActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(TemperatureActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else if (errorCode == 6){ //查無資料
                            Toasty.info(TemperatureActivity.this, getString(R.string.account_is_no_data), Toast.LENGTH_SHORT, true).show();
                        }else {
                            Toasty.error(TemperatureActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //Dialog顯示遠端帳號
    private void showAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.please_select_one_account));

        if(arrayAdapter.isEmpty()) {
            Toasty.info(this, R.string.no_date, Toast.LENGTH_SHORT, true).show();
        }else {
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
    }

    //取得監控帳戶底下觀測者的資料列表　　2021/06/09 後端有問題...
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
                        Log.d(TAG, "run: " + result.toString());
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            parserRemoteData(result);
                        }else if (errorCode == 6){
                            Toasty.error(TemperatureActivity.this, getString(R.string.you_select_account_is_no_data), Toast.LENGTH_SHORT, true).show();
                        }else if (errorCode == 32) {
                            Toasty.error(TemperatureActivity.this, getString(R.string.remote_account_auth_code_error), Toast.LENGTH_SHORT, true).show();
                        }else if (errorCode == 23) { //token失效
                            Toasty.error(TemperatureActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(TemperatureActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(TemperatureActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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
        List<RemoteAccountApi.SuccessBean> remoteList = remoteData.getSuccess();

        //將資料配置到RecyclerView並顯示出來
        RemoteViewAdapter remoteAdapter = new RemoteViewAdapter(this, remoteList);
        remoteRecycle.setAdapter(remoteAdapter);
        remoteRecycle.setHasFixedSize(true);
        remoteRecycle.setLayoutManager(new LinearLayoutManager(this));

        //設定item間距的距離
        int spacingInPixels = 10;
        remoteRecycle.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //設定item間距

        //顯示選擇的帳號
        selectedAccount.setText(accountInfoClicked);
        selectedAccount.setTextColor(Color.RED);
    }

    //斷線狀態回傳給adapter (手動斷線或自動斷線都會跑這一段)
     private void updateDisconnectedStatus(String deviceName, String deviceAddress, String bleStatus){
        if(deviceAddress != null){
            if(tAdapter.findNameByMac(deviceAddress) != null){
                tAdapter.disconnectedDevice(deviceAddress, bleStatus, deviceName);
                //2021/07/23
                Log.d(TAG, "updateDisconnectedStatus: Name:" + tAdapter.findNameByMac(deviceAddress));

//                tAdapter.clear();

                //移除5秒鐘的佇列  2021/05/31
                countDownTimerArrayMap.remove(deviceAddress);
                //移除5秒鐘的定時
                mHandler.removeCallbacks(countDownTimerArrayMap.get(deviceAddress));

                //移除移除5分鐘的佇列
                userMap.remove(deviceAddress);
                //移除5分鐘的定時
                mHandler.removeCallbacks(userMap.get(deviceAddress));

            }else {
                Toasty.info(TemperatureActivity.this, getString(R.string.ble_connect_fail_and_try_again), Toast.LENGTH_SHORT, true).show();
            }
        }
     }

     //斷線音效  2021/07/09
    private void dialogDisconnected(String deviceName) {
        AlertDialog.Builder disconnectBuilder = new AlertDialog.Builder(this);
        disconnectBuilder.setTitle(getString(R.string.bluetooth_is_disconnected));
        disconnectBuilder.setMessage(deviceName +" " + getString(R.string.ble_is_disconnected));
        disconnectBuilder.setCancelable(false);
        disconnectBuilder.setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                stopAlarm();
                dialog.dismiss();
            }
        });

        AlertDialog dialog = disconnectBuilder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.color.orange);

        startAlarm(); //啟動循環撥放音效
    }

    //連線成功後回傳參數給adapter
    private void updateConnectedStatus(String deviceName, String deviceAddress, String bleStatus){
        if (deviceAddress != null){
            statusMemberBean.setMac(deviceAddress);
            statusMemberBean.setStatus(deviceName+" "+ bleStatus);
            statusMemberBean.setDeviceName(deviceName);
            tAdapter.updateItem(statusMemberBean, statusPosition);

            //secondTimerCreator(deviceAddress); //連線後-啟動5秒read 2021/07/01
        }
    }

    //更新收到體溫的訊息給RecyclerView的項目
    private void updateBleData(String receive, String macAddress) {
        DecimalFormat df = new DecimalFormat("#.##");

        String[] str = receive.split(","); //以,分割
        double degree = Double.parseDouble(str[2])/100;
        double battery = Double.parseDouble(str[3]);
        String batteryStr = df.format(battery);

        //溫度不為空
        if (degree != 0){
            //將溫度電量及mac傳到Adapter
            tAdapter.updateItemByMac(degree, batteryStr, macAddress);

            //如果chart視窗存在就將使用者的資訊傳遞到ChartDialog
            if (chartDialog != null && chartDialog.isShowing())
                //需用mac將藍芽回來的資料分開保存
                chartDialog.update(tAdapter.getDegreeByMac(macAddress));  //更新Dialog內的溫度圖表

            //電量低於40%則要通知 2021/05/12
            if (battery < 40) {
                String deviceName = tAdapter.findDeviceNameByMac(macAddress);
                Toasty.warning(TemperatureActivity.this,deviceName + getString(R.string.battery_is_low_40), Toast.LENGTH_SHORT, true).show();
            }

            //溫度低於25度要通知 2021/05/21
            if (degree <= 25){
                String userName = tAdapter.findNameByMac(macAddress);
                Toasty.warning(TemperatureActivity.this, userName + getString(R.string.under_25_degree),Toast.LENGTH_SHORT, true).show();
            }

            DateTime measureStartTime = tAdapter.findTimeByMac(macAddress);  //取得量測開始時的時間
            DateTime dateTime = new DateTime(new Date());                    //現在時間

            //高燒37.5以上+量測時間開始後超過5分鐘才顯示警告dialog 2021/06/25
            if ((dateTime.isAfter(measureStartTime.plusSeconds(301)) && degree > 37.5)){
                feverDialog(tAdapter.findNameByMac(macAddress), degree, tAdapter.findTargetIdByMac(macAddress), macAddress);
                startAlarm(); //啟動循環撥放音效
            }
        }
    }

    //發燒警告 2021/04/22
     private void feverDialog(String bleUserName, double degree, int targetId, String macAddress) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.fever_dialog, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        TextView feverName = view.findViewById(R.id.tvFeverName);
        feverName.setText(bleUserName);  //顯示發燒者

        TextView feverDegree = view.findViewById(R.id.tvFeverDegree);
        feverDegree.setText(getString(R.string.fever_degree_is) + String.valueOf(degree));      //顯示體溫

        EditText edtDrugName = view.findViewById(R.id.edt_drug_name);      //藥名
        EditText edtDrugDosage = view.findViewById(R.id.edt_drug_dosage);  //藥劑量

        EditText edtDrugTime = view.findViewById(R.id.edt_drug_pick_time); //服藥時間dialog
        edtDrugTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                new TimePickerDialog(TemperatureActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        //Time小於10則加0
                        edtDrugTime.setText(new StringBuilder().append(hourOfDay < 10 ? "0" + hourOfDay : hourOfDay).append(":")
                        .append(minute < 10 ? "0" + minute: minute));
                    }
                },hour,minute,false).show();
            }
        });

         TextView drugInfo = view.findViewById(R.id.txt_update_faver);
         drugInfo.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 //上傳到後端
                 if (TextUtils.isEmpty(edtDrugTime.getText().toString()))
                     Toasty.error(TemperatureActivity.this, getString(R.string.please_pick_time), Toast.LENGTH_SHORT, true).show();

                 if (TextUtils.isEmpty(edtDrugName.getText().toString()))
                     Toasty.error(TemperatureActivity.this, getString(R.string.please_input_drug), Toast.LENGTH_SHORT, true).show();

                 if (TextUtils.isEmpty(edtDrugDosage.getText().toString()))
                     Toasty.error(TemperatureActivity.this, getString(R.string.please_input_dosage), Toast.LENGTH_SHORT, true).show();

                 //資料齊全後上傳到後端去
                 updateMedicineRecordToApi(edtDrugName.getText().toString(), edtDrugDosage.getText().toString(), targetId);
             }
         });

         //現在時間
         DateTime dateTime = new DateTime(new Date());
         ImageView close = view.findViewById(R.id.ivClosefever);
         close.setOnClickListener(new View.OnClickListener() {  //關閉Dialog
             @Override
             public void onClick(View view) {
                 //記錄使用者關閉dialog時間
                 tAdapter.setFeverCloseTime(targetId, macAddress, dateTime.plusMinutes(25));
                 stopAlarm();  //停止播放循環音效
                 dialog.dismiss();
             }
        });

        dialog.show();
     }

    //command 0
    private void sendCommand(String deviceAddress) {
        String request = "AIDO,0"; //詢問溫度command
        byte[] messageBytes = new byte[0];
        try {
            messageBytes = request.getBytes("UTF-8"); //Sting to byte
            if(deviceAddress != null)
                mBluetoothLeService.writeDataToDevice(messageBytes, deviceAddress);  //2021/03/30
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to convert message string to byte array");
        }
    }

    //command 6
    private void sendCommand6(String deviceAddress) {
        String request = "AIDO,6"; //詢問溫度command
        byte[] messageBytes = new byte[0];
        try {
            messageBytes = request.getBytes("UTF-8"); //Sting to byte
            if(deviceAddress != null)
                mBluetoothLeService.writeDataToDevice(messageBytes, deviceAddress);  //2021/03/30
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to convert message string to byte array");
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //DateTime dateTime = new DateTime(new Date());
        Log.d(TAG, "onResume: 註冊藍芽接受器+綁定後台服務");
        //Log.d(TAG, "onResume: " + dateTime.toString("HH:mm:ss"));
        //註冊藍芽接受器
        registerBleReceiver();
    }

    //註冊藍芽接受器
    private void registerBleReceiver() {
//        /** 綁定後台服務 ***/
        Intent intent = new Intent(this, yhyBleService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        mBleReceiver = new BleReceiver();
        registerReceiver(mBleReceiver, yhyBleService.makeIntentFilter());
    }

    /** ble Service 背景服務 **/
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBluetoothLeService = ((yhyBleService.LocalBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
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
                    break;

                case yhyBleService.ACTION_GATT_DISCONNECTED: //自動斷開連結
                    Toasty.info(TemperatureActivity.this, getString(R.string.ble_is_disconnected_and_release), Toast.LENGTH_SHORT, true).show();
                    mBluetoothLeService.closeGatt(macAddress);
                    updateDisconnectedStatus(deviceName, macAddress, getString(R.string.ble_unconnected));
                    //2021/07/09 增加斷線通知
                    dialogDisconnected(deviceName);

                    //移除所有的Handler
                    if(mHandler != null)
                        mHandler.removeCallbacksAndMessages(null);

                    //移除所有ble設備佇列 2021/05/05
                    if (!userMap.isEmpty())
                        userMap.clear();

                    if (!countDownTimerArrayMap.isEmpty())
                        countDownTimerArrayMap.clear();
                    break;

                case yhyBleService.ACTION_CONNECTING_FAIL:
                    Toasty.info(TemperatureActivity.this, getString(R.string.ble_is_disconnected), Toast.LENGTH_SHORT, true).show();
                    mBluetoothLeService.disconnect();
                    break;
                case yhyBleService.ACTION_GATT_DISCONNECTED_SPECIFIC: //手動斷開連結
                    Toasty.info(TemperatureActivity.this, getString(R.string.ble_device_name) + ":" + deviceName + getString(R.string.ble_unconnected), Toast.LENGTH_SHORT, true).show();
                    updateDisconnectedStatus(deviceName, macAddress, getString(R.string.ble_unconnected));
                    break;
                case yhyBleService.ACTION_NOTIFY_ON:
                    Log.d(TAG, "onReceive: 收到BLE通知服務 啟動成功: " + macAddress + "裝置名稱:" + deviceName);
                    updateConnectedStatus(deviceName, macAddress, getString(R.string.ble_connect_status));
                    break;
                    
                case yhyBleService.ACTION_DATA_AVAILABLE: //收到Ble回復的資料
                    Log.d(TAG, "onReceive: ACTION_DATA_AVAILABLE" + ByteUtils.byteArrayToString(data));
                    String receiveInfo = ByteUtils.byteArrayToString(data);
                    //2021/07/01
//                    if (receiveInfo.contains("AT+DISCONNECT")){
//                        Log.d(TAG, "onReceive: ble is disconnect!"); //藍芽斷開
//                        updateConnectedStatus(deviceName, macAddress, getString(R.string.ble_is_sleep));
//                    }else {
//                        updateBleData(receiveInfo, macAddress);
//                    }
                    updateBleData(receiveInfo, macAddress); //更新體溫跟電量
                    //2021/07/23
                    Log.d(TAG, "onReceive: target:" + tAdapter.findTargetIdByMac(macAddress));
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
        //Log.d(TAG, "updateDegreeValueToApi: " + object.toString());
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
                        if (errorCode == 0) {
                            Toasty.success(TemperatureActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                        }else if (errorCode == 23) { //token失效
                            Toasty.error(TemperatureActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(TemperatureActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //重新登入
                            finish();
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

    //遠端帳號新增彈跳視窗
    private void dialogRemote() {
        remoteDialog = new AlertDialog.Builder(this).create();
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

               //新增的資料傳送到後台
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0) {
                            Toasty.success(TemperatureActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                            remoteDialog.dismiss(); //2021/07/08
                            setAccountInfo();       //刷新遠端帳號資料
                        }else if (errorCode == 23) { //token失效
                            Toasty.error(TemperatureActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else if (errorCode == 31){ //重複登入
                            Toasty.error(TemperatureActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(TemperatureActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    @Override   //藍芽連線interface (當使用者按下"+"這個icon)
    public void onBleConnect(TempDataApi.SuccessBean data, int position) {
        statusPosition = position;          //RecyclerView's position給予全域變數
        statusMemberBean = data;           //在把data內的資料丟給全域變數statusMemberBean;

        //初始化藍芽
        initBle();
    }

    @Override  //啟動量測 interface 2021/03/30
    public void onBleMeasuring(TempDataApi.SuccessBean data) {


        //5sec@5mins
        secondTimerCreator(data.getMac());

        //寫入啟動時間 2021/06/24增加
        data.setAlertDateTime(new DateTime(new Date()));
    }

    //5秒鐘跑一次command
    private void secondTimerCreator(String mac){
        SecondRun secondRun = new SecondRun(mac);
        Thread t = new Thread(secondRun);
        t.start();
        countDownTimerArrayMap.put(mac, secondRun);
    }

    //5分鐘跑一次Command
    private void timerCreator(String mac) {
        MyRun myRun = new MyRun(mac);
        Thread thread = new Thread(myRun);
        thread.start();
        userMap.put(mac, myRun);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override  //停止量測 interface 2021/04/22
     public void onBleDisConnected(TempDataApi.SuccessBean data) {
        mBluetoothLeService.closeGatt(data.getMac());   //藍芽斷開

        //移除5秒的定時
        mHandler.removeCallbacks(countDownTimerArrayMap.get(data.getMac()));
        countDownTimerArrayMap.remove(data.getMac());           //移除5秒的佇列

        mHandler.removeCallbacks(userMap.get(data.getMac()));   //移除5分鐘的定時
        userMap.remove(data.getMac());                          //移除5分鐘的佇列
     }

     @Override  //更新數據到後台
    public void passTarget(int targetId, double degree) {
        Log.d(TAG, "passTarget: " + targetId + ",degree:" + degree);
//        updateDegreeValueToApi(degree, targetId);
    }

    @Override  //服藥時間
    public void onPillRecord(TempDataApi.SuccessBean data, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.pill_doalog, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); //disable touch other screen

        EditText pillTime = view.findViewById(R.id.edt_pill_time);
        EditText pillName = view.findViewById(R.id.edt_pill_name);
        EditText pillDoes = view.findViewById(R.id.edt_pill_does);
        TextView pillUpdate = view.findViewById(R.id.txt_pill_update);
        ImageView closePill = view.findViewById(R.id.img_pill_close);

        pillTime.setOnClickListener(new View.OnClickListener() {  //時間選擇
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                new TimePickerDialog(TemperatureActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        //Time小於10則加0
                        pillTime.setText(new StringBuilder().append(hourOfDay < 10 ? "0" + hourOfDay : hourOfDay).append(":")
                                .append(minute < 10 ? "0" + minute: minute));
                    }
                },hour,minute,false).show();
            }
        });

        pillUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //上傳到後端前檢查資料是否齊全:時間,藥名,劑量
                if (TextUtils.isEmpty(pillTime.getText().toString()))
                    Toasty.error(TemperatureActivity.this, getString(R.string.please_pick_time), Toast.LENGTH_SHORT, true).show();


                if (TextUtils.isEmpty(pillName.getText().toString()))
                    Toasty.error(TemperatureActivity.this, getString(R.string.please_input_drug), Toast.LENGTH_SHORT, true).show();


                if (TextUtils.isEmpty(pillDoes.getText().toString()))
                    Toasty.error(TemperatureActivity.this, getString(R.string.please_input_dosage), Toast.LENGTH_SHORT, true).show();

                //上傳到後端
                updateMedicineRecordToApi(pillName.getText().toString(), pillDoes.getText().toString(), data.getTargetId());
            }
        });

        closePill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();

        //將對話視窗大小按照螢幕比例設定
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        int dialogWidth = (int) (displayWidth * 0.8f);
        int dialogHeight = (int) (displayHeight * 0.8f);
        layoutParams.width = dialogWidth;
        layoutParams.height = dialogHeight;
        dialog.getWindow().setAttributes(layoutParams);
    }

    //將使用者服藥的資訊上傳到後端 2021/07/02
    private void updateMedicineRecordToApi(String drugName, String drugDoes, int targetId) {
        JSONObject json = new JSONObject();
        try {
            json.put("targetId", targetId);
            json.put("medicine", drugName);
            json.put("drugDose", drugDoes);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(FEVER_RECORD, json.toString(), drugListener);
    }

    private ApiProxy.OnApiListener drugListener = new ApiProxy.OnApiListener() {
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
                        }else if (errorCode == 23) { //token失效
                            Toasty.error(TemperatureActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(TemperatureActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(TemperatureActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    @Override   //呼叫圖表interface
    public void onBleChart(TempDataApi.SuccessBean data) {
        //客製Dialog圖表
        if(data.getMac() != null) {
            chartDialog = new ChartDialog(this, data);
            chartDialog.setCancelable(false); //點擊屏幕或物理返回鍵，dialog不消失
            chartDialog.show();
        }
    }

     @Override  //症狀 2021/04/08
     public void onSymptomRecord(TempDataApi.SuccessBean data, int position) {
        int targetId = data.getTargetId();

        Intent intent = new Intent();
        intent.setClass(this, SymptomActivity.class);

        Bundle bundle  = new Bundle();
        bundle.putInt("targetId", targetId);
        bundle.putInt("position", position);
        intent.putExtras(bundle);
        startActivity(intent);
     }

     //5分鐘定時fxn 2021/05/05
     public class MyRun implements Runnable {

         private String mac;

         public MyRun(String mac){
             this.mac = mac;
         }

         @Override
         public void run() {
             Log.d(TAG, "每5分鐘command: " + mac);
             sendCommand(mac);
             mHandler.postDelayed(this, 1000 * 60 *5);  //5分鐘
         }
     }

    //5秒鐘定時fxn 2021/05/31
     public class SecondRun implements Runnable{

         private String mac;
         private int countTime = 61;

         public SecondRun(String mac){
             this.mac = mac;
         }

         @Override
         public void run() {
             if (countTime > 0) {
                 Log.d(TAG, "執行每5秒鐘command: " + mac + ",times:" + countTime);
                 sendCommand(mac);
                 countTime--;
                 mHandler.postDelayed(this, 1000 * 5); //5秒
             }else {
                 Log.d(TAG, "啟動5分鐘command: ");
                 timerCreator(mac);
             }
         }
     }

     //播放音效
    private void startAlarm(){
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mediaPlayer = MediaPlayer.create(this, notification);
        mediaPlayer.setLooping(true);  //是否循環撥放
        mediaPlayer.start();
    }

    //停止音效
    private void stopAlarm(){
        if (mediaPlayer != null){
            if (mediaPlayer.isPlaying()){
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause:");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
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

         //移除所有的Handler
         if(mHandler != null)
             mHandler.removeCallbacksAndMessages(null);

         //移除所有ble設備佇列 2021/05/05
         if (!userMap.isEmpty())
            userMap.clear();

         if (!countDownTimerArrayMap.isEmpty())
             countDownTimerArrayMap.clear();

         //視窗如果有顯示的話...
         if (chartDialog != null && chartDialog.isShowing())
             chartDialog.dismiss();
     }

     @Override //新增觀測者資料返回 2021/03/24
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK){
            setInfo(); //跟後台要資料並刷新RecyclerView
        }else {
            Toasty.info(this, getString(R.string.nothing), Toast.LENGTH_SHORT, true).show();
        }
    }
}


