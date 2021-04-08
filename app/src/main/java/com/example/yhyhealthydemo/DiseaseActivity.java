package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.example.yhyhealthydemo.adapter.ColorAdapter;
import com.example.yhyhealthydemo.adapter.SecretionTypeAdapter;
import com.example.yhyhealthydemo.adapter.SymptomAdapter;
import com.example.yhyhealthydemo.adapter.TasteAdapter;
import com.example.yhyhealthydemo.module.ApiProxy;
import com.example.yhyhealthydemo.module.RecordColor;
import com.example.yhyhealthydemo.tools.MyGridView;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.yhyhealthydemo.module.ApiProxy.SYMPTOM_ADD;
import static com.example.yhyhealthydemo.module.ApiProxy.SYMPTOM_LIST;

public class DiseaseActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DiseaseActivity";

    private ImageView back;
    private Button    update;

    private int targetId;
    private int position;

    //api
    private ApiProxy proxy;

    private MyGridView sputumColor, sputumType, noseColor, noseTypes;

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
        noseTypes();
    }

    //鼻涕型態
    private void noseTypes() {
        String[] noseType = new String[]{ getString(R.string.normal), getString(R.string.liquid),
                getString(R.string.thick), getString(R.string.Sticky), getString(R.string.stink),getString(R.string.blood_water)};
        SymptomAdapter sAdapter = new SymptomAdapter(this);
        sAdapter.setData(noseType,0);
        noseTypes.setAdapter(sAdapter);
    }

    //鼻涕顏色
    private void noseColors() {
        String[] noseColors = new String[]{ getString(R.string.normal), getString(R.string.transparent), getString(R.string.milky),
                getString(R.string.greenish_yellow), getString(R.string.pink), getString(R.string.brown), getString(R.string.black)};
        SecretionTypeAdapter tAdapter = new SecretionTypeAdapter(this);
        tAdapter.setData(noseColors,0);
        noseColor.setAdapter(tAdapter);
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
        noseTypes = findViewById(R.id.gvNoseType);

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
        DateTime dt1 = new DateTime();
        String createTime = dt1.toString("yyyy-MM-dd,HH:mm:ss");

        //先收集所有的info
        collectInfo();

        JSONObject json = new JSONObject();
        try {
            json.put("targetId", targetId);
            json.put("createDate",createTime); //當前時間
//            json.put("symptoms",xxxx);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(SYMPTOM_ADD, json.toString(), addListener);
    }

    private ApiProxy.OnApiListener addListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {

        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {

        }
    };

    //先收集所有的info
    private void collectInfo() {

    }

}