package com.example.yhyhealthydemo.module;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.yhyhealthydemo.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/*******************
 * 處理網路請求
 * 單例模式
 * 第三方套件 : OKHTTP
* **********************/
public class ApiProxy {

    private static final String TAG = "ApiProxy";

    private static ApiProxy INSTANCE = null;

    //Api網址
    private static String URL = "http://192.168.1.108:8080/allAiniita/aplus/";

    //註冊用api
    public static String REGISTER = "register";

    //查詢用戶資訊api
    public static String USER_INFO = "info";

    //更新用戶資訊api
    public static String USER_UPDATE = "update";

    //忘記密碼api
    public static String FORGET_PASSWORD = "forget";

    //更新密碼api
    public static String CHANGE_PASSWORD = "change";

    //經期是否已設定api
    public static String MENSTRUAL_EXISTS = "MenstrualExists";

    //查詢經期設定資訊api
    public static String MENSTRUAL_RECORD_INFO = "MenstrualRecordInfo";

    //更新經期設定api
    public static String MENSTRUAL_RECORD_UPDATE = "MenstrualRecord";

    //週期狀態api(月曆與圖表)
    public static String CYCLE_RECORD = "CycleRecord";

    //排卵紀錄查詢api
    public static String RECORD_INFO = "RecordInfo";

    //排卵紀錄更新api
    public static String RECORD_UPDATE = "Record";

    //唾液圖片辨識api
    public static String IMAGE_DETECTION = "ImgDetection";

    //實際經期設定api
    public static String PERIOD_UPDATE = "PeriodData";

    //查詢婚姻狀況api
    public static String MARRIAGE_INFO = "MarriageInfo";

    //更新婚姻狀況api
    public static String MARRIAGE = "Marriage";

    //單例化
    private YHYHealthyApp app;

    public static void initial(Application c){
        INSTANCE = new ApiProxy(c);
    }

    private ApiProxy(Application c) {app = (YHYHealthyApp) c; }

    public static ApiProxy getInstance(){
        return INSTANCE;
    }

    //okhttp 套件
    private static OkHttpClient client;

    private OkHttpClient buildClient(){
        if (client == null)
            client = new OkHttpClient.Builder().build();
        return client;
    }

    private static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");

    //POST JSON
    public void buildPOST(String action, String body, OnApiListener listener){
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request.Builder request = new Request.Builder();
        request.url(URL + action);
        request.post(requestBody);
        request.addHeader("Authorization","xxx");
        buildRequest(request.build(), listener);
    }

    private void buildRequest(Request req, OnApiListener listener) {

        Call call = buildClient().newCall(req);

        ConnectivityManager cm = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            listener.onFailure(app.getString(R.string.api_unavailable));

            return;
        }

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "後台Api失敗: ");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                int code = response.code();
                assert response.body() != null;
                String string = response.body().string();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(string);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (code == 200){
                    listener.onSuccess(jsonObject);
                }
                listener.onPostExecute();
            }
        });
    }

    public interface OnApiListener{
        void onPreExecute();
        void onSuccess(JSONObject result);
        void onFailure(String message);
        void onPostExecute();
    }
}