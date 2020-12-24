package com.example.yhyhealthydemo;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.yhyhealthydemo.tools.ApiUtil;

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
import okhttp3.logging.HttpLoggingInterceptor;

public class MeasureFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MeasureFragment";

    private View view;

    private Button ovulation, temperature, pregnancy,monitor;

    private ApiUtil apiUtil;

    private boolean isMenstrualExists = false;

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

        apiUtil = new ApiUtil();

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

    /***  後台Api要求經期是否有設定 (POST)
    *    http://192.168.1.144:8080/allAiniita/aplus/MenstrualExists
    ***/
    private void checkMenstrualExists() {

        new Thread() {
            @Override
            public void run() {
                MediaType JSON = MediaType.parse("application/json;charset=utf-8");
                JSONObject json = new JSONObject();
                try {
                    json.put("type", "3");
                    json.put("userId", "H5E3q5MjA=");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 建立OkHttpClient
                OkHttpClient okHttpClient = new OkHttpClient();

                RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));

                // 建立Request，設置連線資訊
                Request request = new Request.Builder()
                        .url("http://192.168.1.144:8080/allAiniita/aplus/MenstrualExists")
                        .addHeader("Authorization","xxx")
                        .post(requestBody)
                        .build();

                // 執行Call連線到網址
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // 連線失敗
                        Log.i("onFailure", e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // 連線成功，自response取得連線結果
                        String string = response.body().string();  //字串
                        
                        isMenstrualExists = true;
                        Log.d(TAG, "onResponse: 連線結果 : " + string);
                    }
                });
            }
        }.start();
    }

}
