package com.example.yhyhealthydemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yhyhealthydemo.dialog.AddTemperatureDialog;

public class TemperatureActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "TemperatureActivity";

    private Button supervise, remote;
    private Button addTemperatureUser, addRemoteUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        initView();

    }

    private void initView(){
        supervise = (Button) findViewById(R.id.bt_select_supervise);
        remote = (Button) findViewById(R.id.bt_select_remote);
        addTemperatureUser = (Button) findViewById(R.id.bt_add_temp);
        addRemoteUser = (Button) findViewById(R.id.bt_add_remote);

        supervise.setOnClickListener(this);
        remote.setOnClickListener(this);
        addTemperatureUser.setOnClickListener(this);
        addRemoteUser.setOnClickListener(this);

        supervise.setBackgroundResource(R.drawable.rectangle_button); //先顯示觀測Button
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.bt_select_supervise:
                supervise.setBackgroundResource(R.drawable.rectangle_button);
                remote.setBackgroundResource(R.drawable.relative_shape);
                addTemperatureUser.setVisibility(View.VISIBLE);
                addRemoteUser.setVisibility(View.GONE);
                break;
            case R.id.bt_select_remote:
                remote.setBackgroundResource(R.drawable.rectangle_button);
                supervise.setBackgroundResource(R.drawable.relative_shape);
                addTemperatureUser.setVisibility(View.GONE);
                addRemoteUser.setVisibility(View.VISIBLE);
                break;
            case R.id.bt_add_temp:
                dialogTemperature(); //新增觀測者彈跳視窗
                break;
            case R.id.bt_add_remote:
                break;
        }
    }

    private void dialogTemperature() {
        //自定義 一個TemperatureDialog繼承dialog
        AddTemperatureDialog addTemperatureDialog = new AddTemperatureDialog(this, R.style.Theme_AppCompat_Dialog, new AddTemperatureDialog.PriorityListener() {
            @Override
            public void setActivity(String username, String usergender, String userbirthday) {
                //data from dialog
                Log.d(TAG, "setActivity: " + username + "/" + usergender + "/" + userbirthday);

            }
        });
        addTemperatureDialog.setCancelable(false);
        addTemperatureDialog.show();
    }
}
