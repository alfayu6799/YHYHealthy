package com.example.yhyhealthydemo;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

import es.dmoral.toasty.Toasty;

import static android.text.TextUtils.isEmpty;

public class yhyBleService extends Service {

    private static final String TAG = "yhyBleService";

    private BluetoothGatt mBluetoothGatt;

    private BluetoothAdapter mBluetoothAdapter;

    private ArrayMap<String, BluetoothGatt> gattArrayMap = new ArrayMap<>();

    // 蓝牙连接状态
    private int mConnectionState = 0;
    // 蓝牙连接已断开
    private final int STATE_DISCONNECTED = 0;
    // 蓝牙正在连接
    private final int STATE_CONNECTING = 1;
    // 蓝牙已连接
    private final int STATE_CONNECTED = 2;

    // 蓝牙已连接
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetoothletest.ACTION_GATT_CONNECTED";
    // 蓝牙已断开
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetoothletest.ACTION_GATT_DISCONNECTED";
    // 发现GATT服务
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetoothletest.ACTION_GATT_SERVICES_DISCOVERED";
    // 收到蓝牙数据
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetoothletest.ACTION_DATA_AVAILABLE";
    // 连接失败
    public final static String ACTION_CONNECTING_FAIL = "com.example.bluetoothletest.ACTION_CONNECTING_FAIL";
    // 藍芽數據
    public final static String EXTRA_DATA = "com.example.bluetoothletest.EXTRA_DATA";
    // 藍芽MAC
    public final static String EXTRA_MAC = "com.example.bluetoothletest.EXTRA_MAC";
    // 藍芽名稱
    public final static String EXTRA_DEVICE_NAME = "com.example.bluetoothletest.EXTRA_DEVICE_NAME";
    // 藍芽通知
    public final static String ACTION_NOTIFY_ON = "com.example.bluetoothletest.ACTION_NOTIFY_ON";
    // 藍芽斷開指定的設備
    public final static String ACTION_GATT_DISCONNECTED_SPECIFIC = "com.example.bluetoothletest.ACTION_GATT_DISCONNECTED_SPECIFIC";


    // 服务标识
    private final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    // 特征标识（读取数据）
    private final UUID CHARACTERISTIC_READ_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    // 特征标识（发送数据）
    private final UUID CHARACTERISTIC_WRITE_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    // 描述标识
    private final UUID DESCRIPTOR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    @Override
    public void onCreate() {
        super.onCreate();
        initBluetooth();
    }

    private void initBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    // 服務相關
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public yhyBleService getService() {
            return yhyBleService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        release();
        return super.onUnbind(intent);
    }

    /**
     * 藍芽操作回調
     * 藍芽有連接狀態才會回調
     * 當連接狀態發生改變時一定會回調這個方法
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            BluetoothDevice device = gatt.getDevice();
            String address = device.getAddress();

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                gattArrayMap.put(address, gatt);

                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED, gatt);

                Log.d(TAG, "onConnectionStateChange : STATE_CONNECTED: ");

                mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED, gatt);

                Log.d(TAG, "onConnectionStateChange : STATE_DISCONNECTED: " );

            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            // 发现GATT服务
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, gatt);
                setBleNotification(); //啟動通知
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead: ???");
            // 收到数据
            broadcastUpdate(ACTION_DATA_AVAILABLE, gatt);
        }

        @Override  //寫資料
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicWrite: ");
        }

        @Override  //接受到手機端的command後藍芽回覆的資料
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (characteristic.getValue() != null){
                String result = new String(characteristic.getValue());
                String[] str = result.split(","); //以,切割
                String temp = str[2];
                double degree = Double.parseDouble(temp)/100;  //25.0
                //Log.d(TAG, "onCharacteristicChanged: " + degree + " device mac:" + gatt.getDevice().getAddress());
                broadcastUpdate(ACTION_DATA_AVAILABLE, gatt, characteristic);
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
            Log.d(TAG, "onDescriptorWrite: ");
        }
    };

    /**
     * 發送通知
     *
     * @param action 廣播Action
     */
    private void broadcastUpdate(String action, BluetoothGatt gatt){
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_MAC, gatt.getDevice().getAddress());
        intent.putExtra(EXTRA_DEVICE_NAME, gatt.getDevice().getName());
        sendBroadcast(intent);
    }

    /**
     * 發送通知
     *
     * @param action 廣播Action
     * @param characteristic 數據
     */
    private void broadcastUpdate(String action, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
        final Intent intent = new Intent(action);
        if (CHARACTERISTIC_READ_UUID.equals(characteristic.getUuid())) {
            intent.putExtra(EXTRA_DATA, characteristic.getValue());
            intent.putExtra(EXTRA_MAC, gatt.getDevice().getAddress());
            intent.putExtra(EXTRA_DEVICE_NAME, gatt.getDevice().getName());
        }
        sendBroadcast(intent);
    }

    /**
     * 藍牙連接
     *
     * @param address      藍芽MAC
     */
    public synchronized void connect(final String address) {
        BluetoothGatt gatt = gattArrayMap.get(address);
        if (gatt != null){
            gatt.disconnect();
            gatt.close();
            gattArrayMap.remove(address);
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null)
            return;

        mBluetoothGatt = device.connectGatt(this,false,mGattCallback);

        mConnectionState = STATE_CONNECTING;
    }


    /**
     * 藍牙斷開連接
     */
    public void disconnect() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * 釋放相關資源
     */
    public void release() {
        Log.d(TAG, "釋放相關資源: ");
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close(); //關閉gatt避免資源浪費
        mBluetoothGatt = null;
    }

    /***
     * 手動針對mac進行斷線處理
     * 2021/04/28
     * **/
    public void closeGatt(String mac){
        if (!isEmpty(mac) && gattArrayMap.containsKey(mac)){
            BluetoothGatt mBluetoothGatt = gattArrayMap.get(mac);
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();      //關閉gatt避免資源浪費
            broadcastUpdate(ACTION_GATT_DISCONNECTED_SPECIFIC, mBluetoothGatt);
        }
    }

    /**
     * 設置藍牙設備在數據改變時，通知App
     */
    private void setBleNotification(){
        //1.獲取藍牙設備的服務
        BluetoothGattService gattService = mBluetoothGatt.getService(SERVICE_UUID);
        if (gattService != null){
            BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(DESCRIPTOR_UUID);
            if(gattCharacteristic != null){
                boolean success = mBluetoothGatt.setCharacteristicNotification(gattCharacteristic,true);
                if(success){
                    for(BluetoothGattDescriptor dp: gattCharacteristic.getDescriptors()){
                        if (dp != null){
                            dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            boolean isSuccess = mBluetoothGatt.writeDescriptor(dp);
                            Log.d(TAG, "setBleNotification: " + isSuccess + " dp:" + dp.getUuid().toString());
                            broadcastUpdate(ACTION_NOTIFY_ON, mBluetoothGatt);
                        }
                    }
                }
            }
        }
    }

    /***   ****
     *  藍芽發送訊息
     * @param data 數據
     * @param address 藍芽位置
     *
     * **  **********/
    public synchronized void writeDataToDevice(byte[] data, String address){
        BluetoothGatt mBluetoothGatt = gattArrayMap.get(address);
        if (mBluetoothGatt == null) return;
        BluetoothGattService gattService = mBluetoothGatt.getService(SERVICE_UUID);

        if(gattService == null) return;
        BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(CHARACTERISTIC_WRITE_UUID);

        if (gattCharacteristic == null) return;

        //發送訊息
        gattCharacteristic.setValue(data);

        mBluetoothGatt.writeCharacteristic(gattCharacteristic);
    }

    public static IntentFilter makeIntentFilter(){
        final IntentFilter filter = new IntentFilter();
        filter.addAction(yhyBleService.ACTION_GATT_CONNECTED);
        filter.addAction(yhyBleService.ACTION_GATT_DISCONNECTED);
        filter.addAction(yhyBleService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(yhyBleService.ACTION_DATA_AVAILABLE);
        filter.addAction(yhyBleService.ACTION_NOTIFY_ON);
        filter.addAction(yhyBleService.ACTION_CONNECTING_FAIL);
        filter.addAction(yhyBleService.EXTRA_MAC);
        return filter;
    }

    @Override
    public void onDestroy() {
        gattArrayMap.clear();
        super.onDestroy();
    }
}
