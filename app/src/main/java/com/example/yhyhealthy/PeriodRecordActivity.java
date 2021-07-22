package com.example.yhyhealthy;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.yhyhealthy.adapter.BluetoothLeAdapter;
import com.example.yhyhealthy.adapter.ColorAdapter;
import com.example.yhyhealthy.adapter.SecretionTypeAdapter;
import com.example.yhyhealthy.adapter.SymptomAdapter;
import com.example.yhyhealthy.adapter.TasteAdapter;
import com.example.yhyhealthy.data.ScannedData;
import com.example.yhyhealthy.datebase.ChangeRecord;
import com.example.yhyhealthy.datebase.MenstruationRecord;
import com.example.yhyhealthy.datebase.PhotoData;
import com.example.yhyhealthy.module.RecordSymptom;
import com.example.yhyhealthy.module.RecordTaste;
import com.example.yhyhealthy.module.RecordType;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.module.yhyBleService;
import com.example.yhyhealthy.tools.ByteUtils;
import com.example.yhyhealthy.tools.ImageUtils;
import com.example.yhyhealthy.tools.MyGridView;
import com.example.yhyhealthy.module.RecordColor;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import es.dmoral.toasty.Toasty;
import pl.droidsonroids.gif.GifImageView;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static com.example.yhyhealthy.module.ApiProxy.IMAGE_DETECTION;
import static com.example.yhyhealthy.module.ApiProxy.RECORD_INFO;
import static com.example.yhyhealthy.module.ApiProxy.RECORD_UPDATE;

/*********
 * 排卵紀錄資訊
 * 照相 CameraActivity
 * 藍芽 yhyBleService
 * 權限繼承DeviceBaseActivity
 **********/
public class PeriodRecordActivity extends DeviceBaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = PeriodRecordActivity.class.getSimpleName();

    private TextView  textRecordDate;
    private Button    takePhoto, startMeasure, saveSetting, photoIdentify;
    private ImageView searchBLE;
    private ImageView photoShow;
    private TextView  textBleStatus;
    private TextView  textAnalysis;

    private AlertDialog alertDialog;

    private String photoPath = null;
    private String strDay;
    private String base64Str;

    //藍芽
    private yhyBleService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BroadcastReceiver mBleReceiver;
    private BluetoothLeAdapter mDeviceListAdapter;
    private boolean isScanning = false;
    private ArrayList<ScannedData> findDevice = new ArrayList<>();
    private Handler mHandler;
    private String deviceName ="";   //藍芽裝置名稱
    private String deviceAddress = ""; //藍芽MAC

    //使用者自行輸入區
    MyGridView gridViewColor, gridViewTaste, gridViewType, gridViewSymptom;
    private EditText   editWeight;    //體重
    private TextView   textBodyTemp;  //體溫

    private Switch bleeding, breastPain, intercourse;

    //Api
    private ApiProxy proxy;
    private MenstruationRecord record;
    private ChangeRecord changeRecord;
    private PhotoData photoData;

    //顏色,氣味,症狀,型態 Adapter
    private SymptomAdapter sAdapter;
    private SecretionTypeAdapter tAdapter;
    private TasteAdapter aAdapter;
    private ColorAdapter cAdapter;
    private String[] types;  //型態
    private String[] symptom;  //症狀
    private String[] taste;  //氣味
//    private String[] colors; //顏色

    //進度
    private ProgressDialog progressDialog;
    //背景動畫
    private GifImageView gifImageView;

    //
    private static final int CAMERA_RECORD = 2;
    private String mPath = "";

    //量測進度
    private ProgressBar measureProgress;
    private MyCountDownTimer myCountDownTimer;
    private LinearLayout linearLayout;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_period);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //禁止旋轉

        //init
        initView();

        //取的來自OvulationActivity的資料
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            strDay = bundle.getString("DAY");
            if (strDay == null){  //避免從相機拍照完回來後日期為空 2021/02/18
                DateTime day = new DateTime(new Date());
                String today = day.toString("yyyy-MM-dd");
                strDay = today;
            }else {
                textRecordDate.setText(strDay);
            }
            setRecordInfo(strDay);  //以使用者點擊的日期為key
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        textRecordDate = findViewById(R.id.tvRecordDate);
        takePhoto = findViewById(R.id.btnPhoto);
        photoIdentify = findViewById(R.id.btnPhotoIdentify);    //辨識Onclick
        searchBLE = findViewById(R.id.ivBLESearch);
        startMeasure = findViewById(R.id.btnStartMeasure);      //開始量測onclick
        saveSetting = findViewById(R.id.btnSaveSetting);
        photoShow = findViewById(R.id.ivPhoto);                 //顯示照片
        textBleStatus = findViewById(R.id.tvBLEConnectStatus);  //顯示藍芽連線狀態
        textBodyTemp = findViewById(R.id.tvBodyTemp);           //顯示體溫
        textAnalysis = findViewById(R.id.tvAnalysis);           //顯示分析結果

        //動畫background
        gifImageView = findViewById(R.id.game_gif);
        gifImageView.setBackgroundResource(R.mipmap.yhy_new_background);

        linearLayout = findViewById(R.id.ly_progressBar);       //量測進度Layout
        measureProgress = findViewById(R.id.progressBar);       //量測進度條
        measureProgress.setProgressTintList(ColorStateList.valueOf(Color.BLUE)); //量測進度條顏色

        bleeding = findViewById(R.id.swBleeding);               //出血
        breastPain = findViewById(R.id.swBreastPain);           //脹痛
        intercourse = findViewById(R.id.swIntercourse);         //行房

        //體重自行輸入
        editWeight = findViewById(R.id.edtWeight);
        editWeight.setInputType(InputType.TYPE_NULL);           //hide keyboard
        editWeight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                editWeight.setInputType(InputType.TYPE_CLASS_NUMBER);
                editWeight.onTouchEvent(event);
               return true;
            }
        });

        gridViewColor = findViewById(R.id.gvColor);
        gridViewTaste = findViewById(R.id.gvTaste);
        gridViewType = findViewById(R.id.gvType);
        gridViewSymptom = findViewById(R.id.gvSymptom);

        takePhoto.setOnClickListener(this);
        searchBLE.setOnClickListener(this);
        startMeasure.setOnClickListener(this);
        saveSetting.setOnClickListener(this);
        startMeasure.setOnClickListener(this);
        photoIdentify.setOnClickListener(this);  //2021/02/19

        bleeding.setOnCheckedChangeListener(this);
        breastPain.setOnCheckedChangeListener(this);
        intercourse.setOnCheckedChangeListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnPhoto:      //拍照OnClick
                checkIsToday();
                break;
            case R.id.ivBLESearch:   //藍芽搜尋
                String todayStr = String.valueOf(LocalDate.now());
                if (strDay.equals(todayStr)){    //限當日可以量體溫
                    openBleFunction();
                }else {
                    Toasty.info(PeriodRecordActivity.this,getString(R.string.ble_only_today), Toast.LENGTH_SHORT, true).show();
                }
                break;
            case R.id.btnStartMeasure: //開始量測
                startCountDownTime();
                break;
            case R.id.btnSaveSetting: //將資料收集完後上傳至後台Onclick
                checkBeforeUpdate();  //上傳至後台先檢查資訊是否齊全fxn
                break;
            case R.id.btnPhotoIdentify: //將照片傳給後台去辨識
                upPhotoToApi();
                break;
        }
    }

    //總計3分鐘,每10秒執行一次onTick方法
    private void startCountDownTime(){
        myCountDownTimer = new MyCountDownTimer(180000, 1000);
        myCountDownTimer.start();  //計時開始
        startMeasure.setVisibility(View.INVISIBLE); //量測按鈕隱藏
        linearLayout.setVisibility(View.VISIBLE);   //進度條顯示
        sendCommand(deviceAddress);
    }

    //2021/02/19 照片辨識
    private void upPhotoToApi() {
        //先將照片編碼成base64
        base64Str = ImageUtils.imageToBase64(mPath);         //使用內建原生相機
//        base64Str = ImageUtils.imageToBase64(photoPath);   //使用客制相機

        //今天日期
        DateTime today = new DateTime();
        String todayStr = today.toString("yyyy-MM-dd");

        JSONObject json = new JSONObject();
        try {
            json.put("testDate", todayStr);
            json.put("img", base64Str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //執行照片辨識api
        proxy.buildPOST(IMAGE_DETECTION, json.toString(), photoIdentifyListener);
    }

    private ApiProxy.OnApiListener photoIdentifyListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(PeriodRecordActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
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
                            parserPhotoId(result);  //2021/02/19
                        }else if (errorCode == 23) { //token失效
                            Toasty.error(PeriodRecordActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(PeriodRecordActivity.this, LoginActivity.class));
                            finish();
                        }else if (errorCode == 31 ){
                            Toasty.error(PeriodRecordActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(PeriodRecordActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(PeriodRecordActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT,true).show();
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
            progressDialog.dismiss();
        }
    };

    //照片辨識後後台回來的訊息 2021/02/19
    private void parserPhotoId(JSONObject result) {
        photoData = PhotoData.newInstance(result.toString());
        String paramName = photoData.getSuccess().getParamName();
        String param = photoData.getSuccess().getParam();
        changeRecord.getMeasure().setParam(param); //後台需要這個資料
        if(!paramName.isEmpty()) {
            if (paramName.equals("FollicularORLutealPhase")) {
                textAnalysis.setText(R.string.in_low_cell);  //低濾泡
                //字形大小需要中文與英文不同
                textAnalysis.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_record_FollicularORLutealPhase));
            } else if (paramName.equals("Unrecognizable")) {
                textAnalysis.setText(R.string.unknow);      //無法辨識
                textAnalysis.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_unknow));
            } else if (paramName.equals("General")) {
                textAnalysis.setText(R.string.non_period);  //非排卵期
            } else if (paramName.equals("HighFollicularORLutealPhase")) {
                textAnalysis.setText(R.string.in_high_cell); //高濾泡
                textAnalysis.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_record_HighFollicularORLutealPhase));
            } else if (paramName.equals("Ovulation")) {
                textAnalysis.setText(R.string.in_period);    //排卵期
            }
        }
    }


    //上傳至後台先檢查資訊是否齊全
    private void checkBeforeUpdate() {
        //體重
        if(!TextUtils.isEmpty(editWeight.getText().toString())){
            changeRecord.getMeasure().setWeight(Double.parseDouble(editWeight.getText().toString()));
        }

        //體溫
        changeRecord.getMeasure().setTemperature(Double.parseDouble(textBodyTemp.getText().toString()));
        UpdateToApi();
    }

    //拍照辨識需檢查是否當日 2021/02/18
    private void checkIsToday() {
        DateTime today = new DateTime(new Date());
        String todayStr = today.toString("yyyy-MM-dd");
        if (strDay.equals(todayStr)){    //限當日可以拍照&體溫
            openCamera();
        }else {
            Toasty.info(PeriodRecordActivity.this,getString(R.string.camera_only_today), Toast.LENGTH_SHORT, true).show();
        }
    }

    //開啟藍芽相關功能
    private void openBleFunction() {
        if(ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initBle();  //初始化藍芽
        }else {
            requestPermission();
        }
    }

    //開啟相機功能
    private void openCamera() {
        if(ActivityCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  //呼叫原生相機
            File imageFile = getImageFile();
            if (imageFile == null) return;
            Uri imageUri = FileProvider.getUriForFile(this,"com.yhihc.group.yhyhealthy.fileprovider", imageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, Activity.DEFAULT_KEYS_DIALER);
            /* 使用客制相機(自定義Camera功能)
            Intent camera = new Intent(PeriodRecordActivity.this, CameraActivity.class);
            startActivityForResult(camera, CAMERA_RECORD);
            */
        }else {
            requestPermission();
        }
    }

    //取得相片檔案的URL
    private File getImageFile(){
        String time = new SimpleDateFormat("yyMMdd").format(new Date());
        String fileName = time + "_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile = File.createTempFile(fileName, ".jpg", dir);
            mPath = imageFile.getAbsolutePath();
            return imageFile;
        } catch (IOException e) {
            return null;
        }

    }

    //上傳至後台儲存 2021/02/19 leona
    private void UpdateToApi() {
        //需要日期傳到後台去做更新
        changeRecord.setTestDate(strDay);

        proxy.buildPOST(RECORD_UPDATE, changeRecord.toJSONString(), changeRecordListener);
    }

    private ApiProxy.OnApiListener changeRecordListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(PeriodRecordActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            Toasty.success(PeriodRecordActivity.this,getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                            setResult(RESULT_OK);
                            finish(); //回到前一頁
                        }else if (errorCode == 23) { //token失效
                            Toasty.error(PeriodRecordActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(PeriodRecordActivity.this, LoginActivity.class));
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(PeriodRecordActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(PeriodRecordActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(PeriodRecordActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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
            progressDialog.dismiss();
        }
    };

    //Switch button listener  2021/01/07 leona
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
        switch (compoundButton.getId()){
            case R.id.swBleeding:   //出血
                if (isCheck){
                    changeRecord.getStatus().setBleeding(true);
                }else {
                    changeRecord.getStatus().setBleeding(false);
                }
                break;
            case R.id.swBreastPain: //脹痛
                if(isCheck){
                    changeRecord.getStatus().setBreastPain(true);
                }else {
                    changeRecord.getStatus().setBreastPain(false);
                }
                break;
            case R.id.swIntercourse: //行房
                if (isCheck){
                    changeRecord.getStatus().setIntercourse(true);
                }else {
                    changeRecord.getStatus().setIntercourse(false);
                }
                break;
        }
    }

    //向後台要求資料 2021/01/08 leona
    private void setRecordInfo(String selectDay) {
        proxy = ApiProxy.getInstance();      //api實體化
        changeRecord = new ChangeRecord();   //實體化

        //取得日期資訊
        JSONObject json = new JSONObject();
        try {
            json.put("testDate",selectDay);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildPOST(RECORD_INFO, json.toString(), requestListener);
    }

    private ApiProxy.OnApiListener requestListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(PeriodRecordActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            parserJson(result);
                        }else if (errorCode == 23) {  //token失效
                            Toasty.error(PeriodRecordActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(PeriodRecordActivity.this, LoginActivity.class));
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(PeriodRecordActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(PeriodRecordActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(PeriodRecordActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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
            progressDialog.dismiss();
        }
    };

    //解析後台資料
    private void parserJson(JSONObject result) {
        record = MenstruationRecord.newInstance(result.toString());
        //體重
        String userWeight = String.valueOf(record.getSuccess().getMeasure().getWeight());
        editWeight.setText(userWeight);

        //辨識結果
        String paramName = record.getSuccess().getMeasure().getParamName();
        if (paramName.equals("FollicularORLutealPhase")){
            textAnalysis.setText(R.string.in_low_cell);  //低濾泡
            //字形大小需要中文與英文不同
            textAnalysis.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_record_FollicularORLutealPhase));
        }else if (paramName.equals("Unrecognizable")){
            textAnalysis.setText(R.string.unknow);      //無法辨識
            textAnalysis.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_unknow));
        }else if (paramName.equals("General")){
            textAnalysis.setText(R.string.non_period);  //非排卵期
        }else if (paramName.equals("HighFollicularORLutealPhase")){
            textAnalysis.setText(R.string.in_high_cell); //高濾泡
            textAnalysis.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_record_HighFollicularORLutealPhase));
        }else if (paramName.equals("Ovulation")){
            textAnalysis.setText(R.string.in_period);    //排卵期
        }

        //2021/04/21
        changeRecord.getMeasure().setParam(record.getSuccess().getMeasure().getParam());

        //體溫
        String userTemperature = String.valueOf(record.getSuccess().getMeasure().getTemperature());
        textBodyTemp.setText(userTemperature);

        //脹痛,出血,行房
        boolean BeastPain = record.getSuccess().getStatus().isBreastPain();
        breastPain.setChecked(BeastPain);
        boolean Bleeding = record.getSuccess().getStatus().isBleeding();
        bleeding.setChecked(Bleeding);
        boolean Intercourse = record.getSuccess().getStatus().isIntercourse();
        intercourse.setChecked(Intercourse);

        //設置顏色,狀態,氣味,症狀
        setSecretion();
    }

    //設置顏色,狀態,氣味,症狀
    private void setSecretion() {
        //顏色
        String[] colors = new String[]{ getString(R.string.normal), getString(R.string.white), getString(R.string.yellow),
                getString(R.string.milky), getString(R.string.brown), getString(R.string.greenish_yellow)};

        cAdapter = new ColorAdapter(this);

        String secretionsColor = record.getSuccess().getSecretions().getColor();
        RecordColor recordColor = RecordColor.getColor(secretionsColor);
        int pos_color = recordColor.getIndex();
        cAdapter.setData(colors, pos_color);
        gridViewColor.setAdapter(cAdapter);
        changeRecord.getSecretions().setColor(secretionsColor);  //2021/02/19

        gridViewColor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                cAdapter.setSelection(position);   //傳直更新
                cAdapter.notifyDataSetChanged();

                RecordColor recordColor = RecordColor.getEnName(position);
                String ColorName = recordColor.getName();
                changeRecord.getSecretions().setColor(ColorName);//寫回後台
            }
        });

        //味道
        taste = new String[]{ getString(R.string.normal), getString(R.string.fishy), getString(R.string.stink)};

        aAdapter = new TasteAdapter(this);

        String secretionsTaste = record.getSuccess().getSecretions().getSmell();
        RecordTaste recordTaste = RecordTaste.getTaste(secretionsTaste);
        int pos_taste = recordTaste.getIndex();
        aAdapter.setData(taste, pos_taste);
        changeRecord.getSecretions().setSmell(secretionsTaste); //2021/02/19

        gridViewTaste.setAdapter(aAdapter);
        gridViewTaste.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                aAdapter.setSelection(position);   //傳直更新
                aAdapter.notifyDataSetChanged();

                RecordTaste recordTaste = RecordTaste.getEnName(position);
                String TasteName = recordTaste.getName();
                changeRecord.getSecretions().setSmell(TasteName); //寫回後台
            }
        });

        //型態
        types = new String[]{ getString(R.string.normal), getString(R.string.thick),
                getString(R.string.liquid_milky), getString(R.string.liquid)};

        tAdapter = new SecretionTypeAdapter(this);

        String secretionsType = record.getSuccess().getSecretions().getSecretionType();
        RecordType recordType = RecordType.getType(secretionsType);
        int pos_type = recordType.getIndex();
        tAdapter.setData(types, pos_type);
        changeRecord.getSecretions().setSecretionType(secretionsType);  //2021/02/19

        gridViewType.setAdapter(tAdapter);
        gridViewType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                tAdapter.setSelection(position);   //傳值更新
                tAdapter.notifyDataSetChanged();
                RecordType recordType = RecordType.getEnName(position);
                String TypeName = recordType.getName();
                changeRecord.getSecretions().setSecretionType(TypeName);  //寫回後台
            }
        });

        //症狀
        symptom = new String[]{ getString(R.string.normal), getString(R.string.hot),getString(R.string.allergy),
                getString(R.string.pain)};

        sAdapter = new SymptomAdapter(this);

        String secretionsSymptom = record.getSuccess().getSecretions().getSymptom();
        RecordSymptom recordSymptom = RecordSymptom.getSymptom(secretionsSymptom);
        int pos_symptom = recordSymptom.getIndex();
        sAdapter.setData(symptom,pos_symptom);
        changeRecord.getSecretions().setSymptom(secretionsSymptom);  //2021/02/19

        gridViewSymptom.setAdapter(sAdapter);
        gridViewSymptom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                sAdapter.setSelection(position);   //傳直更新
                sAdapter.notifyDataSetChanged();

                RecordSymptom recordSymptom = RecordSymptom.getEnName(position);
                String SymptomName = recordSymptom.getName();
                changeRecord.getSecretions().setSymptom(SymptomName); //寫回後台
            }
        });
    }

    /**** ************************
     * 藍芽
     * 1.initBle
     * 2.scan
     * 3.connect
     * ***********************************************************/
    private void initBle() {
        //啟用藍芽配適器
        BluetoothManager mBluetoothManager = (BluetoothManager)this.getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if(mBluetoothAdapter == null){ //如果==null，利用finish()取消程式。
            Toast.makeText(getBaseContext(),R.string.No_sup_Bluetooth,Toast.LENGTH_SHORT).show();
            finish();
            return;
        }else if (!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable(); //自動啟動藍芽
        }

        //dialog or Activity for ble search 開始掃描
        dialogBleConnect();
    }

    //Ble search
    private void dialogBleConnect() {
        alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_bleconnect, null);
        RecyclerView bleDialog = view.findViewById(R.id.rvBleScanView);  //放置掃描到的藍芽設備List

        mDeviceListAdapter = new BluetoothLeAdapter();
        bleDialog.setAdapter(mDeviceListAdapter);
        bleDialog.setHasFixedSize(true);
        bleDialog.setLayoutManager(new LinearLayoutManager(this));

        alertDialog.setView(view);
        alertDialog.setCancelable(false);  //disable touch screen area only cancel's button can close dialog
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //dialog背景透明

        mDeviceListAdapter.OnItemClick(itemClick);  //這個忘記就會閃退了~~

        //開始掃描
        isScanning = true;
        mHandler = new Handler();
        if (isScanning){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback); //停止搜尋
                    Toasty.info(PeriodRecordActivity.this, R.string.search_in_5_min, Toast.LENGTH_SHORT, true).show();
                }
            }, 5000);
            isScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            findDevice.clear();
            mDeviceListAdapter.clearDevice();
        }else {
            isScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback); //停止搜尋
        }

        //停止搜尋BLE裝置並關閉此dialog
        Button bleCancel = view.findViewById(R.id.btnBleCancel);
        bleCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                Log.d(TAG, "dialog : 使用者自行取消搜尋功能");
                alertDialog.dismiss();  //關閉此dialog
            }
        });

        alertDialog.show();
    }

    //建立一個BLAdapter的Callback，當使用startLeScan或stopLeScan時，每搜尋到一次設備都會跳到此callback
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            new Thread(()->{
                /**如果裝置沒有名字，就不顯示*/
                if (device.getName()!= null){
                    /**將搜尋到的裝置加入陣列*/
                    findDevice.add(new ScannedData(device.getName()
                            , String.valueOf(rssi)
                            , device.getAddress()));
                    /**將陣列中重複Address的裝置濾除，並使之成為最新數據*/
                    ArrayList newList = getSingle(findDevice);
                    runOnUiThread(()->{
                        /**將陣列送到RecyclerView列表中*/
                        mDeviceListAdapter.addDevice(newList);
                    });
                }
            }).start();
        }
    };

    /**濾除重複的藍牙裝置(以Address判定)*/
    private ArrayList getSingle(ArrayList list) {
        ArrayList tempList = new ArrayList<>();
        try {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                if (!tempList.contains(obj)) {
                    tempList.add(obj);
                } else {
                    tempList.set(getIndex(tempList, obj), obj);
                }
            }
            return tempList;
        } catch (ConcurrentModificationException e) {
            return tempList;
        }
    }

    /**
     * 以Address篩選陣列->抓出該值在陣列的哪處
     */
    private int getIndex(ArrayList temp, Object obj) {
        for (int i = 0; i < temp.size(); i++) {
            if (temp.get(i).toString().contains(obj.toString())) {
                return i;
            }
        }
        return -1;
    }

    /**** 取得欲連線之裝置後跳轉頁面 ***/
    private BluetoothLeAdapter.OnItemClick itemClick = new BluetoothLeAdapter.OnItemClick() {
        @Override
        public void onItemClick(ScannedData selectedDevice) {
            //關閉搜尋
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

            //裝置名稱
            deviceName = selectedDevice.getDeviceName();

            //啟動BLE背景連線
            mBluetoothLeService.connect(selectedDevice.getAddress());

            //關閉視窗
            if (alertDialog.isShowing())
                alertDialog.dismiss();
        }
    };

    /** ble背景服務 **/
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBluetoothLeService = ((yhyBleService.LocalBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    //註冊藍芽信息接受器
    private void registerBleReceiver() {
        Log.d(TAG, "註冊藍芽信息接受器");

        /** 綁定 BLE Server  背景服務 **/
        Intent gettIntent = new Intent(this, yhyBleService.class);
        bindService(gettIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(gettIntent);

        mBleReceiver = new BleReceiver();
        registerReceiver(mBleReceiver, yhyBleService.makeIntentFilter());
    }

    /**
     * 藍牙信息接收器
     */
    private class BleReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }

            byte[] data = intent.getByteArrayExtra(yhyBleService.EXTRA_DATA);

            switch (action) {
                case yhyBleService.ACTION_GATT_CONNECTED:
                    Toasty.info(PeriodRecordActivity.this, R.string.ble_is_connect, Toast.LENGTH_SHORT, true).show();
                    break;

                case yhyBleService.ACTION_GATT_DISCONNECTED:
                    Toast.makeText(PeriodRecordActivity.this, R.string.ble_is_disconnected_and_release, Toast.LENGTH_SHORT).show();
                    mBluetoothLeService.disconnect();
                    mBluetoothLeService.release();
                    updateConnectionStatus(getString(R.string.ble_unconnected));
                    searchBLE.setVisibility(View.VISIBLE);         //搜尋藍芽按鈕顯示
                    if(myCountDownTimer != null)
                        myCountDownTimer.cancel();                    //定時功能取消會
                    linearLayout.setVisibility(View.INVISIBLE);  //定時進度條隱藏
                    break;

                case yhyBleService.ACTION_CONNECTING_FAIL:
                    Toast.makeText(PeriodRecordActivity.this, R.string.ble_is_disconnected, Toast.LENGTH_SHORT).show();
                    mBluetoothLeService.disconnect();
                    searchBLE.setVisibility(View.VISIBLE);        //搜尋藍芽按鈕顯示
                    if (myCountDownTimer != null)
                        myCountDownTimer.cancel();                    //定時功能取消
                    linearLayout.setVisibility(View.INVISIBLE);   //定時進度條隱藏
                    break;

                case yhyBleService.ACTION_NOTIFY_ON:
                    Log.d(TAG, "onReceive: 收到BLE通知服務 啟動成功");
                    deviceAddress = intent.getStringExtra(yhyBleService.EXTRA_MAC);
                    textBleStatus.setText(deviceName + " " + getString(R.string.ble_connect_status));
                    textBleStatus.setTextColor(Color.RED);
                    searchBLE.setVisibility(View.INVISIBLE);    //搜尋藍芽按鈕隱藏
                    startMeasure.setVisibility(View.VISIBLE);  //測量按鈕顯示
                    break;
                case yhyBleService.ACTION_DATA_AVAILABLE:
                    String[] str = ByteUtils.byteArrayToString(data).split(","); //以,分割
                    String degreeStr = str[2];
                    double degree = Double.parseDouble(degreeStr)/100;
                    textBodyTemp.setText(String.valueOf(degree));
                    break;

                default:
                    break;
            }
        }
    }

    /*** 顯示現在藍芽連線狀態 *****/
    private void updateConnectionStatus(String bleStatus){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textBleStatus.setText(bleStatus);
            }
        });
    }

    //量測command 2021/03/16
    private void sendCommand(String deviceAddress){
        String request = "AIDO,0"; //詢問溫度command/@3mins
        byte[] messageBytes = new byte[0];
        try {
            messageBytes = request.getBytes("UTF-8"); //Sting to byte
            mBluetoothLeService.writeDataToDevice(messageBytes, deviceAddress);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to convert message string to byte array");
        }

    }

    @Override  ///照片回傳
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Activity.DEFAULT_KEYS_DIALER && resultCode == -1){
            new Thread(()->{
                //在BitmapFactory中以檔案URI路徑取得相片檔案，並處理為AtomicReference<Bitmap>，方便後續旋轉圖片
                AtomicReference<Bitmap> getHighImage = new AtomicReference<>(BitmapFactory.decodeFile(mPath));
                Matrix matrix = new Matrix();
                matrix.setRotate(90f);//轉90度
                getHighImage.set(Bitmap.createBitmap(getHighImage.get()
                        ,0,0
                        ,getHighImage.get().getWidth()
                        ,getHighImage.get().getHeight()
                        ,matrix,true));
                runOnUiThread(()->{
                    //以Glide設置圖片(因為旋轉圖片屬於耗時處理，故會LAG一下，且必須使用Thread執行緒)
                    Glide.with(this)
                            .load(getHighImage.get())
                            .centerCrop()
                            .into(photoShow);
                });
            }).start();
            //當日可以連續拍照
            takePhoto.setText(R.string.re_camera);
            photoIdentify.setVisibility(View.VISIBLE); //辨識按鈕
        }else {
            Toasty.info(PeriodRecordActivity.this, getString(R.string.you_do_nothing), Toast.LENGTH_SHORT, true).show();
        }

        /* 自定義相機
        if(resultCode != RESULT_CANCELED){ //如果沒照相就回來而沒有判斷resultCode會造成閃退 2021/05/19
            if(requestCode == CAMERA_RECORD){
                photoPath = data.getStringExtra("path");
                if(photoPath != null){
                  photoShow.setImageURI(Uri.fromFile(new File(photoPath)));
                  takePhoto.setText(R.string.re_camera);     //2021/02/19
                  photoIdentify.setVisibility(View.VISIBLE); //辨識按鈕
                }
            }
        }
        */
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume:" + deviceAddress);
        //註冊藍芽信息接受器
        registerBleReceiver();
    }

        @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (mBluetoothLeService != null){
            unregisterReceiver(mBleReceiver);
            mBleReceiver = null;
            mBluetoothLeService.disconnect();
            mBluetoothLeService.release();
        }
        unbindService(mServiceConnection);
        mBluetoothLeService = null;

        if(myCountDownTimer != null)
            myCountDownTimer.cancel();
    }

    public class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

            int progress = (int) (millisUntilFinished/1000);

            measureProgress.setProgress(measureProgress.getMax()-progress);
        }

        @Override
        public void onFinish() {
            Toasty.info(PeriodRecordActivity.this, R.string.measure_done, Toast.LENGTH_SHORT, true).show();
            sendCommand(deviceAddress);                //詢問溫度command
            startMeasure.setVisibility(View.VISIBLE);  //量測按鈕顯示
            linearLayout.setVisibility(View.INVISIBLE);   //進度條隱藏
        }
    }
}
