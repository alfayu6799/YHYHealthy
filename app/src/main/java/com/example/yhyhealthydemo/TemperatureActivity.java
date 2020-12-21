package com.example.yhyhealthydemo;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.yhyhealthydemo.adapter.RecyclerViewAdapter;
import com.example.yhyhealthydemo.adapter.RemoteViewAdapter;
import com.example.yhyhealthydemo.adapter.TempViewAdapter;
import com.example.yhyhealthydemo.data.Remote;
import com.example.yhyhealthydemo.data.ScannedData;
import com.example.yhyhealthydemo.dialog.AddTemperatureDialog;
import com.example.yhyhealthydemo.dialog.ChartDialog;
import com.example.yhyhealthydemo.datebase.Member;
import com.example.yhyhealthydemo.tools.RecyclerViewListener;
import com.example.yhyhealthydemo.tools.SpacesItemDecoration;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class TemperatureActivity extends AppCompatActivity implements View.OnClickListener, RecyclerViewListener {

    private final static String TAG = "TemperatureActivity";

    private Button supervise, remote;
    private Button addTemperatureUser, addRemoteUser;

    //使用者
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter;
    private List<Member> members;
    private String name;
    private Double degree = 00.00;
    private Member user1; //假資料
    private Member user2; //假資料
    private Member user3; //假資料
    private Member user4; //假資料
//    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //日期格式
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm"); //日期格式

    //遠端
    private RecyclerView remoteRecycle;
    private RemoteViewAdapter remoteAdapter;
    private List<Remote> remotes;
    private Remote remote1;  //假資料
    private Remote remote2;  //假資料
    private Button refresh;  //啟動量測

    //藍芽相關
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothGattService gattService;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic characteristic;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 102;
    private static final int REQUEST_ENABLE_BT = 2;
    private boolean isScanning = false;
    private ArrayList<ScannedData> findDevice = new ArrayList<>();
    private RecyclerView bleRecycleView;
    private TempViewAdapter tempAdapter;
    private Handler mHandler;                    //Handler用來搜尋Devices10秒後，自動停止搜尋
    //藍芽服務UUID設置
    private static final UUID TEMPERATURE_SERVICE_UUID = UUID
            .fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID TEMPERATURE_NOTIF_UUID = UUID
            .fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID TEMPERATURE_WRITE_DATA = UUID
            .fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");

    private ChartDialog chartDialog;
    //將資料寫到sharepreferences
    private SharedPreferences temperatureInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        Log.d(TAG, "onCreate: ");

        //init sharepreferences
        temperatureInfo = getSharedPreferences("temperature", MODE_PRIVATE); //只允許本應用程式內存取

        initView();
    }

    private void initView(){
        supervise = (Button) findViewById(R.id.bt_select_supervise);
        remote = (Button) findViewById(R.id.bt_select_remote);
        addTemperatureUser = (Button) findViewById(R.id.bt_add_temp);
        addRemoteUser = (Button) findViewById(R.id.bt_add_remote);

        refresh = findViewById(R.id.btnRefresh);
        refresh.setOnClickListener(this);

        //init RecyclerView's data
        recyclerView = findViewById(R.id.rvTempUser);
        bleRecycleView = findViewById(R.id.rvSignUser);
        remoteRecycle = findViewById(R.id.rvRomoteUser);

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

    ///////////////////////////藍芽/////////////////////////////////////////////////////////////////////
    private void initBle() {
        /**啟用藍牙適配器*/
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        /**開始掃描*/
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        Log.d(TAG, "initBle: first start scan !!");
        isScanning = true;
        /**設置Recyclerview列表*/
        tempAdapter = new TempViewAdapter();
        bleRecycleView.setAdapter(tempAdapter);
        bleRecycleView.setHasFixedSize(true);
        bleRecycleView.setLayoutManager(new LinearLayoutManager(this));

        mHandler = new Handler();
        if (isScanning){  //開始掃描
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);//停止搜尋
                    Toast.makeText(TemperatureActivity.this, getString(R.string.ble_stop_in_5secs), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "initBle: 5秒時間到停止掃描");
                }
            },10 *500);
            isScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            findDevice.clear();
            tempAdapter.clearDevice();
            Log.d(TAG, "initBle: 開始掃描");
        }else {
            isScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);//停止搜尋
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
    private TempViewAdapter.OnItemClick itemClick = new TempViewAdapter.OnItemClick() {
        @Override
        public void onItemClick(ScannedData selectedDevice) {

            String bleAddress = selectedDevice.getAddress();
            Log.d(TAG, "選擇的藍芽裝置: " + bleAddress);

            /** 觀測者 layout顯示 , 而藍芽layout隱藏 */
            recyclerView.setVisibility(View.VISIBLE);
            addTemperatureUser.setVisibility(View.VISIBLE);
            bleRecycleView.setVisibility(View.GONE);

            //獲取設備
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bleAddress);
            //藍芽gett開始連線
            gatt = device.connectGatt(TemperatureActivity.this, false, mGattCallback);
        }
    };

    //gatt連上線
   private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
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

           if (newState == BluetoothGatt.STATE_CONNECTED){
               Log.d(TAG, "連結成功 : 跑到discoverServices");
               gatt.discoverServices();  //啟動服務
           }else if (newState == BluetoothGatt.STATE_DISCONNECTED){
               Log.d(TAG, "斷開連結並釋放資源");
               gatt.close();
               update(); //20201210 刷新RecyclerView
           }else if (newState == BluetoothGatt.STATE_CONNECTING){
               Log.d(TAG, "正在連結.." );

           }else if (newState == BluetoothGatt.STATE_DISCONNECTING){
               Log.d(TAG, "正在斷開..");
           }
       }

       @Override
       public void onServicesDiscovered(BluetoothGatt gatt, int status) {
           Log.d(TAG, String.format("onServicesDiscovered:%s,%s", gatt.getDevice().getName(), status));
           if (status == gatt.GATT_SUCCESS) { //發現BLE服務成功
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gattService = gatt.getService(TEMPERATURE_SERVICE_UUID); //獲取64e0001服務
                        if (gattService != null){
                            characteristic = gattService.getCharacteristic(TEMPERATURE_NOTIF_UUID); //獲取64e0003服務通知
                            if (characteristic != null){
                                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()){
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);  //啟動notif通知
                                    boolean sucess = gatt.writeDescriptor(descriptor);
                                    Log.d(TAG, "onServicesDiscovered : writeDescriptor = " + sucess);
                                }
                                gatt.setCharacteristicNotification(characteristic, true); //notif listener
                                Log.d(TAG, "onServicesDiscovered的通知啟動成功");
                                //要顯示"已連線"的訊息在RecyclerView's 項目
                                updateStatus(name);
                            }
                        }
                    }
                });
           }
       }

       @Override
       public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
           super.onCharacteristicRead(gatt, characteristic, status);
           Log.d(TAG, "onCharacteristicRead: ");
       }

       @Override
       public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
           super.onCharacteristicWrite(gatt, characteristic, status);
           Log.d(TAG, "onCharacteristicWrite: this is onCharacteristicWrite !!");
       }

       //接受到手機端的command後藍芽回覆的資料
       @Override
       public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
           super.onCharacteristicChanged(gatt, characteristic);
           if (characteristic.getValue() != null){
               String result = new String(characteristic.getValue());
               String[] str = result.split(",");
               String temp = str[2];
               degree = Double.parseDouble(temp)/100;  //25.0
               Log.d(TAG, "onCharacteristicChanged: Characteristic get value : " + degree);  //result : AIDO,0,2500,100
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       update(); //更新  20201216
                   }
               });

           }
       }

       @Override
       public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
           super.onDescriptorRead(gatt, descriptor, status);
           Log.d(TAG, "onDescriptorRead: ");
       }

       @Override
       public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
           super.onDescriptorWrite(gatt, descriptor, status);
           Log.d(TAG, "onDescriptorWrite: success");
       }

       @Override
       public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
           super.onReliableWriteCompleted(gatt, status);
       }

       @Override
       public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
           super.onReadRemoteRssi(gatt, rssi, status);
       }

       @Override
       public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
           super.onMtuChanged(gatt, mtu, status);
       }
   };

   ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //RecyclerView's Item 填入Data
    private void setInfo() {
        int spacingInPixels = 10;  //設定item間距的距離
        members = new ArrayList<>();

        setMemberDate(); //填入資料

        mAdapter = new RecyclerViewAdapter(this, members, this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //設定item間距
    }

    private void setMemberDate() {  //日後要從api拉回照片跟姓名等資料

        user1 = new Member(R.mipmap.imageview, "Matt Bomer");
        user2 = new Member(R.mipmap.imageview2, "Brad Pitt");
        user3 = new Member(R.mipmap.imageview3, "Anne Hathaway");
        user4 = new Member(R.mipmap.image4, "Emma Watson");

        members.add(user1);
        members.add(user2);
        members.add(user3);
        members.add(user4);
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
            case R.id.bt_add_temp:       //新增觀測者Button
                dialogTemperature();
                break;
            case R.id.bt_add_remote:     //新增遠端者Button
                dialogRemote();
                break;
            case R.id.btnRefresh:       //發送藍芽command
                Log.d(TAG, "按下發送藍芽的command : ");
                supervise.setEnabled(true);  //取消觀測Button禁用
                remote.setEnabled(true);     //取消遠端Button禁用

                //計時5分鐘送一次command
                requestTemp.run();

                break;
        }
    }

    //遠端帳號新增彈跳視窗
    private void dialogRemote() {
        AlertDialog remoteDialog = new AlertDialog.Builder(this).create();
        LayoutInflater remotelayout = LayoutInflater.from(this);
        View remoteView = remotelayout.inflate(R.layout.dialog_remote_add, null);
        remoteDialog.setView(remoteView);
        remoteDialog.setCancelable(false); //禁用非視窗區

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
                //送資料到後台
                //RemoteApi();
                remoteDialog.dismiss();
            }
        });

        remoteDialog.show();
    }


    private void updateStatus(String name){
        //使用者手動按下啟用5分鐘讀取一次藍芽體溫資料
        refresh.setVisibility(View.VISIBLE); //20201218
        //改更藍芽狀態的文字顯示
        if(this.name != null){
            for (int j = 0; j < members.size(); j++){
                if (members.get(j).getName().equals(name)){
                    Member user = members.get(j);
                    user.setStatus("已連線");
                    members.set(j, user);
                    mAdapter.updateItem(user, j);
                }
            }
        }
    }

    //更新收到體溫的訊息給RecyclerView的項目
    private void update(){
        String currentDateTime;
        currentDateTime = sdf.format(new Date());  // 目前時間

        //如果有量到體溫的話按鈕隱藏
        if (degree != 0.0){
            refresh.setVisibility(View.GONE);
        }

        if (name != null){
            for(int i = 0; i < members.size(); i++){
                Log.d(TAG, "update: " + name + " time: " + currentDateTime + " degree :" + degree);
                if (members.get(i).getName().equals(name)) {
                    Member user = members.get(i);
                    user.setDegree(degree, currentDateTime);
                    user.setBattery("100%");
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

        //寫入sharePreferences
        if (degree != 00.00){
            Gson gson = new Gson();
            Type listOfMemberObject = new TypeToken<List<Member>>(){}.getType();
            String s = gson.toJson(members, listOfMemberObject);   //20201215
            //寫入Local端  20201215
            temperatureInfo.edit().putString("degree", s).apply();
            //寫回Api
//            temperatureToApi();
        }
    }

    private void sendCommand() {
        if (characteristic != null) { //確保write uuid要有資料才能寫資料
            characteristic = gattService.getCharacteristic(TEMPERATURE_WRITE_DATA); //AIDO寫入uuid : 64e00002
            String request = "AIDO,0"; //詢問溫度command
            byte[] messageBytes = new byte[0];
            try {
                messageBytes = request.getBytes("UTF-8"); //Sting to byte
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Failed to convert message string to byte array");
            }
            characteristic.setValue(messageBytes);    //詢問溫度Command
            boolean success = gatt.writeCharacteristic(characteristic);

            Log.d(TAG, "sendCommand 2: " + success);
        }
    }

    //每5分鐘執行一次
    private Runnable requestTemp = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "requestTemp start: @5mins");
            sendCommand();
            mHandler.postDelayed(this, 1000 * 60 * 1);    //5mins
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
    }

//    //禁用返回鍵 20201218
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//
//            return true;
//        }
//        return false;
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: gatt close!!");
        if (mHandler != null) {
            mHandler.removeCallbacks(requestTemp);
            Log.d(TAG, "onDestroy: remove Handler");
        }
        if(isScanning){
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        gattClose(); //斷開藍芽
    }

    //斷開藍芽
    private void gattClose() {
        if (gatt == null) {
            return;
        }
        gatt.close();
        gatt = null;
        Toast.makeText(TemperatureActivity.this, getString(R.string.ble_is_not_connect), Toast.LENGTH_SHORT).show();
    }

    //新增觀測者Dioalog
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

    ///////////////////////////來自Adapter的callBack////////////////////////////////////

    //呼叫藍芽
    @Override
    public void onBleConnect(Member member) {
        name = member.getName();  //取得使用者名稱
        //初始化及相關搜尋
        initBle();
        tempAdapter.OnItemClick(itemClick);
        /** 觀測者 layout隱藏 , 而藍芽layout顯示 */
        recyclerView.setVisibility(View.GONE);
        addTemperatureUser.setVisibility(View.GONE);
        bleRecycleView.setVisibility(View.VISIBLE);
        supervise.setEnabled(false);  //禁用觀測Button
        remote.setEnabled(false);     //禁用遠端Button
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


