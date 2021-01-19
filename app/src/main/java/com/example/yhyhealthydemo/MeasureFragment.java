package com.example.yhyhealthydemo;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.yhyhealthydemo.module.ApiProxy;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.yhyhealthydemo.module.ApiProxy.MENSTRUAL_EXISTS;

/****
 * 首頁
 * 四個功能 : 排卵紀錄, 藍芽體溫 , 懷孕紀錄 , 呼吸監控
 * 三個icon : 公告,購物,教學
 * */
public class MeasureFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MeasureFragment";

    private View view;

    private Button ovulation, temperature, pregnancy, monitor;

    private boolean isMenstrualExists = false;

    //api
    private ApiProxy proxy;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;

        view = inflater.inflate(R.layout.fragment_measure, container, false);

        ovulation = view.findViewById(R.id.bt_ovulation);
        temperature = view.findViewById(R.id.bt_temperature);
        pregnancy = view.findViewById(R.id.bt_pregnancy);
//        pregnancy.setVisibility(View.INVISIBLE);
        monitor = view.findViewById(R.id.bt_monitor);
//        monitor.setVisibility(View.INVISIBLE);

        proxy = ApiProxy.getInstance();  //api實例化

        checkMenstrualExists();  //經期是否有設定

        ovulation.setOnClickListener(this);
        temperature.setOnClickListener(this);
        pregnancy.setOnClickListener(this);
        monitor.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.bt_ovulation:
                if(isMenstrualExists) {
                    startActivity(new Intent(getActivity(), OvulationActivity.class));
                }else {
                    startActivity(new Intent(getActivity(), SystemUserActivity.class));
                }
                break;
            case R.id.bt_temperature:
                Intent intent_t = new Intent(getActivity(), TemperatureActivity.class);
                startActivity(intent_t);
                break;
            case R.id.bt_pregnancy:
                Intent intent_p = new Intent(getActivity(), PregnancyActivity.class);
                startActivity(intent_p);
                break;
            case R.id.bt_monitor:

                break;
        }
    }

    //經期是否有設定
    private void checkMenstrualExists() {

        JSONObject json = new JSONObject();
        try {
            json.put("type", "3");
            json.put("userId", "H5E3q5MjA=");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(MENSTRUAL_EXISTS, json.toString(), exitsListener);
    }

    private ApiProxy.OnApiListener exitsListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            Log.d(TAG, "isMenstrualExists: success!!");
            isMenstrualExists = true;
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "isMenstrualExists failure!!");
        }

        @Override
        public void onPostExecute() {

        }
    };

}
