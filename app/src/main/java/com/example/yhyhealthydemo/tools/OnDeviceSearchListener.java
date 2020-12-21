package com.example.yhyhealthydemo.tools;

/**
 * 藍牙設備搜索監聽者
 * 1、開啟搜索
 * 2、完成搜索
 * 3、搜索到設備
 */

public interface OnDeviceSearchListener {
    void onDeviceFound(BLEDevice bleDevice); //搜索到設備
    void onDiscoveryOutTime(); //掃描超時
}
