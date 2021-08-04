package com.example.yhyhealthy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.thanosfisherman.wifiutils.WifiUtils;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener;
import com.thanosfisherman.wifiutils.wifiRemove.RemoveErrorCode;
import com.thanosfisherman.wifiutils.wifiRemove.RemoveSuccessListener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.dmoral.toasty.Toasty;

/**** ***********
 * 無線基地台配置設計
 * create 2021/08/04
 * remoteServ 要隨上架地點進行手動變動
 * 中國主機: "39.108.85.178"
 * 國際主機: "47.74.247.30"
 * ***********/

public class UserWifiSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UserWifiSettingActivity";

    private ImageView back;
    private ImageView searchCJM410, searchWifi;
    private TextView  txtCJM410, txtWifi;
    private EditText  cjm410Password, wifiPassword;
    private Button    CJM410Connect, wifiConnect, wifiCancel,clearConfig;

    private ProgressDialog progressDialog; //搜尋wifi進度

    public static final int REQUEST_ENABLE_FINE_LOCATION = 1256;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_wifi_setting);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //禁止旋轉

        //不讓虛擬鍵盤蓋文
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        //需要Local GPS權限
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ENABLE_FINE_LOCATION);

        //init progressDialog
        progressDialog = new ProgressDialog(UserWifiSettingActivity.this);
        
        initView();
    }

    private void initView() {
        back = findViewById(R.id.ivBackWifiSetting);             //返回

        txtCJM410 = findViewById(R.id.tvCJM410);
        txtWifi = findViewById(R.id.tvHomeWifi);

        CJM410Connect =  findViewById(R.id.btnConnectCMJ410);
        wifiConnect = findViewById(R.id.btnSaveWifiConfig);

        searchWifi = findViewById(R.id.ivSearchWifi);           //wifi搜尋
        wifiPassword = findViewById(R.id.edtWifiPassword);      ///wifi密碼
        //密碼先隱藏
        wifiPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        searchCJM410 = findViewById(R.id.ivSearchCJM410);       //cjm410搜尋
        cjm410Password= findViewById(R.id.edtCJM410Password);  //cjm410的密碼
        //密碼先隱藏
        cjm410Password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        
        wifiCancel = findViewById(R.id.btnCancelWifiConfig);
        clearConfig = findViewById(R.id.btnClearConfig);
        clearConfig.setVisibility(View.INVISIBLE);  //清除定按鈕隱藏 2021/08/04

        searchCJM410.setOnClickListener(this);
        searchWifi.setOnClickListener(this);
        CJM410Connect.setOnClickListener(this);
        wifiConnect.setOnClickListener(this);
        wifiCancel.setOnClickListener(this);
        clearConfig.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackWifiSetting: //返回上一頁
                finish();
                break;
            case R.id.ivSearchCJM410:  //搜尋cjm410裝置
            case R.id.ivSearchWifi:    //搜尋wifi裝置
                searchWifiDevice();
                break;
            case R.id.btnConnectCMJ410: //連接CJM410
                connectCJM410();
                break;
            case R.id.btnSaveWifiConfig: //設定寫入CJM410
                saveConfig();
                break;
            case R.id.btnCancelWifiConfig:  //取消寫入配置
                cancelWifiConfig();
                break;
            case R.id.btnClearConfig:      //清除所有的設定
                break;
        }
    }

    //取消寫入配置 2021/08/04
    private void cancelWifiConfig() {
        //清除cjm410 password
        cjm410Password.setText("");

        //清除cjm410 ssid
        txtCJM410.setText("");

        //清除客戶wifi password
        wifiPassword.setText("");

        //清除客戶wifi ssid
        txtWifi.setText("");

        //解除與cjm410之間的連線
        WifiUtils.withContext(getApplicationContext()).remove(txtCJM410.getText().toString(), new RemoveSuccessListener() {
            @Override
            public void success() {
                toasty(getString(R.string.remove_succeed));
            }

            @Override
            public void failed(@NonNull RemoveErrorCode errorCode) {
                errorToast(getString(R.string.remove_failed) + errorCode.toString());
            }
        });
    }

    //將設定寫入到CJM410
    private void saveConfig() {
        if (TextUtils.isEmpty(txtWifi.getText().toString()) || TextUtils.isEmpty(txtWifi.getText().toString())){
            toasty(getString(R.string.ssid_is_empty));
            return;
        }

        //將config寫入cjm410  2021/08/03
        UPD_Client upd_client = new UPD_Client();
        upd_client.execute();
    }

    //搜尋wifi裝置
    private void searchWifiDevice() {
        progressDialog.setMessage("Search...");
        progressDialog.show();

        WifiUtils.withContext(getApplicationContext()).scanWifi(this::getScanResult).start();
    }

    private void getScanResult(List<ScanResult> scanResults) {
        if (scanResults.isEmpty()){
            toasty(getString(R.string.scan_is_empty));
            return;
        }
        //close search progress Dialog
        progressDialog.dismiss();

        //將結果顯示在ListView
        showJCM410Dialog(scanResults);
    }

    //將Search wifi devices 結果顯示在ListView
    private void showJCM410Dialog(List<ScanResult> scanResults) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //標題背景
        TextView textView = new TextView(this);
        textView.setText(getString(R.string.select_wifi_devices));
        textView.setPadding(20, 30, 20, 30);
        textView.setTextSize(20F);
        textView.setBackgroundResource(R.color.colorPrimaryDark);
        textView.setTextColor(Color.WHITE);
        builder.setCustomTitle(textView);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);

        ScanResult result;
        for (int i = 0; i < scanResults.size(); i++){
            result = scanResults.get(i);
            String ssid = result.SSID;
            arrayAdapter.add(ssid);
        }

        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                String strName = arrayAdapter.getItem(which);
                if (strName.contains("H")){
                    txtCJM410.setText(strName);
                }else {
                    txtWifi.setText(strName);
                }

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //連接CJM410
    private void connectCJM410() {
        WifiUtils.withContext(getApplicationContext())
                .connectWith(txtCJM410.getText().toString(), cjm410Password.getText().toString())
                .setTimeout(4000)
                .onConnectionResult(new ConnectionSuccessListener() {
                    @Override
                    public void success() {
                        toasty(getString(R.string.connect_succeed)); //連接成功
                        //顯示下一個步驟以及關閉此連接按鈕功能 2021/08/03
                    }

                    @Override
                    public void failed(@NonNull ConnectionErrorCode errorCode) {
                        if (errorCode.toString().equals("DID_NOT_FIND_NETWORK_BY_SCANNING")){
                            errorToast(getString(R.string.cjm410_cant_find) + txtCJM410.getText().toString());
                        }else {
                            errorToast(getString(R.string.connect_fail) + errorCode.toString());
                        }
                    }
                }).start();
    }

    //以下為廠商給予的simple code 改寫
    private byte[] wmcpDataSetReq(int cmdcode, String params) {
        byte[] wmcpDataBuffer = new byte[params.length() + 3];
        wmcpDataBuffer[0] = 0x01;
        wmcpDataBuffer[1] = (byte)cmdcode;
        wmcpDataBuffer[2] = 0x02;

        if (!params.equals("")) {
            int i, j;
            byte[] byteParams = params.getBytes();

            j = 3;
            for (i = 0; i < byteParams.length; i++){
                wmcpDataBuffer[j++] = byteParams[i];
            }
        }

        return wmcpDataBuffer;
    }

    //Toast顯示位置及內容(正確)
    private void toasty(String messages){
        Toast toast = Toasty.info(this, messages, Toast.LENGTH_SHORT, true);
        toast.setGravity(Gravity.CENTER, 0, 0); //正中央
        toast.show();
    }

    //Toast顯示位置及內容(錯誤)
    private void errorToast(String messages){
        Toast toast = Toasty.error(this, messages, Toast.LENGTH_SHORT, true);
        toast.setGravity(Gravity.CENTER, 0, 0); //正中央
        toast.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ENABLE_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted!
                } else {
                    errorToast(getString(R.string.permiss_info));
                }
        }
    }

    //
    public class UPD_Client extends AsyncTask<String, String, String>{

        InetAddress servIpAddr = null;
        String netProto = "TCP";
        String remoteServ = "47.74.247.30"; //國際
        //String remoteServ = "39.108.85.178"; //中國
        String port = "10007";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                servIpAddr = InetAddress.getByName("192.168.1.10");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            DatagramSocket clientSocket = null;
            byte[] rxData = new byte[512];
            DatagramPacket dp = new DatagramPacket(rxData, rxData.length);

            boolean setOK = false;

            ArrayList<byte[]> wmcpDataArray = new ArrayList<>();

            if (servIpAddr == null)
                return "CJM410 IP ERROR";

            wmcpDataArray.add(wmcpDataSetReq(0x11, "0"));      //Client mode
            wmcpDataArray.add(wmcpDataSetReq(0x14, txtWifi.getText().toString()));
            if (TextUtils.isEmpty(wifiPassword.getText().toString())) { //沒有密碼
                wmcpDataArray.add(wmcpDataSetReq(0x15, "NONE"));  //WSTASEC
            } else {
                wmcpDataArray.add(wmcpDataSetReq(0x15, "WPA2"));  //password加密型態
                String cypher = "AES";
                String psk = cypher + "," + wifiPassword.getText().toString();
                wmcpDataArray.add(wmcpDataSetReq(0x16, psk));            //password
            }
            wmcpDataArray.add(wmcpDataSetReq(0x23, netProto));       //TCP

            if (TextUtils.isEmpty(remoteServ))
                return "server is empty";

            String client = remoteServ + "," + port;
            wmcpDataArray.add(wmcpDataSetReq(0x24, client));         //將遠端server寫入
            wmcpDataArray.add(wmcpDataSetReq(0x32, ""));     //Apply(執行)

            try {
                clientSocket = new DatagramSocket();
                for (int i = 0; i < wmcpDataArray.size(); i++) {
                    byte[] wmcpDataPayload = wmcpDataArray.get(i);
                    byte cmdcode = wmcpDataPayload[1];

                    DatagramPacket packet = new DatagramPacket(wmcpDataPayload, wmcpDataPayload.length, servIpAddr, 60002);
                    clientSocket.send(packet);
                    int retry = 0;
                    while (retry < 3) {
                        try {
                            clientSocket.setSoTimeout(2000);
                            clientSocket.receive(dp);

                            byte[] data = dp.getData();
                            if (data[0] == 0x02 &&
                                    data[1] == cmdcode &&
                                    data[2] == 0x02) {
                                // handle rx data
                                byte[] resp = Arrays.copyOfRange(data, 3, dp.getLength());
                                String strResp = new String(resp);
                                String msg = String.format("Set cmdcode %x response=", cmdcode);
                                Log.i("CJM", msg + strResp);

                                if (strResp.equals("ok")) {
                                    setOK = true;
                                    break;
                                } else {
                                    setOK = false;
                                    break;
                                }
                            }
                        } catch (SocketTimeoutException e) {
                            retry += 1;
                            clientSocket.send(packet);
                            String str = String.format("Retry device configuration request : %d", retry);
                            Log.i("CJM", str);
                        }
                    }
                    if (!setOK)
                        break;
                }

            } catch (SocketException e) {
                if (clientSocket != null) {
                    clientSocket.close();
                }
                return "Socket Exception";
            } catch (IOException e) {
                if (clientSocket != null) {
                    clientSocket.close();
                }
                return "IO Exception";
            }

            if (clientSocket != null) {
                clientSocket.close();
            }
            return "OK";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("OK")) {
                toasty(getString(R.string.set_succeed));
                //設定成功後顯示清除config的按紐
                //clearConfig.setVisibility(View.VISIBLE);
            }else if (result.equals("server is empty")){
                errorToast(getString(R.string.server_empty));
            } else {
                errorToast(getString(R.string.set_failed));
            }
        }
    }
}