package com.example.yhyhealthydemo.tools;

import android.app.Application;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/*******************
 * 處理網路請求
 * 單例模式
 * 第三方套件 : OKHTTP
* **********************/
public class ApiUtil {

    private static ApiUtil INSTANCE = null;

    //Api網址
    private static String URL = "http://192.168.1.108:8080/allAiniita/aplus/";

    //排卵紀錄查詢網址
    private static String RECORD_INFO = "RecordInfo";

    //排卵紀錄更新網址
    private static String RECORD_UPDATE = "Record";

    //經期是否已設定網址
    private static String MENSTRUAL_EXISTS = "MenstrualExists";

    private static OkHttpClient client;


    private OkHttpClient buildClient(){
        if (client == null)
            client = new OkHttpClient.Builder().build();
        return client;
    }

    private static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");

    public void buildPOST(String action,String body){
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request.Builder request = new Request.Builder();
        request.url(URL + action);
        request.post(requestBody);

    }
}