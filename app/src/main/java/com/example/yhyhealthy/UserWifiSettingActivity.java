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
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
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

import java.util.List;

import es.dmoral.toasty.Toasty;

public class UserWifiSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UserWifiSettingActivity";

    private ImageView back;
    private ImageView searchCJM410, searchWifi;
    private TextView  txtCJM410, txtWifi;
    private EditText  cjm410Password, wifiPassword;
    private Button    CJM410Connect, wifiConnect;

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

        searchWifi = findViewById(R.id.ivSearchWifi);           //wifi搜尋
        wifiPassword = findViewById(R.id.edtWifiPassword);

        searchCJM410 = findViewById(R.id.ivSearchCJM410);       //cjm410搜尋
        cjm410Password= findViewById(R.id.edtCJM410Password);  //cjm410的密碼
        //密碼先隱藏
        cjm410Password.setTransformationMethod(PasswordTransformationMethod.getInstance());

        searchCJM410.setOnClickListener(this);
        searchWifi.setOnClickListener(this);
        CJM410Connect.setOnClickListener(this);
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
        }
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
                if (strName.contains("410")){
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
}