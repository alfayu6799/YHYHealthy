package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthy.adapter.CheckBoxAdapter;
import com.example.yhyhealthy.adapter.SwitchItemAdapter;
import com.example.yhyhealthy.adapter.VaccineCovidAdapter;
import com.example.yhyhealthy.datebase.SymptomData;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.SpacesItemDecoration;
import com.google.gson.JsonObject;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.SYMPTOM_ADD;
import static com.example.yhyhealthy.module.ApiProxy.SYMPTOM_LIST;
import static com.example.yhyhealthy.module.ApiProxy.covid19Select;

/****  ***********
 * 症狀
 * source data from Api (熱更新)
 * 配適器 switch - SwitchItemAdapter
 * 配適器 checkBox - CheckBoxAdapter
 * create 2021/04/07
 * *  *************/

public class SymptomActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "SymptomActivity";

    private ImageView back;
    private RecyclerView viewSymptomSW, viewSymptomCH;
    private Switch swVaccine;
    private LinearLayout viewVaccination;

    private RecyclerView viewCovid19;

    private int targetId;
    private int position;

    private Button update;

    //api
    private ApiProxy proxy;

    //
   private List<SymptomData.SwitchItemBean> switchItemBeanList = new ArrayList<>();
   private List<SymptomData.CheckBoxGroup> checkBoxGroupList = new ArrayList<>();
   private List<String> arrayList = new ArrayList<>();

   private TextView txtOther;
   private EditText edtOther;
   private TextView txtVaccineName;
   private EditText edtVaccineName;
   private TextView txtPainSite;
   private TextView txtSwellingRedness;
   private Switch   swSwellingRedness, swPainSite;
   private TextView txtVaccineOtherBrand;
   private EditText edtOtherBrand;
   private TextView txtCovidSelect;

   private String  vaccineName;                //疫苗名稱
   private String  painAtTheVaccinationSite;   //部位疼痛
   private boolean PaintSiteBoolean;           //疼痛(Y/N)
   private String  swellingRedness;            //部位紅腫
   private boolean swellingRednessBoolean;     //紅腫(Y/N)
   private String  vaccineNameOtherBrand;      //其他新冠疫苗
   private String  covid19Vaccine;             //新冠19疫苗
   private String  otherSymptom;                      //其他症狀

   private LinearLayout lyCovid19Vaccine;

    public SymptomActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //禁止旋轉

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

        initData();
    }

    private void initView() {
        update = findViewById(R.id.btnUpdate);
        back = findViewById(R.id.ivBackBlePage);

        swVaccine = findViewById(R.id.sw_vaccine);
        swVaccine.setText(R.string.close);
        swVaccine.setOnCheckedChangeListener(this);

        viewSymptomSW = findViewById(R.id.rvSymptomUp);
        viewSymptomCH = findViewById(R.id.rvSymptomDown);
        viewVaccination = findViewById(R.id.ly_Vaccination);
        viewCovid19 = findViewById(R.id.rvSymptom19);

        txtOther = findViewById(R.id.txt_key_other);
        edtOther = findViewById(R.id.edt_other);
        txtVaccineName = findViewById(R.id.txt_vaccine_name);
        edtVaccineName = findViewById(R.id.edt_vaccine_name);
        txtPainSite = findViewById(R.id.tv_vaccine_pain);
        txtSwellingRedness = findViewById(R.id.tv_vaccine_swell);
        swPainSite = findViewById(R.id.sw_pain_site);
        swPainSite.setText(getString(R.string.off));
        swSwellingRedness = findViewById(R.id.sw_swelling_redness);
        swSwellingRedness.setText(getString(R.string.off));
        txtVaccineOtherBrand = findViewById(R.id.txt_vaccine_other_Brand);
        edtOtherBrand = findViewById(R.id.edt_vaccine_other_Brand);
        txtCovidSelect = findViewById(R.id.tv_covid_select);

        lyCovid19Vaccine = findViewById(R.id.ly_covid19_vaccine);

        update.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    private void initData(){
        proxy.buildPOST(SYMPTOM_LIST, "", symptomListener);
    }

    private ApiProxy.OnApiListener symptomListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0) {
                            initSymptom(result);
                        }else if (errorCode == 23){ //token失效
                            Toasty.error(SymptomActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(SymptomActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(SymptomActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {

        }
    };

    //症狀初始化 2021/04/07
    private void initSymptom(JSONObject result) {
        Log.d(TAG, "initSymptom all: " + result.toString());
        Dictionary dictionary = getDictionary();
        try {
            JSONObject jsonObject = new JSONObject(result.toString());
            JSONArray array = jsonObject.getJSONArray("success");
            for (int i = 0; i < array.length(); i++){
                JSONObject newObject = array.getJSONObject(i);
                String key = newObject.getString("key");
                Object value = newObject.get("value");
                if (key.contains("vaccine")){ //疫苗選項
                    if (value instanceof String){
                        String[] vaccineStr = key.split(",");
                        if (vaccineStr[1].equals("vaccineName")){  //疫苗名稱
                            txtVaccineName.setText((CharSequence) dictionary.get(vaccineStr[1]));
                            txtVaccineName.setTextSize(TypedValue.COMPLEX_UNIT_DIP,22);
                            vaccineName = key;      //將取到的key值給予vaccineName(上傳後台需要此資訊) 2021/06/29
                        }else if (vaccineStr[1].equals("otherBrand")){  //新冠接種之其他疫苗
                            txtVaccineOtherBrand.setText((CharSequence) dictionary.get(vaccineStr[1]));
                            txtVaccineOtherBrand.setTextSize(TypedValue.COMPLEX_UNIT_DIP,22);
                            vaccineNameOtherBrand = key;  //將取到的key值給予vaccineNameOtherBrand(上傳後台需要此資訊) 2021/06/29
                        }
                    }else if (value instanceof JSONArray){  //新冠疫苗
                        JSONArray vaccineArray = newObject.getJSONArray("value");
                        String[] vaccineStr = key.split(",");
                        txtCovidSelect.setText((CharSequence) dictionary.get(vaccineStr[1]));
                        covid19Vaccine = key;
                        for(int k = 0 ; k < vaccineArray.length(); k++){
                            arrayList.add(vaccineArray.getString(k));
                        }
                    }else if (value instanceof Boolean){ //疼痛與紅腫
                        boolean vaccineBoolean = newObject.getBoolean("value");
                        String[] vaccineStr = key.split(",");

                        if (vaccineStr[1].equals("painAtTheVaccinationSite")){ //疼痛
                            txtPainSite.setText((CharSequence) dictionary.get(vaccineStr[1]));
                            txtPainSite.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
                            painAtTheVaccinationSite = key;  //將取到的key值給予painAtTheVaccinationSite(上傳後台需要此資訊) 2021/06/29
                            swPainSite.setChecked(vaccineBoolean);
                            swPainSite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                                    if (isChecked){
                                        swPainSite.setChecked(true);
                                        swPainSite.setText(getString(R.string.on));
                                        PaintSiteBoolean = true;
                                    }else {
                                        swPainSite.setChecked(false);
                                        swPainSite.setText(getString(R.string.off));
                                        PaintSiteBoolean = false;
                                    }
                                }
                            });
                        }else if (vaccineStr[1].equals("swellingRedness")){  //紅腫
                            txtSwellingRedness.setText((CharSequence) dictionary.get(vaccineStr[1]));
                            txtSwellingRedness.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
                            swellingRedness = key;   //將取到的key值給予swellingRedness(上傳後台需要此資訊) 2021/06/29
                            swSwellingRedness.setChecked(vaccineBoolean);
                            swSwellingRedness.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                                    if (isChecked){
                                        swSwellingRedness.setText(getString(R.string.on));
                                        swSwellingRedness.setChecked(true);
                                        swellingRednessBoolean = true;
                                    }else {
                                        swSwellingRedness.setText(getString(R.string.off));
                                        swSwellingRedness.setChecked(false);
                                        swellingRednessBoolean = false;
                                    }
                                }
                            });
                        }
                    }
                }else {
                    if (value instanceof Boolean) {
                        boolean booleanValue = newObject.getBoolean("value");
                        switchItemBeanList.add(new SymptomData.SwitchItemBean(key, booleanValue));
                    }else if (value instanceof JSONArray) {
                        JSONArray jsonValue = newObject.getJSONArray("value");
                        List<String> listData = new ArrayList<>();
                        for (int k = 0; k < jsonValue.length(); k++){
                            listData.add(jsonValue.getString(k));
                        }
                        checkBoxGroupList.add(new SymptomData.CheckBoxGroup(key,listData));
                    }else if (value instanceof String){  //如果value值是String (其他症狀)
                        otherSymptom = key;
                        txtOther.setText((CharSequence) dictionary.get(key));
                        txtOther.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20); //font's size = 22dp
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //covid19
        VaccineCovidAdapter vaccineCovidAdapter = new VaccineCovidAdapter(this,arrayList);
        viewCovid19.setAdapter(vaccineCovidAdapter);
        viewCovid19.setHasFixedSize(true);
        viewCovid19.setLayoutManager(new LinearLayoutManager(this));

        //解析出來的布林資料傳到Switch的Adapter
        SwitchItemAdapter switchItemAdapter = new SwitchItemAdapter(this, switchItemBeanList);
        viewSymptomSW.setAdapter(switchItemAdapter);
        viewSymptomSW.setHasFixedSize(true);
        viewSymptomSW.setLayoutManager(new LinearLayoutManager(this));
        viewSymptomSW.addItemDecoration(new SpacesItemDecoration(10));

        //解析出來的陣列資料傳到checkbox的Adapter
        CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(this, checkBoxGroupList);
        viewSymptomCH.setAdapter(checkBoxAdapter);
        viewSymptomCH.setHasFixedSize(true);
        viewSymptomCH.setLayoutManager(new LinearLayoutManager(this));
        viewSymptomCH.addItemDecoration(new SpacesItemDecoration(10));
    }

    private Dictionary getDictionary() {
        Dictionary dictionary = new Hashtable();
        dictionary.put("other",getString(R.string.symptom_other));
        dictionary.put("vaccineName", getString(R.string.vaccine_name));
        dictionary.put("painAtTheVaccinationSite", getString(R.string.pain_site));
        dictionary.put("swellingRedness", getString(R.string.swelling_redness));
        dictionary.put("otherBrand", getString(R.string.other_brand));
        dictionary.put("covid19Vaccine", getString(R.string.covid19));
        return dictionary;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivBackBlePage:
                finish();
                break;
            case R.id.btnUpdate:  //上傳到後台
                updateToApi();
                break;
        }
    }

    //更新上傳到後台
    @SuppressLint("NewApi")
    private void updateToApi(){
        DateTime dt1 = new DateTime();
        String SymptomRecordTime = dt1.toString("yyyy-MM-dd,HH:mm:ss");

        JSONArray array = new JSONArray();

        //疫苗名稱(自行輸入)
        JSONObject objectVaccine = new JSONObject();
        try {
            objectVaccine.put("key", vaccineName);
            objectVaccine.put("value", edtVaccineName.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        array.put(objectVaccine);

        //covid19
        //if (!covid19Select.isEmpty()) {
            JSONObject objectCovid = new JSONObject();
            try {
                objectCovid.put("key", covid19Vaccine);
                //objectCovid.put("value", new JSONArray(Arrays.asList(covid19Select))); //字串轉陣列
                objectCovid.put("value",covid19Select);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            array.put(objectCovid);
       // }

        //新冠疫苗其他類 2021/07/01 增加
        JSONObject objectOtherBrand = new JSONObject();
        try {
            objectOtherBrand.put("key", vaccineNameOtherBrand);
            objectOtherBrand.put("value", edtOtherBrand.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        array.put(objectOtherBrand);

        //部位疼痛  2021/06/29
        JSONObject objectPainSite = new JSONObject();
        try {
            objectPainSite.put("key", painAtTheVaccinationSite);
            objectPainSite.put("value", PaintSiteBoolean);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        array.put(objectPainSite);

        //部位紅腫 2021/06/29
        JSONObject objectSwellingRedness= new JSONObject();
        try {
            objectSwellingRedness.put("key", swellingRedness);
            objectSwellingRedness.put("value", swellingRednessBoolean);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        array.put(objectSwellingRedness);

        //switch
        for(int i=0; i < switchItemBeanList.size(); i++){
            JSONObject objectSwitch = new JSONObject();
            try {
                objectSwitch.put("key", switchItemBeanList.get(i).getKey());
                objectSwitch.put("value",switchItemBeanList.get(i).isValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            array.put(objectSwitch);
        }

        //String(other) 2021/07/01 增加
        JSONObject objectOther = new JSONObject();
        try {
            objectOther.put("key", otherSymptom);
            objectOther.put("value", edtOther.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        array.put(objectOther);

        //checkBox
        for(int j = 0; j < checkBoxGroupList.size(); j++){
            //String Delimiter =",";  //分隔符號
            JSONObject objectCheckBox = new JSONObject();
            try {
                objectCheckBox.put("key", checkBoxGroupList.get(j).getKey());
                //String resStr = String.join(Delimiter,checkBoxGroupList.get(j).getChecked()); //將字串與分隔符號連結
                objectCheckBox.put("value", new JSONArray(checkBoxGroupList.get(j).getChecked())); //字串陣列
                //objectCheckBox.put("value",resStr); //字串+分隔符號","

            } catch (JSONException e) {
                e.printStackTrace();
            }
            array.put(objectCheckBox);
        }

        JSONObject finalObject = new JSONObject();
        try {
            finalObject.put("targetId", targetId);
            finalObject.put("createDate", SymptomRecordTime);
            finalObject.put("symptoms", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "updateToApi: " + finalObject.toString());
        proxy.buildPOST(SYMPTOM_ADD, finalObject.toString(), addListener);
    }

    private ApiProxy.OnApiListener addListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0) {
                            Toasty.success(SymptomActivity.this, R.string.update_success, Toast.LENGTH_SHORT, true).show();
                            finish(); //返回上一頁
                        }else if (errorCode == 23){ //token失效
                            Toasty.error(SymptomActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(SymptomActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(SymptomActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {

        }
    };

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked){  //有選擇才會變成true
            swVaccine.setText(R.string.open);
            viewVaccination.setVisibility(View.VISIBLE);
        }else {
            swVaccine.setText(R.string.close);
            viewVaccination.setVisibility(View.GONE);
        }
    }
}