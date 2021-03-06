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
 * ??????????????????
 * ?????? CameraActivity
 * ?????? yhyBleService
 * ????????????DeviceBaseActivity
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

    //??????
    private yhyBleService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BroadcastReceiver mBleReceiver;
    private BluetoothLeAdapter mDeviceListAdapter;
    private boolean isScanning = false;
    private ArrayList<ScannedData> findDevice = new ArrayList<>();
    private Handler mHandler;
    private String deviceName ="";   //??????????????????
    private String deviceAddress = ""; //??????MAC

    //????????????????????????
    MyGridView gridViewColor, gridViewTaste, gridViewType, gridViewSymptom;
    private EditText   editWeight;    //??????
    private TextView   textBodyTemp;  //??????

    private Switch bleeding, breastPain, intercourse;

    //Api
    private ApiProxy proxy;
    private MenstruationRecord record;
    private ChangeRecord changeRecord;
    private PhotoData photoData;

    //??????,??????,??????,?????? Adapter
    private SymptomAdapter sAdapter;
    private SecretionTypeAdapter tAdapter;
    private TasteAdapter aAdapter;
    private ColorAdapter cAdapter;
    private String[] types;  //??????
    private String[] symptom;  //??????
    private String[] taste;  //??????
//    private String[] colors; //??????

    //??????
    private ProgressDialog progressDialog;
    //????????????
    private GifImageView gifImageView;

    //
    private static final int CAMERA_RECORD = 2;
    private String mPath = "";

    //????????????
    private ProgressBar measureProgress;
    private MyCountDownTimer myCountDownTimer;
    private LinearLayout linearLayout;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_period);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //????????????

        //init
        initView();

        //????????????OvulationActivity?????????
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            strDay = bundle.getString("DAY");
            if (strDay == null){  //????????????????????????????????????????????? 2021/02/18
                DateTime day = new DateTime(new Date());
                String today = day.toString("yyyy-MM-dd");
                strDay = today;
            }else {
                textRecordDate.setText(strDay);
            }
            setRecordInfo(strDay);  //??????????????????????????????key
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        textRecordDate = findViewById(R.id.tvRecordDate);
        takePhoto = findViewById(R.id.btnPhoto);
        photoIdentify = findViewById(R.id.btnPhotoIdentify);    //??????Onclick
        searchBLE = findViewById(R.id.ivBLESearch);
        startMeasure = findViewById(R.id.btnStartMeasure);      //????????????onclick
        saveSetting = findViewById(R.id.btnSaveSetting);
        photoShow = findViewById(R.id.ivPhoto);                 //????????????
        textBleStatus = findViewById(R.id.tvBLEConnectStatus);  //????????????????????????
        textBodyTemp = findViewById(R.id.tvBodyTemp);           //????????????
        textAnalysis = findViewById(R.id.tvAnalysis);           //??????????????????

        //??????background
        gifImageView = findViewById(R.id.game_gif);
        gifImageView.setBackgroundResource(R.mipmap.yhy_new_background);

        linearLayout = findViewById(R.id.ly_progressBar);       //????????????Layout
        measureProgress = findViewById(R.id.progressBar);       //???????????????
        measureProgress.setProgressTintList(ColorStateList.valueOf(Color.BLUE)); //?????????????????????

        bleeding = findViewById(R.id.swBleeding);               //??????
        breastPain = findViewById(R.id.swBreastPain);           //??????
        intercourse = findViewById(R.id.swIntercourse);         //??????

        //??????????????????
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
            case R.id.btnPhoto:      //??????OnClick
                checkIsToday();
                break;
            case R.id.ivBLESearch:   //????????????
                String todayStr = String.valueOf(LocalDate.now());
                if (strDay.equals(todayStr)){    //????????????????????????
                    openBleFunction();
                }else {
                    Toasty.info(PeriodRecordActivity.this,getString(R.string.ble_only_today), Toast.LENGTH_SHORT, true).show();
                }
                break;
            case R.id.btnStartMeasure: //????????????
                startCountDownTime();
                break;
            case R.id.btnSaveSetting: //????????????????????????????????????Onclick
                checkBeforeUpdate();  //??????????????????????????????????????????fxn
                break;
            case R.id.btnPhotoIdentify: //??????????????????????????????
                upPhotoToApi();
                break;
        }
    }

    //??????3??????,???10???????????????onTick??????
    private void startCountDownTime(){
        myCountDownTimer = new MyCountDownTimer(180000, 1000);
        myCountDownTimer.start();  //????????????
        startMeasure.setVisibility(View.INVISIBLE); //??????????????????
        linearLayout.setVisibility(View.VISIBLE);   //???????????????
        sendCommand(deviceAddress);
    }

    //2021/02/19 ????????????
    private void upPhotoToApi() {
        //?????????????????????base64
        base64Str = ImageUtils.imageToBase64(mPath);         //????????????????????????
//        base64Str = ImageUtils.imageToBase64(photoPath);   //??????????????????

        //????????????
        DateTime today = new DateTime();
        String todayStr = today.toString("yyyy-MM-dd");

        JSONObject json = new JSONObject();
        try {
            json.put("testDate", todayStr);
            json.put("img", base64Str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //??????????????????api
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
                        }else if (errorCode == 23) { //token??????
                            Toasty.error(PeriodRecordActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(PeriodRecordActivity.this, LoginActivity.class));
                            finish();
                        }else if (errorCode == 31 ){
                            Toasty.error(PeriodRecordActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(PeriodRecordActivity.this, LoginActivity.class)); //????????????
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

    //???????????????????????????????????? 2021/02/19
    private void parserPhotoId(JSONObject result) {
        photoData = PhotoData.newInstance(result.toString());
        String paramName = photoData.getSuccess().getParamName();
        String param = photoData.getSuccess().getParam();
        changeRecord.getMeasure().setParam(param); //????????????????????????
        if(!paramName.isEmpty()) {
            if (paramName.equals("FollicularORLutealPhase")) {
                textAnalysis.setText(R.string.in_low_cell);  //?????????
                //???????????????????????????????????????
                textAnalysis.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_record_FollicularORLutealPhase));
            } else if (paramName.equals("Unrecognizable")) {
                textAnalysis.setText(R.string.unknow);      //????????????
                textAnalysis.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_unknow));
            } else if (paramName.equals("General")) {
                textAnalysis.setText(R.string.non_period);  //????????????
            } else if (paramName.equals("HighFollicularORLutealPhase")) {
                textAnalysis.setText(R.string.in_high_cell); //?????????
                textAnalysis.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_record_HighFollicularORLutealPhase));
            } else if (paramName.equals("Ovulation")) {
                textAnalysis.setText(R.string.in_period);    //?????????
            }else if (paramName.equals("SalivaTooThick")){ //???????????? 2021/08/05
                textAnalysis.setText(R.string.saliva_too_thick);
            }else if (paramName.equals("SalivaWet")){       //???????????? 2021/08/06
                textAnalysis.setText(R.string.saliva_wet);
            }else if (paramName.equals("BubblesExcessive")){   //???????????? 2021/08/06
                textAnalysis.setText(R.string.bubbles_excessive);
            }else if (paramName.equals("Insufficient")){  //????????????
                textAnalysis.setText(R.string.insufficient);
            }else if (paramName.equals("Brightness")){  //????????????
                textAnalysis.setText(R.string.brightness);
            }
        }
    }


    //??????????????????????????????????????????
    private void checkBeforeUpdate() {
        //??????
        if(!TextUtils.isEmpty(editWeight.getText().toString())){
            changeRecord.getMeasure().setWeight(Double.parseDouble(editWeight.getText().toString()));
        }

        //??????
        changeRecord.getMeasure().setTemperature(Double.parseDouble(textBodyTemp.getText().toString()));
        UpdateToApi();
    }

    //????????????????????????????????? 2021/02/18
    private void checkIsToday() {
        DateTime today = new DateTime(new Date());
        String todayStr = today.toString("yyyy-MM-dd");
        if (strDay.equals(todayStr)){    //?????????????????????&??????
            openCamera();
        }else {
            Toasty.info(PeriodRecordActivity.this,getString(R.string.camera_only_today), Toast.LENGTH_SHORT, true).show();
        }
    }

    //????????????????????????
    private void openBleFunction() {
        if(ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initBle();  //???????????????
        }else {
            requestPermission();
        }
    }

    //??????????????????
    private void openCamera() {
        if(ActivityCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  //??????????????????
            File imageFile = getImageFile();
            if (imageFile == null) return;
            Uri imageUri = FileProvider.getUriForFile(this,"com.yhihc.group.yhyhealthy.fileprovider", imageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, Activity.DEFAULT_KEYS_DIALER);
            /* ??????????????????(?????????Camera??????)
            Intent camera = new Intent(PeriodRecordActivity.this, CameraActivity.class);
            startActivityForResult(camera, CAMERA_RECORD);
            */
        }else {
            requestPermission();
        }
    }

    //?????????????????????URL
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

    //????????????????????? 2021/02/19 leona
    private void UpdateToApi() {
        //????????????????????????????????????
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
                            finish(); //???????????????
                        }else if (errorCode == 23) { //token??????
                            Toasty.error(PeriodRecordActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(PeriodRecordActivity.this, LoginActivity.class));
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(PeriodRecordActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(PeriodRecordActivity.this, LoginActivity.class)); //????????????
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
            case R.id.swBleeding:   //??????
                if (isCheck){
                    changeRecord.getStatus().setBleeding(true);
                }else {
                    changeRecord.getStatus().setBleeding(false);
                }
                break;
            case R.id.swBreastPain: //??????
                if(isCheck){
                    changeRecord.getStatus().setBreastPain(true);
                }else {
                    changeRecord.getStatus().setBreastPain(false);
                }
                break;
            case R.id.swIntercourse: //??????
                if (isCheck){
                    changeRecord.getStatus().setIntercourse(true);
                }else {
                    changeRecord.getStatus().setIntercourse(false);
                }
                break;
        }
    }

    //????????????????????? 2021/01/08 leona
    private void setRecordInfo(String selectDay) {
        proxy = ApiProxy.getInstance();      //api?????????
        changeRecord = new ChangeRecord();   //?????????

        //??????????????????
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
                        }else if (errorCode == 23) {  //token??????
                            Toasty.error(PeriodRecordActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(PeriodRecordActivity.this, LoginActivity.class));
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(PeriodRecordActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(PeriodRecordActivity.this, LoginActivity.class)); //????????????
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

    //??????????????????
    private void parserJson(JSONObject result) {
        record = MenstruationRecord.newInstance(result.toString());
        //??????
        String userWeight = String.valueOf(record.getSuccess().getMeasure().getWeight());
        editWeight.setText(userWeight);

        //????????????
        String paramName = record.getSuccess().getMeasure().getParamName();
        if (paramName.equals("FollicularORLutealPhase")){   //?????????
            textAnalysis.setText(R.string.in_low_cell);
            //???????????????????????????????????????
            textAnalysis.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_record_FollicularORLutealPhase));
        }else if (paramName.equals("Unrecognizable")){     //????????????
            textAnalysis.setText(R.string.unknow);
            textAnalysis.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_unknow));
        }else if (paramName.equals("General")){   //?????????
            textAnalysis.setText(R.string.non_period);
        }else if (paramName.equals("HighFollicularORLutealPhase")){  //?????????
            textAnalysis.setText(R.string.in_high_cell);
            textAnalysis.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_record_HighFollicularORLutealPhase));
        }else if (paramName.equals("Ovulation")){  //?????????
            textAnalysis.setText(R.string.in_period);
        }else if (paramName.equals("SalivaTooThick")){ //???????????? 2021/08/05
            textAnalysis.setText(R.string.saliva_too_thick);
        }else if (paramName.equals("SalivaWet")){       //???????????? 2021/08/06
            textAnalysis.setText(R.string.saliva_wet);
        }else if (paramName.equals("BubblesExcessive")){   //???????????? 2021/08/06
            textAnalysis.setText(R.string.bubbles_excessive);
        }else if (paramName.equals("Insufficient")){  //???????????? 2021/08/06
            textAnalysis.setText(R.string.insufficient);
        }else if (paramName.equals("Brightness")){  //???????????? 2021/08/06
            textAnalysis.setText(R.string.brightness);
        }

        //2021/04/21
        changeRecord.getMeasure().setParam(record.getSuccess().getMeasure().getParam());

        //??????
        String userTemperature = String.valueOf(record.getSuccess().getMeasure().getTemperature());
        textBodyTemp.setText(userTemperature);

        //??????,??????,??????
        boolean BeastPain = record.getSuccess().getStatus().isBreastPain();
        breastPain.setChecked(BeastPain);
        boolean Bleeding = record.getSuccess().getStatus().isBleeding();
        bleeding.setChecked(Bleeding);
        boolean Intercourse = record.getSuccess().getStatus().isIntercourse();
        intercourse.setChecked(Intercourse);

        //????????????,??????,??????,??????
        setSecretion();
    }

    //????????????,??????,??????,??????
    private void setSecretion() {
        //??????
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
                cAdapter.setSelection(position);   //????????????
                cAdapter.notifyDataSetChanged();

                RecordColor recordColor = RecordColor.getEnName(position);
                String ColorName = recordColor.getName();
                changeRecord.getSecretions().setColor(ColorName);//????????????
            }
        });

        //??????
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
                aAdapter.setSelection(position);   //????????????
                aAdapter.notifyDataSetChanged();

                RecordTaste recordTaste = RecordTaste.getEnName(position);
                String TasteName = recordTaste.getName();
                changeRecord.getSecretions().setSmell(TasteName); //????????????
            }
        });

        //??????
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
                tAdapter.setSelection(position);   //????????????
                tAdapter.notifyDataSetChanged();
                RecordType recordType = RecordType.getEnName(position);
                String TypeName = recordType.getName();
                changeRecord.getSecretions().setSecretionType(TypeName);  //????????????
            }
        });

        //??????
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
                sAdapter.setSelection(position);   //????????????
                sAdapter.notifyDataSetChanged();

                RecordSymptom recordSymptom = RecordSymptom.getEnName(position);
                String SymptomName = recordSymptom.getName();
                changeRecord.getSecretions().setSymptom(SymptomName); //????????????
            }
        });
    }

    /**** ************************
     * ??????
     * 1.initBle
     * 2.scan
     * 3.connect
     * ***********************************************************/
    private void initBle() {
        //?????????????????????
        BluetoothManager mBluetoothManager = (BluetoothManager)this.getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if(mBluetoothAdapter == null){ //??????==null?????????finish()???????????????
            Toast.makeText(getBaseContext(),R.string.No_sup_Bluetooth,Toast.LENGTH_SHORT).show();
            finish();
            return;
        }else if (!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable(); //??????????????????
        }

        //dialog or Activity for ble search ????????????
        dialogBleConnect();
    }

    //Ble search
    private void dialogBleConnect() {
        alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_bleconnect, null);
        RecyclerView bleDialog = view.findViewById(R.id.rvBleScanView);  //??????????????????????????????List

        mDeviceListAdapter = new BluetoothLeAdapter();
        bleDialog.setAdapter(mDeviceListAdapter);
        bleDialog.setHasFixedSize(true);
        bleDialog.setLayoutManager(new LinearLayoutManager(this));

        alertDialog.setView(view);
        alertDialog.setCancelable(false);  //disable touch screen area only cancel's button can close dialog
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //dialog????????????

        mDeviceListAdapter.OnItemClick(itemClick);  //???????????????????????????~~

        //????????????
        isScanning = true;
        mHandler = new Handler();
        if (isScanning){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback); //????????????
                    Toasty.info(PeriodRecordActivity.this, R.string.search_in_5_min, Toast.LENGTH_SHORT, true).show();
                }
            }, 5000);
            isScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            findDevice.clear();
            mDeviceListAdapter.clearDevice();
        }else {
            isScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback); //????????????
        }

        //????????????BLE??????????????????dialog
        Button bleCancel = view.findViewById(R.id.btnBleCancel);
        bleCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                Log.d(TAG, "dialog : ?????????????????????????????????");
                alertDialog.dismiss();  //?????????dialog
            }
        });

        alertDialog.show();
    }

    //????????????BLAdapter???Callback????????????startLeScan???stopLeScan?????????????????????????????????????????????callback
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            new Thread(()->{
                /**???????????????????????????????????????*/
                if (device.getName()!= null){
                    /**?????????????????????????????????*/
                    findDevice.add(new ScannedData(device.getName()
                            , String.valueOf(rssi)
                            , device.getAddress()));
                    /**??????????????????Address?????????????????????????????????????????????*/
                    ArrayList newList = getSingle(findDevice);
                    runOnUiThread(()->{
                        /**???????????????RecyclerView?????????*/
                        mDeviceListAdapter.addDevice(newList);
                    });
                }
            }).start();
        }
    };

    /**???????????????????????????(???Address??????)*/
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
     * ???Address????????????->??????????????????????????????
     */
    private int getIndex(ArrayList temp, Object obj) {
        for (int i = 0; i < temp.size(); i++) {
            if (temp.get(i).toString().contains(obj.toString())) {
                return i;
            }
        }
        return -1;
    }

    /**** ??????????????????????????????????????? ***/
    private BluetoothLeAdapter.OnItemClick itemClick = new BluetoothLeAdapter.OnItemClick() {
        @Override
        public void onItemClick(ScannedData selectedDevice) {
            //????????????
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

            //????????????
            deviceName = selectedDevice.getDeviceName();

            //??????BLE????????????
            mBluetoothLeService.connect(selectedDevice.getAddress());

            //????????????
            if (alertDialog.isShowing())
                alertDialog.dismiss();
        }
    };

    /** ble???????????? **/
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

    //???????????????????????????
    private void registerBleReceiver() {
        Log.d(TAG, "???????????????????????????");

        /** ?????? BLE Server  ???????????? **/
        Intent gettIntent = new Intent(this, yhyBleService.class);
        bindService(gettIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(gettIntent);

        mBleReceiver = new BleReceiver();
        registerReceiver(mBleReceiver, yhyBleService.makeIntentFilter());
    }

    /**
     * ?????????????????????
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
                    searchBLE.setVisibility(View.VISIBLE);         //????????????????????????
                    if(myCountDownTimer != null)
                        myCountDownTimer.cancel();                    //?????????????????????
                    linearLayout.setVisibility(View.INVISIBLE);  //?????????????????????
                    break;

                case yhyBleService.ACTION_CONNECTING_FAIL:
                    Toast.makeText(PeriodRecordActivity.this, R.string.ble_is_disconnected, Toast.LENGTH_SHORT).show();
                    mBluetoothLeService.disconnect();
                    searchBLE.setVisibility(View.VISIBLE);        //????????????????????????
                    if (myCountDownTimer != null)
                        myCountDownTimer.cancel();                    //??????????????????
                    linearLayout.setVisibility(View.INVISIBLE);   //?????????????????????
                    break;

                case yhyBleService.ACTION_NOTIFY_ON:
                    Log.d(TAG, "onReceive: ??????BLE???????????? ????????????");
                    deviceAddress = intent.getStringExtra(yhyBleService.EXTRA_MAC);
                    textBleStatus.setText(deviceName + " " + getString(R.string.ble_connect_status));
                    textBleStatus.setTextColor(Color.RED);
                    searchBLE.setVisibility(View.INVISIBLE);    //????????????????????????
                    startMeasure.setVisibility(View.VISIBLE);  //??????????????????
                    break;
                case yhyBleService.ACTION_DATA_AVAILABLE:
                    String[] str = ByteUtils.byteArrayToString(data).split(","); //???,??????
                    String degreeStr = str[2];
                    double degree = Double.parseDouble(degreeStr)/100;
                    textBodyTemp.setText(String.valueOf(degree));
                    break;

                default:
                    break;
            }
        }
    }

    /*** ?????????????????????????????? *****/
    private void updateConnectionStatus(String bleStatus){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textBleStatus.setText(bleStatus);
            }
        });
    }

    //??????command 2021/03/16
    private void sendCommand(String deviceAddress){
        String request = "AIDO,0"; //????????????command/@3mins
        byte[] messageBytes = new byte[0];
        try {
            messageBytes = request.getBytes("UTF-8"); //Sting to byte
            mBluetoothLeService.writeDataToDevice(messageBytes, deviceAddress);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to convert message string to byte array");
        }

    }

    @Override  ///????????????
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Activity.DEFAULT_KEYS_DIALER && resultCode == -1){
            new Thread(()->{
                //???BitmapFactory????????????URI???????????????????????????????????????AtomicReference<Bitmap>???????????????????????????
                AtomicReference<Bitmap> getHighImage = new AtomicReference<>(BitmapFactory.decodeFile(mPath));
                Matrix matrix = new Matrix();
                matrix.setRotate(90f);//???90???
                getHighImage.set(Bitmap.createBitmap(getHighImage.get()
                        ,0,0
                        ,getHighImage.get().getWidth()
                        ,getHighImage.get().getHeight()
                        ,matrix,true));
                runOnUiThread(()->{
                    //???Glide????????????(?????????????????????????????????????????????LAG????????????????????????Thread?????????)
                    Glide.with(this)
                            .load(getHighImage.get())
                            .centerCrop()
                            .into(photoShow);
                });
            }).start();
            //????????????????????????
            takePhoto.setText(R.string.re_camera);
            photoIdentify.setVisibility(View.VISIBLE); //????????????
        }else {
            Toasty.info(PeriodRecordActivity.this, getString(R.string.you_do_nothing), Toast.LENGTH_SHORT, true).show();
        }

        /* ???????????????
        if(resultCode != RESULT_CANCELED){ //???????????????????????????????????????resultCode??????????????? 2021/05/19
            if(requestCode == CAMERA_RECORD){
                photoPath = data.getStringExtra("path");
                if(photoPath != null){
                  photoShow.setImageURI(Uri.fromFile(new File(photoPath)));
                  takePhoto.setText(R.string.re_camera);     //2021/02/19
                  photoIdentify.setVisibility(View.VISIBLE); //????????????
                }
            }
        }
        */
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume:" + deviceAddress);
        //???????????????????????????
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
            sendCommand(deviceAddress);                //????????????command
            startMeasure.setVisibility(View.VISIBLE);  //??????????????????
            linearLayout.setVisibility(View.INVISIBLE);   //???????????????
        }
    }
}
