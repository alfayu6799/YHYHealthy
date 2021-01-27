package com.example.yhyhealthydemo.module;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ScrollView;

import com.example.yhyhealthydemo.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http2.Header;

/*******************
 * 處理網路請求
 * 單例模式
 * 第三方套件 : OKHTTP
* **********************/
public class ApiProxy {

    private static final String TAG = "ApiProxy";

    private static ApiProxy INSTANCE = null;

    //Api網址
    private static String URL = "http://192.168.1.108:8080/";

    //註冊用api
    public static String REGISTER = "allUser/users/register";

    //登入api
    public static String LOGIN = "allUser/users/login";

    //驗證碼api
    public static String COMP = "allUser/ext/comp";

    //查詢用戶資訊api
    public static String USER_INFO = "allUser/users/info";

    //更新用戶資訊api
    public static String USER_UPDATE = "allUser/users/update";

    //忘記密碼api
    public static String FORGET_PASSWORD = "allUser/users/forget";

    //更新密碼api
    public static String CHANGE_PASSWORD = "allUser/users/change";

    //經期是否已設定api
    public static String MENSTRUAL_EXISTS = "allAiniita/aplus/MenstrualExists";

    //查詢經期設定資訊api
    public static String MENSTRUAL_RECORD_INFO = "allAiniita/aplus/MenstrualRecordInfo";

    //更新經期設定api
    public static String MENSTRUAL_RECORD_UPDATE = "allAiniita/aplus/MenstrualRecord";

    //週期狀態api(月曆與圖表)
    public static String CYCLE_RECORD = "allAiniita/aplus/CycleRecord";

    //排卵紀錄查詢api
    public static String RECORD_INFO = "allAiniita/aplus/RecordInfo";

    //排卵紀錄更新api
    public static String RECORD_UPDATE = "allAiniita/aplus/Record";

    //唾液圖片辨識api
    public static String IMAGE_DETECTION = "allAiniita/aplus/ImgDetection";

    //實際經期設定api
    public static String PERIOD_UPDATE = "allAiniita/aplus/PeriodData";

    //查詢婚姻狀況api
    public static String MARRIAGE_INFO = "allAiniita/aplus/MarriageInfo";

    //更新婚姻狀況api
    public static String MARRIAGE = "allAiniita/aplus/Marriage";

    //
    private static final String AUTHORIZATION = "Authorization";
    private static final String SCEPTER = "Scepter";
    private static String authToken;
    private static String scepterToken;

    //註冊專用Authorization token
    private static final String REGISTER_AUTH_CODE = "$2a$10$x42hx/UBe.PxFEoAk0RyuO0ImZ4h71hptmgvIF1sRZxA1HFqjJUAK";
    //忘記密碼專用Authorization token
    private static final String FORGET_AUTH_CODE = "$2a$10$yXAkhpwHtBm6Ws0dYohU5OzcjpkWW5QOCW7d6LOnVFPjDbnCjeciO";
    //驗證碼專用Authorization token
    private static final String VERIFICATION_CODE = "$2a$10$Ymh9oITzzZN3KZVDzajXZODBZqHXBrCexz1I3P5nhRL14cDDOZxH6";

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

    //忘記密碼專用
    public void buildPassWD(String action, String body, OnApiListener listener){
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request.Builder request = new Request.Builder();
        request.url(URL + action);
        request.post(requestBody);
        request.addHeader("Authorization", FORGET_AUTH_CODE);
        buildRequest(request.build(), listener);
    }

    //驗證碼專用
    public void buildInit(String action, String body, OnApiListener listener){
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request.Builder request = new Request.Builder();
        request.url(URL + action);
        request.post(requestBody);
        request.addHeader("Authorization", VERIFICATION_CODE);
        buildRequest(request.build(), listener);
    }

    //註冊專用
    public void buildRegister(String action, String body, OnApiListener listener){
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request.Builder request = new Request.Builder();
        request.url(URL + action);
        request.post(requestBody);
        request.addHeader("Authorization", REGISTER_AUTH_CODE);
        request.addHeader("DefaultLan","zh-TW");  //不能省略
        buildRequest(request.build(), listener);
    }

    //登入專用
    public void buildLogin(String action, String body, OnApiListener listener){
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request.Builder request = new Request.Builder();
        request.url(URL + action);
        request.post(requestBody);
        request.addHeader("Authorization", "xxx");
        buildRequest(request.build(), listener);
    }

    //POST JSON
    public void buildPOST(String action, String body, OnApiListener listener){
        String authToken1 = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2LTMtMjAiLCJpYXQiOjE2MTE2NTIzMjMsImV4cCI6MTYxMTY1NTkyM30.JvZKoy7Q_Aj4WAQ1bOK3r4K_mt7r9VkQCKgXleas2__SrS09yiy60sA7MvJHFPwgSvGU7-QznhW96ocDHtSklg";
        String scepterToken1 = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2LTMtMjAiLCJpYXQiOjE2MTE2NTIzMjN9.4IpAvwzoCBo-Kite2sih36LBZ5yTgBRCgoiAuJT8Z_p1tCu4bAsgontZqqsdG5EeQWML3nIHlhWwYwNwf4pLLQ";
        Log.d(TAG, "buildPOST authToken: " + authToken);
        Log.d(TAG, "buildPOST scepterToken: " + scepterToken);

        RequestBody requestBody = RequestBody.create(JSON, body);
        Request.Builder request = new Request.Builder();
        request.url(URL + action);
        request.post(requestBody);
        request.addHeader(AUTHORIZATION, authToken);
        request.addHeader(SCEPTER, scepterToken);
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
//                String string = response.body().string();

                if(response.header("Authorization") != null){  //如果回覆是空值的話則不要複寫
                    authToken = response.header("Authorization");
                }

                if(response.header("Scepter") != null){   //如果回覆是空值的話則不要複寫
                    scepterToken = response.header("Scepter");
                }

                Log.d(TAG, "onResponse auth: " + authToken);
                Log.d(TAG, "onResponse scepter: " + scepterToken);

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response.body().string());
                    int erCode = jsonObject.getInt("errorCode");
                    if(erCode != 0){  //如果errorCode有訊息
                        listener.onSuccess(jsonObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (code == 200 ){
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