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
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class yhyBleService extends Service {

    private static final String TAG = "yhyBleService";

    private Context context;

    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    // 蓝牙连接状态
    private int mConnectionState = 0;
    // 蓝牙连接已断开
    private final int STATE_DISCONNECTED = 0;
    // 蓝牙正在连接
    private final int STATE_CONNECTING = 1;
    // 蓝牙已连接
    private final int STATE_CONNECTED = 2;

    // 蓝牙已连接
    public final static String ACTION_GATT_CONNECTED = "com.example.yhyhealthydemo.ACTION_GATT_CONNECTED";
    // 蓝牙已断开
    public final static String ACTION_GATT_DISCONNECTED = "com.example.yhyhealthydemo.ACTION_GATT_DISCONNECTED";
    // 发现GATT服务
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.yhyhealthydemo.ACTION_GATT_SERVICES_DISCOVERED";
    // 啟動通知服務    Notification
    public final static String ACTION_NOTIFICATION_SUCCESS = "com.example.yhyhealthydemo.ACTION_NOTIFICATION_SUCCESS";
    // 收到蓝牙数据
    public final static String ACTION_DATA_AVAILABLE = "com.example.yhyhealthydemo.ACTION_DATA_AVAILABLE";
    // 连接失败
    public final static String ACTION_CONNECTING_FAIL = "com.example.yhyhealthydemo.ACTION_CONNECTING_FAIL";
    // 蓝牙数据
    public final static String EXTRA_DATA = "com.example.yhyhealthydemo.EXTRA_DATA";

    // 服务标识
    private final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    // 特征标识（读取数据）
    private final UUID CHARACTERISTIC_READ_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    // 特征标识（发送数据）
    private final UUID CHARACTERISTIC_WRITE_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    // 描述标识
    private final UUID DESCRIPTOR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    // 服务相关
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

    /** Service建立完成後 , 執行此區塊 , 建立藍芽連線所需物件 **/
    public boolean initialize(Context context){

        if (mBluetoothManager == null){
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null)
                return false;
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null)
            return false;

        if (!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable(); //自動啟動藍芽

        this.context = context;

        return true;
    }


    /**
     * 藍牙操作callback
     * 藍牙有連接狀態才會callback
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "onConnectionStateChange: 藍牙已連接");
                // 藍牙已連接
                mConnectionState = STATE_CONNECTED;
                sendBleBroadcast(ACTION_GATT_CONNECTED);
                // 搜尋GATT服務
                mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "onConnectionStateChange: 藍牙斷開連接");
                // 藍牙斷開連接
                mConnectionState = STATE_DISCONNECTED;
                sendBleBroadcast(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            // 發現GATT服務
            if (status == BluetoothGatt.GATT_SUCCESS) {
                setBleNotification(); //通知
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead: 收到數據");
            // 收到數據
            sendBleBroadcast(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override  //寫資料
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicWrite: 寫資料成功");
        }

        @Override  //接受到手機端的command後藍芽回覆的資料
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "接受到手機端的command後藍芽回覆的資料 ");
            if (characteristic.getValue() != null){
                sendBleBroadcast(ACTION_DATA_AVAILABLE, characteristic);
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
            Log.d(TAG, "開啟通知模式成功: ");
            sendBleBroadcast(ACTION_NOTIFICATION_SUCCESS);
        }
    };

    /**
     * 發送通知
     *
     * @param action 廣播Action
     */
    private void sendBleBroadcast(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     * 發送通知
     *
     * @param action         廣播Action
     * @param characteristic 數據
     */
    private void sendBleBroadcast(String action, BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent(action);
        if (CHARACTERISTIC_READ_UUID.equals(characteristic.getUuid())) {
            intent.putExtra(EXTRA_DATA, characteristic.getValue());
        }
        sendBroadcast(intent);
    }

    /**
     * 藍牙連接
     *
     * @param bluetoothAdapter BluetoothAdapter
     * @param address          設備mac地址
     * @return true：成功 false：
     */
    public boolean connect(BluetoothAdapter bluetoothAdapter, String address) {
        if (bluetoothAdapter == null || TextUtils.isEmpty(address)) {
            return false;
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            return false;
        }
        mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * 斷開連接
     */
    public void disconnect() {
        if (mBluetoothGatt == null) {
            return;
        }
        Toasty.info(yhyBleService.this, getString(R.string.ble_is_not_connect), Toast.LENGTH_SHORT, true).show();
        mBluetoothGatt.disconnect();
    }

    /**
     * 釋放資源
     */
    public void release() {
        if (mBluetoothGatt == null) {
            return;
        }
        Toasty.info(yhyBleService.this, getString(R.string.ble_is_release), Toast.LENGTH_SHORT, true).show();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        sendBleBroadcast(ACTION_GATT_DISCONNECTED);
    }

    /**
     * 設置藍牙設備在數據改變時，通知App
     */
    private void setBleNotification(){
        BluetoothGattService gattService = mBluetoothGatt.getService(SERVICE_UUID);
        if (gattService != null){
            BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(DESCRIPTOR_UUID);
            if(gattCharacteristic != null){
                boolean success = mBluetoothGatt.setCharacteristicNotification(gattCharacteristic,true);
                if(success){
                    for(BluetoothGattDescriptor dp: gattCharacteristic.getDescriptors()){
                        if (dp != null){
                            dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            mBluetoothGatt.writeDescriptor(dp);
                        }
                    }
                }
            }
        }
    }


    /**
     * 發送command
     *
     * @param data 資料
     * @return true：發送成功 false：發送失敗
     */
    public boolean sendData(byte[] data) {
        //Log.d(TAG, "sendData: " + ByteUtils.byteArrayToHexString(data)); //4149444F2C30
        // 獲取藍牙設備的服務
        BluetoothGattService gattService = null;
        if (mBluetoothGatt != null) {
            gattService = mBluetoothGatt.getService(SERVICE_UUID);
        }
        if (gattService == null) {
            return false;
        }

        // 獲取藍牙設備的特徵
        BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(CHARACTERISTIC_WRITE_UUID);
        if (gattCharacteristic == null) {
            return false;
        }

        // command
        gattCharacteristic.setValue(data);

        return mBluetoothGatt.writeCharacteristic(gattCharacteristic);
    }
}
