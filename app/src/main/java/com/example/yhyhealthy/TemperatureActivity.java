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
import com.thanosfisherman.wifiutils.WifiUtils;

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
 * ????????????????????????
 * ?????????????????? TemperMainAdapter
 * ?????????????????? RemoteViewAdapter
 * ???????????????  BluetoothLeAdapter
 * */

 public class TemperatureActivity extends DeviceBaseActivity implements View.OnClickListener, TemperMainAdapter.TemperMainListener {

    private final static String TAG = "TemperatureActivity";

    private Button   supervise, remote;
    private Button   addTemperatureUser, addRemoteUser;
    private Button   selectedAccount;
    private TextView txtUserInfoEdit;

    //?????????
    private RecyclerView recyclerView;
    private TemperMainAdapter tAdapter;

    //??????
    private RecyclerView remoteRecycle;
    private ArrayAdapter<String> arrayAdapter;
    private String accountInfoClicked;
    private AlertDialog remoteDialog;

    //????????????
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

     //????????????
     private TempDataApi.SuccessBean statusMemberBean = new TempDataApi.SuccessBean();   //for ble???????????????
     private int statusPosition;

     //????????????????????????
     private final ArrayMap<String, Runnable> userMap = new ArrayMap<>();
     private final ArrayMap<String, Runnable> countDownTimerArrayMap = new ArrayMap<>();

    //??????dialog
    private ChartDialog chartDialog;

    //api
    private ApiProxy proxy;

    //?????????
    private ProgressDialog progressDialog;

    //Other
    private boolean isBleList = true;
    private MediaPlayer mediaPlayer;

    //????????????
    private GifImageView gifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //????????????

        //????????????
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

        //??????background
        gifImageView = findViewById(R.id.game_gif);
        gifImageView.setBackgroundResource(R.mipmap.yhy_new_background);

        //init RecyclerView's data
        recyclerView = findViewById(R.id.rvTempUser);
        remoteRecycle = findViewById(R.id.rvRemoteUser);  //??????

        setInfo();        //????????????????????????

        supervise.setOnClickListener(this);
        remote.setOnClickListener(this);
        addTemperatureUser.setOnClickListener(this);
        addRemoteUser.setOnClickListener(this);
        txtUserInfoEdit.setOnClickListener(this);
        selectedAccount.setOnClickListener(this);
        searchPairBle.setOnClickListener(this);

        supervise.setBackgroundResource(R.drawable.rectangle_button); //???????????????Button
        supervise.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
    }

    /**** ?????? or wifi 2021/08/02 *****/
    @SuppressLint("NewApi")
    private void initWireless() {

        /**?????????????????????*/
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) return;

        if (!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();   //??????????????????

        /** ???????????? ***/
//        dialogBleConnect();

        /** ???????????? ?????? 2021/07/30 ***/
        showConnectDialog();
    }

    //2021/07/30
    private void showConnectDialog() {
          AlertDialog.Builder builder = new AlertDialog.Builder(this);

          //????????????
          TextView textView = new TextView(this);
          textView.setText(getString(R.string.please_chose_wifiless_type));
          textView.setPadding(20, 30, 20, 30);
          textView.setTextSize(20F);
          textView.setBackgroundResource(R.color.colorPrimaryDark);
          textView.setTextColor(Color.WHITE);
          builder.setCustomTitle(textView);

          //?????????
          String[] wirelessType = {getString(R.string.ble_devide), getString(R.string.wifi_device)};
          int itemChecked = -1;
          builder.setSingleChoiceItems(wirelessType, itemChecked, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case 0: /** ????????????BLE:?????? ***/
                            dialogBleConnect();
                            //??????????????????
                            dialog.dismiss();
                            break;
                        case 1: /** ??????????????????wifi?????? **/
                            boolean isExists = getWifiSerialNumber();
                            if (isExists){
                                dialog.dismiss();
                            }else {
                                wifiDialog();  //?????????????????????wifi????????????
                                dialog.dismiss();
                            }
                            break;
                    }
              }
          });


          AlertDialog dialog = builder.create();
          dialog.show();

//        RecyclerView bleList = view.findViewById(R.id.rv_ble_scan_view);       //??????ble
//        RecyclerView pairedList = view.findViewById(R.id.rv_ble_paired_view);  //??????ble
    }

    //?????????????????????wifi????????????  2021/07/30
    private boolean getWifiSerialNumber(){

        return false;
    }

    /**BLE????????????*/
    @SuppressLint("NewApi")
    private void dialogBleConnect(){
        alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_bleconnect, null);
        RecyclerView bleRecyclerView = view.findViewById(R.id.rvBleScanView);

//        View view = inflater.inflate(R.layout.dialog_ble_list, null);
//        RecyclerView bleRecyclerView = view.findViewById(R.id.rv_ble_scan_view);

        /**??????Recyclerview??????*/
        tempAdapter = new BluetoothLeAdapter();
        bleRecyclerView.setAdapter(tempAdapter);
        bleRecyclerView.setHasFixedSize(true);
        bleRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        alertDialog.setView(view);
        alertDialog.setCancelable(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        tempAdapter.OnItemClick(itemClick);

        searchBleDevices(); //????????????

        //??????????????????
        Button btnCancel = view.findViewById(R.id.btnBleCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                alertDialog.dismiss(); //????????????
            }
        });

        //????????????
        btnSearch = view.findViewById(R.id.btnBleSubmit);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBleDevices();
            }
        });

        alertDialog.show();
    }

    //???????????? 2021/07/06
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

    /**?????????????????????*/
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            new Thread(()->{
                /**???????????????????????????????????????*/
                if (device.getName()!= null){
                    /**?????????????????????????????????*/
                    findDevice.add(new ScannedData(device.getName()
                            , String.valueOf(rssi)
                            , device.getAddress()));
                    /**??????????????????Address?????????????????????????????????????????????*/
                    ArrayList newList = getSingle(findDevice);
                    runOnUiThread(()->{
                        /**???????????????RecyclerView?????????*/
                        tempAdapter.addDevice(newList);
                    });
                }
            }).start();
        }
    };

    /**???????????????????????????(???Address??????)*/
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
     * ???Address????????????->??????????????????????????????
     */
    private int getIndex(ArrayList temp, Object obj) {
        for (int i = 0; i < temp.size(); i++) {
            if (temp.get(i).toString().contains(obj.toString())) {
                return i;
            }
        }
        return -1;
    }

    /**???????????????????????????????????????*/
    private BluetoothLeAdapter.OnItemClick itemClick = new BluetoothLeAdapter.OnItemClick() {
        @SuppressLint("NewApi")
        @Override
        public void onItemClick(ScannedData selectedDevice) {

            mBluetoothAdapter.stopLeScan(mLeScanCallback); //????????????

            //??????ble server??????
            mBluetoothLeService.connect(selectedDevice.getAddress());  //2021/03/30

            //????????????
            if (alertDialog.isShowing())
                alertDialog.dismiss();
        }
    };

    //2021/07/30  ?????????????????????wifi?????????????????????
    private void wifiDialog(){
        new SmartDialogBuilder(TemperatureActivity.this)
                .setTitle(getString(R.string.txt_alart_dialog))
                .setSubTitle(getString(R.string.txt_alert_messages))
                .setCancalable(false)
                .setNegativeButtonHide(false)
                .setPositiveButton(getString(R.string.dialog_ok), new SmartDialogClickListener() {
                    @Override
                    public void onClick(SmartDialog smartDialog) {
                        //??????????????????????????????????????????????????? 2021/08/02
                        bindWifiDevice();
                        //??????????????????
                        smartDialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_cancel), new SmartDialogClickListener() {
                    @Override
                    public void onClick(SmartDialog smartDialog) {
                        Toasty.info(TemperatureActivity.this, getString(R.string.you_are_do_nothing), Toast.LENGTH_SHORT,true).show();
                        //??????????????????
                        smartDialog.dismiss();
                    }
                })
                .build().show();
    }

    //??????????????????????????????????????????????????? 2021/08/02
    private void bindWifiDevice() {
        Intent intent = new Intent();
        intent.setClass(TemperatureActivity.this, TemperEditActivity.class);
        Bundle bundle = new Bundle();

        bundle.putInt("targetId", statusMemberBean.getTargetId());
        bundle.putString("name", statusMemberBean.getUserName());
        bundle.putString("gender", statusMemberBean.getGender());
        bundle.putString("birthday", statusMemberBean.getTempBirthday());
        bundle.putString("height", String.valueOf(statusMemberBean.getTempHeight()));
        bundle.putString("weight", String.valueOf(statusMemberBean.getTempWeight()));
        bundle.putString("imgId", statusMemberBean.getImgUrl());  //?????????
//        bundle.putString("HeadShot", statusMemberBean.getHeadShot()); //?????????
        bundle.putBoolean("wifi", true);  //????????????wifi????????????

        intent.putExtras(bundle);
        startActivityForResult(intent, 1);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //????????? Item ??????Data
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
                        }else if (errorCode == 23) { //token??????
                            Toasty.error(TemperatureActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //????????????
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(TemperatureActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //????????????
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

    //???????????????????????????
    private void parserJson(JSONObject result) {
        TempDataApi tempDataApi = TempDataApi.newInstance(result.toString());
        List<TempDataApi.SuccessBean> dataList = tempDataApi.getSuccess();

        //??????????????????Adapter???????????????
        tAdapter = new TemperMainAdapter(this, dataList, this);

        //??????item???????????????
        int spacingInPixels = 20;
        recyclerView.setAdapter(tAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //??????item??????
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.bt_select_supervise:  //??????Button
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
                setInfo();           //????????????????????????
                break;
            case R.id.bt_select_remote:    //??????Button
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
                setAccountInfo();           //???????????????????????????  2021/03/25
                break;
            case R.id.bt_add_temp:       //???????????????onClick
                Intent intent = new Intent(this, TemperatureAddActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.bt_add_remote:     //???????????????onClick
                dialogRemote();
                break;
            case R.id.tvEdit:           //?????????????????????
                if(isBleList) {
                    Intent intent1 = new Intent(this,TemperEditListActivity.class);
                    startActivityForResult(intent1, 1);
                }else {
                    startActivity(new Intent(this, RemoteEditListActivity.class));
                }
                break;
            case R.id.btnChoseAccount:   //???????????????-????????????
                setAccountInfo();        //?????????
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

    //??????????????????-????????????api
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
                        if (errorCode == 0) { //???????????????success???
                            JSONArray array = object.getJSONArray("success");
                            for (int i = 0; i < array.length(); i++){
                                arrayAdapter.add(array.getString(i));
                            }
                        }else if (errorCode == 23) { //token??????
                            Toasty.error(TemperatureActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //????????????
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(TemperatureActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //????????????
                            finish();
                        }else if (errorCode == 6){ //????????????
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

    //Dialog??????????????????
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

    //??????????????????????????????????????????????????????2021/06/09 ???????????????...
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
                        }else if (errorCode == 23) { //token??????
                            Toasty.error(TemperatureActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //????????????
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(TemperatureActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //????????????
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

    //?????????????????????????????????????????????  2021/03/26
    private void parserRemoteData(JSONObject result) {
        RemoteAccountApi remoteData = RemoteAccountApi.newInstance(result.toString());
        List<RemoteAccountApi.SuccessBean> remoteList = remoteData.getSuccess();

        //??????????????????RecyclerView???????????????
        RemoteViewAdapter remoteAdapter = new RemoteViewAdapter(this, remoteList);
        remoteRecycle.setAdapter(remoteAdapter);
        remoteRecycle.setHasFixedSize(true);
        remoteRecycle.setLayoutManager(new LinearLayoutManager(this));

        //??????item???????????????
        int spacingInPixels = 10;
        remoteRecycle.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //??????item??????

        //?????????????????????
        selectedAccount.setText(accountInfoClicked);
        selectedAccount.setTextColor(Color.RED);
    }

    //?????????????????????adapter (?????????????????????????????????????????????)
     private void updateDisconnectedStatus(String deviceName, String deviceAddress, String bleStatus){
        if(deviceAddress != null){
            if(tAdapter.findNameByMac(deviceAddress) != null){
                tAdapter.disconnectedDevice(deviceAddress, bleStatus, deviceName);
                //2021/07/23
                //Log.d(TAG, "updateDisconnectedStatus: Name:" + tAdapter.findNameByMac(deviceAddress));

                //??????5???????????????  2021/05/31
                countDownTimerArrayMap.remove(deviceAddress);
                //??????5???????????????
                mHandler.removeCallbacks(countDownTimerArrayMap.get(deviceAddress));

                //????????????5???????????????
                userMap.remove(deviceAddress);
                //??????5???????????????
                mHandler.removeCallbacks(userMap.get(deviceAddress));

            }else {
                Toasty.info(TemperatureActivity.this, getString(R.string.ble_connect_fail_and_try_again), Toast.LENGTH_SHORT, true).show();
            }
        }
     }

     //????????????  2021/07/09
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

        startAlarm(); //????????????????????????
    }

    //??????????????????????????????adapter
    private void updateConnectedStatus(String deviceName, String deviceAddress, String bleStatus){
        if (deviceAddress != null){
            statusMemberBean.setMac(deviceAddress);
            statusMemberBean.setStatus(deviceName+" "+ bleStatus);
            statusMemberBean.setDeviceName(deviceName);
            tAdapter.updateItem(statusMemberBean, statusPosition);

            //secondTimerCreator(deviceAddress); //?????????-??????5???read 2021/07/01
        }
    }

    //??????????????????????????????RecyclerView?????????
    private void updateBleData(String receive, String macAddress) {
        DecimalFormat df = new DecimalFormat("#.##");

        String[] str = receive.split(","); //???,??????
        double degree = Double.parseDouble(str[2])/100;
        double battery = Double.parseDouble(str[3]);
        String batteryStr = df.format(battery);

        //???????????????
        if (degree != 0){
            //??????????????????mac??????Adapter
            tAdapter.updateItemByMac(degree, batteryStr, macAddress);

            //??????chart?????????????????????????????????????????????ChartDialog
            if (chartDialog != null && chartDialog.isShowing())
                //??????mac????????????????????????????????????
                chartDialog.update(tAdapter.getDegreeByMac(macAddress));  //??????Dialog??????????????????

            //????????????40%???????????? 2021/05/12
            if (battery < 40) {
                String deviceName = tAdapter.findDeviceNameByMac(macAddress);
                Toasty.warning(TemperatureActivity.this,deviceName + getString(R.string.battery_is_low_40), Toast.LENGTH_SHORT, true).show();
            }

            //????????????25???????????? 2021/05/21
            if (degree <= 25){
                String userName = tAdapter.findNameByMac(macAddress);
                Toasty.warning(TemperatureActivity.this, userName + getString(R.string.under_25_degree),Toast.LENGTH_SHORT, true).show();
            }

            DateTime measureStartTime = tAdapter.findTimeByMac(macAddress);  //??????????????????????????????
            DateTime dateTime = new DateTime(new Date());                    //????????????

            //??????37.5??????+???????????????????????????5?????????????????????dialog 2021/06/25
            if ((dateTime.isAfter(measureStartTime.plusSeconds(301)) && degree > 37.5)){
                feverDialog(tAdapter.findNameByMac(macAddress), degree, tAdapter.findTargetIdByMac(macAddress), macAddress);
                startAlarm(); //????????????????????????
            }
        }
    }

    //???????????? 2021/04/22
     private void feverDialog(String bleUserName, double degree, int targetId, String macAddress) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.fever_dialog, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        TextView feverName = view.findViewById(R.id.tvFeverName);
        feverName.setText(bleUserName);  //???????????????

        TextView feverDegree = view.findViewById(R.id.tvFeverDegree);
        feverDegree.setText(getString(R.string.fever_degree_is) + String.valueOf(degree));      //????????????

        EditText edtDrugName = view.findViewById(R.id.edt_drug_name);      //??????
        EditText edtDrugDosage = view.findViewById(R.id.edt_drug_dosage);  //?????????

        EditText edtDrugTime = view.findViewById(R.id.edt_drug_pick_time); //????????????dialog
        edtDrugTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                new TimePickerDialog(TemperatureActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        //Time??????10??????0
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
                 //???????????????
                 if (TextUtils.isEmpty(edtDrugTime.getText().toString()))
                     Toasty.error(TemperatureActivity.this, getString(R.string.please_pick_time), Toast.LENGTH_SHORT, true).show();

                 if (TextUtils.isEmpty(edtDrugName.getText().toString()))
                     Toasty.error(TemperatureActivity.this, getString(R.string.please_input_drug), Toast.LENGTH_SHORT, true).show();

                 if (TextUtils.isEmpty(edtDrugDosage.getText().toString()))
                     Toasty.error(TemperatureActivity.this, getString(R.string.please_input_dosage), Toast.LENGTH_SHORT, true).show();

                 //?????????????????????????????????
                 updateMedicineRecordToApi(edtDrugName.getText().toString(), edtDrugDosage.getText().toString(), targetId);
             }
         });

         //????????????
         DateTime dateTime = new DateTime(new Date());
         ImageView close = view.findViewById(R.id.ivClosefever);
         close.setOnClickListener(new View.OnClickListener() {  //??????Dialog
             @Override
             public void onClick(View view) {
                 //?????????????????????dialog??????
                 tAdapter.setFeverCloseTime(targetId, macAddress, dateTime.plusMinutes(25));
                 stopAlarm();  //????????????????????????
                 dialog.dismiss();
             }
        });

        dialog.show();
     }

    //command 0
    private void sendCommand(String deviceAddress) {
        String request = "AIDO,0"; //????????????command
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
        String request = "AIDO,6"; //????????????command
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
        Log.d(TAG, "onResume: ?????????????????????+??????????????????");
        //Log.d(TAG, "onResume: " + dateTime.toString("HH:mm:ss"));
        //?????????????????????
        registerBleReceiver();
    }

    //?????????????????????
    private void registerBleReceiver() {
//        /** ?????????????????? ***/
        Intent intent = new Intent(this, yhyBleService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        mBleReceiver = new BleReceiver();
        registerReceiver(mBleReceiver, yhyBleService.makeIntentFilter());
    }

    /** ble Service ???????????? **/
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
     * ?????????????????????
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

                case yhyBleService.ACTION_GATT_DISCONNECTED: //??????????????????
                    Toasty.info(TemperatureActivity.this, getString(R.string.ble_is_disconnected_and_release), Toast.LENGTH_SHORT, true).show();
                    mBluetoothLeService.closeGatt(macAddress);
                    updateDisconnectedStatus(deviceName, macAddress, getString(R.string.ble_unconnected));
                    //2021/07/09 ??????????????????
                    dialogDisconnected(deviceName);

                    //???????????????Handler
                    if(mHandler != null)
                        mHandler.removeCallbacksAndMessages(null);

                    //????????????ble???????????? 2021/05/05
                    if (!userMap.isEmpty())
                        userMap.clear();

                    if (!countDownTimerArrayMap.isEmpty())
                        countDownTimerArrayMap.clear();
                    break;

                case yhyBleService.ACTION_CONNECTING_FAIL:
                    Toasty.info(TemperatureActivity.this, getString(R.string.ble_is_disconnected), Toast.LENGTH_SHORT, true).show();
                    mBluetoothLeService.disconnect();
                    break;
                case yhyBleService.ACTION_GATT_DISCONNECTED_SPECIFIC: //??????????????????
                    Toasty.info(TemperatureActivity.this, getString(R.string.ble_device_name) + ":" + deviceName + getString(R.string.ble_unconnected), Toast.LENGTH_SHORT, true).show();
                    updateDisconnectedStatus(deviceName, macAddress, getString(R.string.ble_unconnected));
                    break;
                case yhyBleService.ACTION_NOTIFY_ON:
                    Log.d(TAG, "onReceive: ??????BLE???????????? ????????????: " + macAddress + "????????????:" + deviceName);
                    updateConnectedStatus(deviceName, macAddress, getString(R.string.ble_connect_status));
                    break;
                    
                case yhyBleService.ACTION_DATA_AVAILABLE: //??????Ble???????????????
                    Log.d(TAG, "onReceive: ACTION_DATA_AVAILABLE" + ByteUtils.byteArrayToString(data));
                    String receiveInfo = ByteUtils.byteArrayToString(data);
                    //2021/07/01
//                    if (receiveInfo.contains("AT+DISCONNECT")){
//                        Log.d(TAG, "onReceive: ble is disconnect!"); //????????????
//                        updateConnectedStatus(deviceName, macAddress, getString(R.string.ble_is_sleep));
//                    }else {
//                        updateBleData(receiveInfo, macAddress);
//                    }
                    updateBleData(receiveInfo, macAddress); //?????????????????????
                    //2021/07/23
                    Log.d(TAG, "onReceive: target:" + tAdapter.findTargetIdByMac(macAddress));
                    break;

                default:
                    break;
            }
        }
    }

    //2021/03/25 update???????????????????????????????????????
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
                        }else if (errorCode == 23) { //token??????
                            Toasty.error(TemperatureActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //????????????
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(TemperatureActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //????????????
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

    //??????????????????????????????
    private void dialogRemote() {
        remoteDialog = new AlertDialog.Builder(this).create();
        LayoutInflater remoteLayout = LayoutInflater.from(this);
        View remoteView = remoteLayout.inflate(R.layout.dialog_remote_add, null);
        remoteDialog.setView(remoteView);
        remoteDialog.setCancelable(false); //??????????????????

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
                //????????????????????????
               if (TextUtils.isEmpty(account.getText().toString()))
                   return;
               if (TextUtils.isEmpty(authCode.getText().toString()))
                   return;

               //??????????????????????????????
                updateRemoteToApi(account, authCode);
            }
        });

        remoteDialog.show();
    }

    //????????????????????????????????? 2021/03/19
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
                            setAccountInfo();       //????????????????????????
                        }else if (errorCode == 23) { //token??????
                            Toasty.error(TemperatureActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //????????????
                            finish();
                        }else if (errorCode == 31){ //????????????
                            Toasty.error(TemperatureActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //????????????
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

    @Override   //????????????interface (??????????????????"+"??????icon)
    public void onBleConnect(TempDataApi.SuccessBean data, int position) {
        statusPosition = position;          //RecyclerView's position??????????????????
        statusMemberBean = data;           //??????data??????????????????????????????statusMemberBean;

        //?????????????????????wifi??????
        initWireless();
    }

    @Override  //???????????? interface 2021/03/30
    public void onBleMeasuring(TempDataApi.SuccessBean data) {


        //5sec@5mins
        secondTimerCreator(data.getMac());

        //?????????????????? 2021/06/24??????
        data.setAlertDateTime(new DateTime(new Date()));
    }

    //5???????????????command
    private void secondTimerCreator(String mac){
        SecondRun secondRun = new SecondRun(mac);
        Thread t = new Thread(secondRun);
        t.start();
        countDownTimerArrayMap.put(mac, secondRun);
    }

    //5???????????????Command
    private void timerCreator(String mac) {
        MyRun myRun = new MyRun(mac);
        Thread thread = new Thread(myRun);
        thread.start();
        userMap.put(mac, myRun);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override  //???????????? interface 2021/04/22
     public void onBleDisConnected(TempDataApi.SuccessBean data) {
        mBluetoothLeService.closeGatt(data.getMac());   //????????????

        //??????5????????????
        mHandler.removeCallbacks(countDownTimerArrayMap.get(data.getMac()));
        countDownTimerArrayMap.remove(data.getMac());           //??????5????????????

        mHandler.removeCallbacks(userMap.get(data.getMac()));   //??????5???????????????
        userMap.remove(data.getMac());                          //??????5???????????????
     }

     @Override  //?????????????????????
    public void passTarget(int targetId, double degree) {
        Log.d(TAG, "passTarget: " + targetId + ",degree:" + degree);
//        updateDegreeValueToApi(degree, targetId);
    }

    @Override  //????????????
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

        pillTime.setOnClickListener(new View.OnClickListener() {  //????????????
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                new TimePickerDialog(TemperatureActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        //Time??????10??????0
                        pillTime.setText(new StringBuilder().append(hourOfDay < 10 ? "0" + hourOfDay : hourOfDay).append(":")
                                .append(minute < 10 ? "0" + minute: minute));
                    }
                },hour,minute,false).show();
            }
        });

        pillUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //??????????????????????????????????????????:??????,??????,??????
                if (TextUtils.isEmpty(pillTime.getText().toString()))
                    Toasty.error(TemperatureActivity.this, getString(R.string.please_pick_time), Toast.LENGTH_SHORT, true).show();


                if (TextUtils.isEmpty(pillName.getText().toString()))
                    Toasty.error(TemperatureActivity.this, getString(R.string.please_input_drug), Toast.LENGTH_SHORT, true).show();


                if (TextUtils.isEmpty(pillDoes.getText().toString()))
                    Toasty.error(TemperatureActivity.this, getString(R.string.please_input_dosage), Toast.LENGTH_SHORT, true).show();

                //???????????????
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

        //?????????????????????????????????????????????
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

    //?????????????????????????????????????????? 2021/07/02
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
                        }else if (errorCode == 23) { //token??????
                            Toasty.error(TemperatureActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //????????????
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(TemperatureActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureActivity.this, LoginActivity.class)); //????????????
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

    @Override   //????????????interface
    public void onBleChart(TempDataApi.SuccessBean data) {
        //??????Dialog??????
        if(data.getMac() != null) {
            chartDialog = new ChartDialog(this, data);
            chartDialog.setCancelable(false); //?????????????????????????????????dialog?????????
            chartDialog.show();
        }
    }

     @Override  //?????? 2021/04/08
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

     //5????????????fxn 2021/05/05
     public class MyRun implements Runnable {

         private String mac;

         public MyRun(String mac){
             this.mac = mac;
         }

         @Override
         public void run() {
             Log.d(TAG, "???5??????command: " + mac);
             sendCommand(mac);
             mHandler.postDelayed(this, 1000 * 60 *5);  //5??????
         }
     }

    //5????????????fxn 2021/05/31
     public class SecondRun implements Runnable{

         private String mac;
         private int countTime = 61;

         public SecondRun(String mac){
             this.mac = mac;
         }

         @Override
         public void run() {
             if (countTime > 0) {
                 Log.d(TAG, "?????????5??????command: " + mac + ",times:" + countTime);
                 sendCommand(mac);
                 countTime--;
                 mHandler.postDelayed(this, 1000 * 5); //5???
             }else {
                 Log.d(TAG, "??????5??????command: ");
                 timerCreator(mac);
             }
         }
     }

     //????????????
    private void startAlarm(){
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mediaPlayer = MediaPlayer.create(this, notification);
        mediaPlayer.setLooping(true);  //??????????????????
        mediaPlayer.start();
    }

    //????????????
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

         //???????????????Handler
         if(mHandler != null)
             mHandler.removeCallbacksAndMessages(null);

         //????????????ble???????????? 2021/05/05
         if (!userMap.isEmpty())
            userMap.clear();

         if (!countDownTimerArrayMap.isEmpty())
             countDownTimerArrayMap.clear();

         //???????????????????????????...
         if (chartDialog != null && chartDialog.isShowing())
             chartDialog.dismiss();
     }

     @Override //??????????????????????????? 2021/03/24
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK){
            setInfo(); //???????????????????????????RecyclerView
        }else {
            Toasty.info(this, getString(R.string.nothing), Toast.LENGTH_SHORT, true).show();
        }
    }
}


