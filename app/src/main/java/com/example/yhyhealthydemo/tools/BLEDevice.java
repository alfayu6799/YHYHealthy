package com.example.yhyhealthydemo.tools;

import android.bluetooth.BluetoothDevice;

public class BLEDevice {
    private BluetoothDevice bluetoothDevice;
    private int RSSI;

    public BLEDevice(BluetoothDevice bluetoothDevice, int RSSI) {
        this.bluetoothDevice = bluetoothDevice;
        this.RSSI = RSSI;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public int getRSSI() {
        return RSSI;
    }

}
