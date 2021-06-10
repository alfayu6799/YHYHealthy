package com.example.yhyhealthy;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.ImageUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import es.dmoral.toasty.Toasty;

import static android.Manifest.permission.CAMERA;
import static com.example.yhyhealthy.module.ApiProxy.BLE_USER_ADD;

/**
 * 新增觀測者
 * Camera權限
 * create 2021/03/20
 * **/
public class TemperatureAddActivity extends DeviceBaseActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "TemperatureAddActivity";

    private ImageView cancel;     //回上一頁
    private ImageView takePhoto;  //拍照
    private ImageView photoShow;  //照片顯示
    private EditText  userName;   //使用者名稱
    private EditText  userHeight, userWeight; //使用者身高體重
    private EditText  userBirthday; //使用者生日

    private RadioGroup rdGroup;
    private String Gender = "F";  //性別

    private Button btnSave;       //存檔上傳到後台

    private String mPath = "";  //照片位址全域宣告

    //api
    private ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_add);

        proxy = ApiProxy.getInstance();

        initView();
    }

    private void initView() {
        userName = findViewById(R.id.edtInputName);
        photoShow = findViewById(R.id.circularImageView);
        takePhoto = findViewById(R.id.ivTakePhoto);
        userHeight = findViewById(R.id.edtInputHeight);  //身高
        userWeight = findViewById(R.id.edtInputWeight);  //體重
        cancel = findViewById(R.id.imageCancel);  //取消
        btnSave = findViewById(R.id.btnAddUser);   //存檔
        userBirthday = findViewById(R.id.edtInputBirthday); //生日

        rdGroup = findViewById(R.id.rdGroup);  //性別
        rdGroup.setOnCheckedChangeListener(this);

        takePhoto.setOnClickListener(this);
        userBirthday.setOnClickListener(this);
        cancel.setOnClickListener(this);
        btnSave.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageCancel:
                finish(); //回上一頁
                break;
            case R.id.ivTakePhoto:
                if(ActivityCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCamera(); //開啟相機
                }else {
                    requestPermission(); //要求權限
                }
                break;
            case R.id.edtInputBirthday:   //日期選擇
                showDatePickerDialog();
                break;
            case R.id.btnAddUser:  //上傳
                checkBeforeUpdate();
                break;
        }
    }

    //日期的設定
    private void showDatePickerDialog() {
        //設定初始日期
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR) - 12;
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        // 跳出日期選擇器
        DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (year <= mYear) {
                    // 完成選擇，顯示日期
                    userBirthday.setText(mDateTimeFormat(year) + "-" + mDateTimeFormat(monthOfYear + 1) + "-" + mDateTimeFormat(dayOfMonth));
                }
            }
        }, mYear, mMonth, mDay);
        dpd.show();
    }

    private String mDateTimeFormat(int value) {
        String RValue = String.valueOf(value);
        if (RValue.length() == 1)
            RValue = "0" + RValue;
        return RValue;
    }

    //上傳前先檢查資料是否齊全
    private void checkBeforeUpdate() {

        if(TextUtils.isEmpty(userName.getText().toString().trim())){
            Toasty.error(this, R.string.please_input_name, Toast.LENGTH_SHORT,true).show();
        }else if(TextUtils.isEmpty(userBirthday.getText().toString())){
            Toasty.error(this, R.string.please_input_birthday, Toast.LENGTH_SHORT,true).show();
        }else if(TextUtils.isEmpty(userHeight.getText().toString())) {
            Toasty.error(this, R.string.please_input_height, Toast.LENGTH_SHORT,true).show();
        }else if(TextUtils.isEmpty(userWeight.getText().toString())) {
            Toasty.error(this, R.string.please_input_weight, Toast.LENGTH_SHORT,true).show();
        }else{
            updateToApi();  //update to 後台
        }
    }

    //傳資料給後台
    private void updateToApi() {
        String base64Str = null;
        if (mPath != null) {
            File file = new File((mPath));
            String filePath = file.getAbsolutePath();
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            base64Str = ImageUtils.getBase64String(bitmap);
        }

//        String base64Str = ImageUtils.imageToBase64(mPath);   //照片

        String Name = userName.getText().toString().trim();  //名稱
        String Birthday = userBirthday.getText().toString(); //生日
        String Height = userHeight.getText().toString();     //身高
        String Weight = userWeight.getText().toString();     //體重


        JSONObject json = new JSONObject();
        try {
            json.put("name", Name);
            json.put("gender", Gender);
            json.put("birthday", Birthday);
            json.put("height", Height);
            json.put("weight", Weight);
            json.put("headShot", base64Str);
//            json.put("headShot", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        Log.d(TAG, "updateToApi: " + json.toString());
        proxy.buildPOST(BLE_USER_ADD, json.toString(), addUserListener);
    }

    private ApiProxy.OnApiListener addUserListener = new ApiProxy.OnApiListener() {
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
                            Toasty.success(TemperatureAddActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                            setResult(RESULT_OK);
                            finish(); //回到上一頁
                        }else if (errorCode == 23){ //token失效
                            Toasty.error(TemperatureAddActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(TemperatureAddActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(TemperatureAddActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //呼叫原生相機
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //呼叫原生相機
        File imageFile = getImageFile(); //取得相片位置
        if (imageFile == null) return;
        //取得相片檔案的本機位置
        Uri imageUri = FileProvider.getUriForFile(this,"com.yhihc.group.yhyhealthy.fileprovider", imageFile);
        //通知相機照片儲存位置
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        //將照片帶回
        startActivityForResult(intent, Activity.DEFAULT_KEYS_DIALER);
    }

    //取得相片檔案的URL
    private File getImageFile(){
        String time = new SimpleDateFormat("yyMMdd").format(new Date());
        String fileName = time + "_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile = File.createTempFile(fileName, ".jpg", dir);
            mPath = imageFile.getAbsolutePath(); //照片檔案位置
            return imageFile;
        } catch (IOException e) {
            return null;
        }
    }

    //取得照片回傳
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Activity.DEFAULT_KEYS_DIALER && resultCode == -1) {
            new Thread(() -> {
                //在BitmapFactory中以檔案URI路徑取得相片檔案，並處理為AtomicReference<Bitmap>，方便後續旋轉圖片
                AtomicReference<Bitmap> getHighImage = new AtomicReference<>(BitmapFactory.decodeFile(mPath));
                Matrix matrix = new Matrix();
                //matrix.setRotate(90f);//轉90度
                getHighImage.set(Bitmap.createBitmap(getHighImage.get()
                        , 0, 0
                        , getHighImage.get().getWidth()
                        , getHighImage.get().getHeight()
                        , matrix, true));
                runOnUiThread(() -> {
                    //以Glide設置圖片(因為旋轉圖片屬於耗時處理，故會LAG一下，且必須使用Thread執行緒)
                    Glide.with(this)
                            .load(getHighImage.get())
                            .centerCrop()
                            .into(photoShow);
                });
            }).start();

        } else {
            Toasty.info(TemperatureAddActivity.this, getString(R.string.camera_not_action), Toast.LENGTH_SHORT, true).show();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        if(checkedId == R.id.rdMale){
            Gender = "M";
        }else{
            Gender = "F";
        }
    }
}