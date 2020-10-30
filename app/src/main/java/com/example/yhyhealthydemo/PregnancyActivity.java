package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PregnancyActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = PregnancyActivity.class.getSimpleName();

    private Button prenatalRecord, pregnancyRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregnancy);

        initView();
    }

    private void initView() {
        prenatalRecord = (Button) findViewById(R.id.bt_preg_prenatal);
        pregnancyRecord = (Button) findViewById(R.id.bt_preg_pregnancy);

        prenatalRecord.setOnClickListener(this);
        pregnancyRecord.setOnClickListener(this);

        prenatalRecord.setBackgroundResource(R.drawable.rectangle_button);  //先顯示產檢紀錄Button
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.bt_preg_prenatal:
                prenatalRecord.setBackgroundResource(R.drawable.rectangle_button);
                pregnancyRecord.setBackgroundResource(R.drawable.relative_shape);
                break;
            case R.id.bt_preg_pregnancy:
                prenatalRecord.setBackgroundResource(R.drawable.relative_shape);
                pregnancyRecord.setBackgroundResource(R.drawable.rectangle_button);
                break;
        }
    }

}
