package com.example.yhyhealthy.module;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.yhyhealthy.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import es.dmoral.toasty.Toasty;
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
//    private static final String URL = "http://192.168.1.144:8080/"; //內部網路 還需要去network_security_config.xml改為true才可以使用
    private static final String URL = "https://www.yhihc.com/test/";

    //衛教IP網址
    private static final String URL_EDUCATION = "https://www.yhihc.com/test/";

    //使用者api目錄
    private static final String USER_API = "allUser/users/";

    //註冊用api
    public static String REGISTER = USER_API + "register";

    //登入api
    public static String LOGIN = USER_API + "login";

    //查詢用戶資訊api
    public static String USER_INFO = USER_API + "info";

    //更新用戶資訊api
    public static String USER_UPDATE = USER_API + "update";

    //忘記密碼api
    public static String FORGET_PASSWORD = USER_API + "forget";

    //更新密碼api
    public static String CHANGE_PASSWORD = USER_API + "change";

    //更新驗證方式
    public static String CHANGE_VERIFICATION_STYLE = USER_API + "sendType";

    //查詢婚姻狀況api
    public static String MARRIAGE_INFO = USER_API + "marriageInfo";

    //更新婚姻狀況api
    public static String MARRIAGE_UPDATE = USER_API + "marriage";

    //裝置列表
    public static String PRODUCTS_NO = USER_API + "products";

    //綁定裝置(新增)
    public static String PRODUCTS_BIND = USER_API + "modifyProduct";

    //解除綁定裝置
    public static String PRODUCTS_BIND_REMOVE = USER_API + "delProduct";

    //App版本api
    public static String APP_VER = "allUser/ext/ver";

    //驗證碼比對api
    public static String COMP = "allUser/ext/comp";

    //重發驗證碼
    public static String COMP_CODE_REQUEST = "allUser/ext/code";

    //更新token
    public static String RENEW_TOKEN = "allUser/ext/renew";

    //經期行事曆api目錄
    public static final String MENSTRUAL_API = "allApp/aplus/";

    //查詢經期設定資訊api
    public static String MENSTRUAL_RECORD_INFO = MENSTRUAL_API + "MenstrualRecordInfo";

    //更新經期設定api
    public static String MENSTRUAL_RECORD_UPDATE = MENSTRUAL_API + "MenstrualRecord";

    //週期狀態api(月曆與圖表用)
    public static String CYCLE_RECORD = MENSTRUAL_API + "CycleRecord";

    //排卵紀錄查詢api
    public static String RECORD_INFO = MENSTRUAL_API + "RecordInfo";

    //排卵紀錄更新api
    public static String RECORD_UPDATE = MENSTRUAL_API + "Record";

    //唾液圖片辨識api
    public static String IMAGE_DETECTION = MENSTRUAL_API + "ImgDetection";

    //實際經期設定api(更新)
    public static String PERIOD_UPDATE = MENSTRUAL_API + "Period";

    //刪除實際經期設定api
    public static String PERIOD_DELETE = MENSTRUAL_API + "DelPeriod";

    //衛教文章分類api
    public static String EDU_ART_CATALOG = "yhyHe/article/getNewItemAttr";

    //衛教影片分類api
    public static String EDU_VIDEO_CATALOG = "yhyHe/video/getNewItemAttr";

    //衛教文章api網址
    public static String ARTICLE_LIST = "yhyHe/article/getNewList";

    //衛教影片api網址
    public static String VIDEO_LIST = "yhyHe/video/getNewList";

    //藍芽體溫api目錄
    public static final String BLE_TEMPERATURE_API = "allApp/aido/";

    //新增體溫量測資料
    public static String BLE_USER_ADD_VALUE = BLE_TEMPERATURE_API + "addValues";

    //新增觀測對象
    public static String BLE_USER_ADD = BLE_TEMPERATURE_API + "processTarget";

    //修改觀測對象資料
    public static String BLE_USER_UPDATE = BLE_TEMPERATURE_API + "updateTarget";

    //刪除觀測對象
    public static String BLE_USER_DELETE = BLE_TEMPERATURE_API + "delTarget";

    //查詢觀測對象列表
    public static String BLE_USER_LIST = BLE_TEMPERATURE_API + "target";

    //查詢遠端(本機)授權碼
    public static String MONITOR_CODE = BLE_TEMPERATURE_API + "monitorCode";

    //更新遠端(本機)授權碼
    public static String MONITOR_CODE_RENEW = BLE_TEMPERATURE_API + "renewCode";

    //更新遠端觀測帳號之授權碼
    public static String MONITOR_CODE_UPDATE = BLE_TEMPERATURE_API + "updCode";

    //新增遠端觀測帳號
    public static String REMOTE_USER_ADD = BLE_TEMPERATURE_API + "addGoal";

    //查詢遠端觀測帳號
    public static String REMOTE_USER_LIST = BLE_TEMPERATURE_API + "findGoal";

    //刪除遠端觀測帳號
    public static String REMOTE_USER_DELETE = BLE_TEMPERATURE_API + "delGoal";

    //遠端帳號底下觀測帳號之量測資料
    public static String REMOTE_USER_UNDER_LIST = BLE_TEMPERATURE_API + "goalValue";

    //取得初始症狀物件
    public static String SYMPTOM_LIST = BLE_TEMPERATURE_API + "defaultSymptom";

    //新增症狀
    public static String SYMPTOM_ADD = BLE_TEMPERATURE_API + "addAidoSymptom";

    //高燒服藥紀錄
    public static String FEVER_RECORD = BLE_TEMPERATURE_API + "medicineRecord";

    //歷史紀錄api目錄  2021/07/07
    public static String HISTORY_API = "allApp/history/";

    //歷史紀錄查詢觀測對象
    public static String HISTORY_TARGET = HISTORY_API + "target";

    public static String HISTORY_RECORD = HISTORY_API + "historyRecord";

    //教學影片
    public static String TEACHING_VIDEO = URL + "allPush/tutorial/video";

    private static final String AUTHORIZATION = "Authorization";
    private static final String SCEPTER = "Scepter";
    private static final String DEFAULTLAN = "DefaultLan";
    private static final String SYSTEM = "system";
    public static String authToken;     //2021/05/25變更為共用變數
    public static String scepterToken;  //2021/05/25變更為共用變數

    //記錄使用者相關的資訊變數
    public static boolean maritalSetting = false;    //婚姻設定
    public static boolean menstrualSetting = false;  //經期設定
    public static boolean userSetting = false;       //使用者設定
    public static String  accountInfo = "";          //使用者帳戶

    //covid-19疫苗
    public static String covid19Select ="";        //covid19疫苗

    //註冊專用Authorization token
    private static final String REGISTER_AUTH_CODE = "$2a$10$x42hx/UBe.PxFEoAk0RyuO0ImZ4h71hptmgvIF1sRZxA1HFqjJUAK";

    //忘記密碼專用Authorization token
    private static final String FORGET_AUTH_CODE = "$2a$10$yXAkhpwHtBm6Ws0dYohU5OzcjpkWW5QOCW7d6LOnVFPjDbnCjeciO";

    //驗證碼專用Authorization token
    private static final String VERIFICATION_CODE = "$2a$10$Ymh9oITzzZN3KZVDzajXZODBZqHXBrCexz1I3P5nhRL14cDDOZxH6";

    //發送驗證碼專用
    private static final String REQUEST_COMP_CODE = "$2a$10$jBSbzD.JToYeHV7jH8TWXeePdGcFd0bCyOSn4VhsGlqZ/KC61e/qK";

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

    //衛教專用
    public void buildEdu(String action, String body, String language, OnApiListener listener){
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request.Builder request = new Request.Builder();
        request.url(URL_EDUCATION + action);
        request.post(requestBody);
        request.addHeader(AUTHORIZATION, authToken);
        request.addHeader(SCEPTER, scepterToken);
        request.addHeader(DEFAULTLAN, language);
        buildRequest(request.build(), listener);
    }

    //重新要求驗證碼專用
    public void buildCompCode(String action, String body, String language, OnApiListener listener){
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request.Builder request = new Request.Builder();
        request.url(URL + action);
        request.post(requestBody);
        request.addHeader(AUTHORIZATION, REQUEST_COMP_CODE);
        request.addHeader(DEFAULTLAN, language);
        buildRequest(request.build(), listener);
    }

    //更改驗證方式專用
    public void buildVerification(String action, String body, String language, OnApiListener listener){
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request.Builder request = new Request.Builder();
        request.url(URL + action);
        request.post(requestBody);
        request.addHeader(AUTHORIZATION, authToken);
        request.addHeader(SCEPTER, scepterToken);
        request.addHeader(DEFAULTLAN, language);
        buildRequest(request.build(), listener);
    }

    //忘記密碼專用
    public void buildPassWD(String action, String body, OnApiListener listener){
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request.Builder request = new Request.Builder();
        request.url(URL + action);
        request.post(requestBody);
        request.addHeader(AUTHORIZATION, FORGET_AUTH_CODE);
        buildRequest(request.build(), listener);
    }

    //驗證碼比對專用
    public void buildInit(String action, String body, OnApiListener listener){
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request.Builder request = new Request.Builder();
        request.url(URL + action);
        request.post(requestBody);
        request.addHeader(AUTHORIZATION, VERIFICATION_CODE);
        buildRequest(request.build(), listener);
    }

    //註冊專用
    public void buildRegister(String action, String body, String language, OnApiListener listener){
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request.Builder request = new Request.Builder();
        request.url(URL + action);
        request.post(requestBody);
        request.addHeader(AUTHORIZATION, REGISTER_AUTH_CODE);
        request.addHeader(DEFAULTLAN, language);  //不能省略
        request.addHeader(SYSTEM,"6"); //後台要求要增加系統ID
        buildRequest(request.build(), listener);
    }

    //登入專用
    public void buildLogin(String action, String body, OnApiListener listener){
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request.Builder request = new Request.Builder();
        request.url(URL + action);
        request.post(requestBody);
        request.addHeader(AUTHORIZATION, "xxx");
        request.addHeader(SYSTEM,"6"); //後台要求要增加系統ID
        buildRequest(request.build(), listener);
    }

    //POST JSON
    public void buildPOST(String action, String body, OnApiListener listener){
        RequestBody requestBody = RequestBody.create(JSON, body);
        Request.Builder request = new Request.Builder();
        request.url(URL + action);
        request.post(requestBody);
        request.addHeader(AUTHORIZATION, authToken);
        request.addHeader(SCEPTER, scepterToken);
        buildRequest(request.build(), listener);
    }


    private void buildRequest(Request req, OnApiListener listener) {

        final Call call = buildClient().newCall(req);

        final Handler handler = new Handler(Looper.getMainLooper());

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
                Log.d(TAG, "onFailure: 後台api錯誤訊息:" + e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                int code = response.code();
                assert response.body() != null;

                //2021/08/06 解決資源洩漏的問題
//                if (!response.isSuccessful()){
//                    response.close();
//                }

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
                }finally {  //2021/08/06 解決資源洩漏的問題
                    response.close();
                }

                String message = app.getString(R.string.service_is_not_respond);
                final JSONObject finalJsonObject = jsonObject;
                final String finalMessage = message;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(code == 200){
                            listener.onSuccess(finalJsonObject);
                        }else {
                            listener.onFailure(finalMessage);
                        }
                        listener.onPostExecute();
                    }
                });
            }
        });
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onPreExecute();
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