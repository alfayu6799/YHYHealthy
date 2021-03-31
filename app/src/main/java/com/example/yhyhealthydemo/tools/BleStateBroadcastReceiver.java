package com.example.yhyhealthydemo.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;
import com.example.yhyhealthydemo.yhyBleService;
import es.dmoral.toasty.Toasty;

public class BleStateBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "BleStateBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }

        switch (action){
            case yhyBleService.ACTION_GATT_CONNECTED:
                Toasty.info(context, "藍芽連接中...", Toast.LENGTH_SHORT, true).show();
                break;
            case yhyBleService.ACTION_GATT_DISCONNECTED:
                Toasty.info(context, "藍芽已斷開並釋放資源", Toast.LENGTH_SHORT, true).show();
                break;
            case yhyBleService.ACTION_CONNECTING_FAIL:
                Toasty.info(context, "藍芽已斷開", Toast.LENGTH_SHORT, true).show();
                break;
            case yhyBleService.ACTION_NOTIFY_ON:  //03/30
                Toasty.info(context, "通知服務啟動成功", Toast.LENGTH_SHORT, true).show();
                break;
            case yhyBleService.ACTION_DATA_AVAILABLE:

                byte[] data = intent.getByteArrayExtra(yhyBleService.EXTRA_DATA);

                String[] str = ByteUtils.byteArrayToString(data).split(","); //以,分割
                String degreeStr = str[2];
                String batteryStr = str[3];
                double degree = Double.parseDouble(degreeStr)/100;
                double battery = Double.parseDouble(batteryStr);

                break;

            default:
                break;
        }
    }
}
