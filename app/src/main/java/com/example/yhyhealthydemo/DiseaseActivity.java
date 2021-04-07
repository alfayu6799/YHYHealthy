package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.example.yhyhealthydemo.adapter.ColorAdapter;
import com.example.yhyhealthydemo.adapter.TasteAdapter;
import com.example.yhyhealthydemo.module.ApiProxy;
import com.example.yhyhealthydemo.module.RecordColor;
import com.example.yhyhealthydemo.tools.MyGridView;

import static com.example.yhyhealthydemo.module.ApiProxy.SYMPTOM_LIST;

public class DiseaseActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DiseaseActivity";

    private ImageView back;
    private Button    update;

    private int targetId;
    private int position;

    //api
    private ApiProxy proxy;

    private MyGridView sputumColor, sputumType, noseColor, noseType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease);

        //休眠禁止
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //接受來自TemperatureActivity的資料
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            targetId = bundle.getInt("targetId");  //使用全域變數
            position = bundle.getInt("position");  //使用全域變數
        }

        proxy = ApiProxy.getInstance();

        initView();

        initLayout();
    }

    private void initLayout() {
        //痰的顏色
        sputumColors();

        //痰的型態
        sputumTypes();

        //鼻涕顏色
        noseColors();

        //鼻涕型態

    }

    private void noseColors() {
    }

    //痰的型態
    private void sputumTypes() {
        String[] type = new String[]{ getString(R.string.normal), getString(R.string.foamy),
                getString(R.string.bloodshot), getString(R.string.slimy)};
        TasteAdapter aAdapter = new TasteAdapter(this);
        aAdapter.setData(type,0);
        sputumType.setAdapter(aAdapter);
    }

    //痰的顏色
    private void sputumColors() {
        String[] colors = new String[]{ getString(R.string.normal), getString(R.string.white), getString(R.string.yellow),
                getString(R.string.green), getString(R.string.rusty), getString(R.string.gray_black)};

        ColorAdapter cAdapter = new ColorAdapter(this);
        cAdapter.setData(colors, 0);
        sputumColor.setAdapter(cAdapter);
    }

    private void initView() {
        update = findViewById(R.id.btnUpdate);
        back = findViewById(R.id.ivBackBlePage);

        sputumColor = findViewById(R.id.gvSputumColor);
        sputumType = findViewById(R.id.gvSputumType);
        noseColor = findViewById(R.id.gvNoseColor);

        back.setOnClickListener(this);
        update.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackBlePage:
                finish();
                break;
            case R.id.btnUpdate:
                updateToApi(targetId);
                break;
        }
    }

    //上傳到後台
    private void updateToApi(int targetId) {
    }


}