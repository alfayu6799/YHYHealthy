package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TemperatureActivity extends AppCompatActivity implements View.OnClickListener {

    private Button supervise, remote;
    private Button addTemperatureUser;

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

        supervise.setOnClickListener(this);
        remote.setOnClickListener(this);

        supervise.setBackgroundResource(R.drawable.rectangle_button); //先顯示觀測Button
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.bt_select_supervise:
                supervise.setBackgroundResource(R.drawable.rectangle_button);
                remote.setBackgroundResource(R.drawable.relative_shape);
                addTemperatureUser.setText("新增使用者");
                break;
            case R.id.bt_select_remote:
                remote.setBackgroundResource(R.drawable.rectangle_button);
                supervise.setBackgroundResource(R.drawable.relative_shape);
                addTemperatureUser.setText("新增監看者");
                break;
        }
    }
}
