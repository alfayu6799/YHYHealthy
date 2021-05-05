package com.example.yhyhealthydemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.example.yhyhealthydemo.datebase.TempDataApi;
import com.example.yhyhealthydemo.module.ApiProxy;
import com.example.yhyhealthydemo.tools.ImageUtils;
import com.example.yhyhealthydemo.tools.RotateTransformation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.atomic.AtomicReference;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthydemo.module.ApiProxy.BLE_USER_UPDATE;
/***
 * 觀測者對象編輯(單一個體)
 * */
public class TemperEditActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "TemperEditActivity";

    private ImageView back;        //回上一頁
    private ImageView takePhoto;   //拍照
    private ImageView photoShow;   //照片顯示
    private EditText  userName;    //使用者名稱
    private EditText  userHeight, userWeight; //使用者身高體重
    private EditText  userBirthday; //使用者生日

    private RadioGroup rdGroup;
    private String Gender = "F";  //性別

    private Button btnSave;       //存檔上傳到後台

    private String mPath = "";         //原照片位址全域宣告
    private File tmpPhoto;

    //更新使用者需要targetId
    private int targetId = 0;
    private File file;

    //api
    private ApiProxy proxy;

    //進度條
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temper_edit);

        initView();

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            targetId = bundle.getInt("targetId");
            String degreeName = bundle.getString("name");
            String degreeGender = bundle.getString("gender");
            String degreeBirthday = bundle.getString("birthday");
            String degreeWeight = bundle.getString("weight");
            String degreeHeight = bundle.getString("height");
            String degreeHeadShot = bundle.getString("HeadShot");

            initData(degreeName,degreeGender,degreeBirthday,degreeWeight,degreeHeight,degreeHeadShot);
        }

    }

    private void initData(String name, String gender, String birthday, String weight, String height, String headShot) {
        userName.setText(name);
        userBirthday.setText(birthday);
        userHeight.setText(height);
        userWeight.setText(weight);
        if(gender.equals("F")){
            rdGroup.check(R.id.rdFemale1);
        }else {
            rdGroup.check(R.id.rdMale1);
        }

        if(headShot != null){
            //照片顯示
            file = new File(headShot);
            Uri imageUri = Uri.fromFile(file);
            Glide.with(this)
                    .load(imageUri)
                    .signature(new ObjectKey(Long.toString(System.currentTimeMillis())))
                    .into(photoShow);
        }

    }

    private void initView() {
        userName = findViewById(R.id.edtInputName1);
        photoShow = findViewById(R.id.circularImageView1);
        takePhoto = findViewById(R.id.ivTakePhoto1);
        userHeight = findViewById(R.id.edtInputHeight1);  //身高
        userWeight = findViewById(R.id.edtInputWeight1);  //體重
        back = findViewById(R.id.imageCancel1);  //取消
        btnSave = findViewById(R.id.btnEditUser1);   //存檔
        userBirthday = findViewById(R.id.edtInputBirthday1); //生日

        rdGroup = findViewById(R.id.rdGroup1);  //性別
        rdGroup.setOnCheckedChangeListener(this);

        takePhoto.setOnClickListener(this);
        userBirthday.setOnClickListener(this);
        back.setOnClickListener(this);
        btnSave.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageCancel1:
                finish();  //回上一頁
                break;
            case R.id.ivTakePhoto1:
                openCamera();   //啟動相機
                break;
            case R.id.edtInputBirthday1:
                showDatePickerDialog();  //日期
                break;
            case R.id.btnEditUser1:
                updateToApi();  //update to 後台
                break;
        }
    }

    //更新到後台
    private void updateToApi() {
        proxy = ApiProxy.getInstance();

        String base64Str = null;
        //可能存在空指针須使用null判断  2021/04/25
        if(tmpPhoto != null )
           base64Str = ImageUtils.imageToBase64(tmpPhoto.toString());

        String Name = userName.getText().toString().trim();   //名稱
        String Birthday = userBirthday.getText().toString();  //生日
        float Height = Float.parseFloat(userHeight.getText().toString()); //身高
        float Weight = Float.parseFloat(userWeight.getText().toString()); //體重

        JSONObject json = new JSONObject();
        try {
            json.put("targetId", targetId);
            json.put("name", Name);
            json.put("gender", Gender);
            json.put("birthday",Birthday);
            json.put("height", Height);
            json.put("weight", Weight);
            json.put("headShot", base64Str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //執行上傳到後台
        proxy.buildPOST(BLE_USER_UPDATE, json.toString(), updateListener);
    }

    private ApiProxy.OnApiListener updateListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(TemperEditActivity.this, getString(R.string.title_process), getString(R.string.process), true);
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
                            Toasty.success(TemperEditActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();

                            //if(tmpPhoto.exists()) //如果檔案存在就刪除會閃退
                            //    tmpPhoto.delete();

                            setResult(RESULT_OK); //回到上一頁
                            finish();  //關閉此頁
                        }else {
                            Toasty.error(TemperEditActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //日期選擇
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

    //原生相機
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //呼叫原生相機
        File imageFile = getImageFile(); //取得相片位置
        if (imageFile == null) return;
        //取得相片檔案的本機位置
        Uri imageUri = FileProvider.getUriForFile(this,"com.example.yhyhealthydemo.fileprovider", imageFile);
        //通知相機照片儲存位置
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
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

    @Override  //性別顯示
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        if(checkedId == R.id.rdMale1){
            Gender = "M";
        }else{
            Gender = "F";
        }
    }

    @Override  //拍照後回傳
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Activity.DEFAULT_KEYS_DIALER && resultCode == -1) {

            new Thread(() -> {
                //取得旋轉度
                int rotate = readPictureDegree(mPath);

                //取得bitmap
                Bitmap bitmap = BitmapFactory.decodeFile(mPath);

                if(rotate == 0){  //不用旋轉的照片則直接存檔  2021/04/23
                    saveBitmap(bitmap);
                }else {
                    rotateBitmap(bitmap);
                }

                //在BitmapFactory中以檔案URI路徑取得相片檔案，並處理為AtomicReference<Bitmap>，方便後續旋轉圖片
                AtomicReference<Bitmap> getHighImage = new AtomicReference<>(BitmapFactory.decodeFile(mPath)); //實例化
                Matrix matrix = new Matrix();
                matrix.setScale(0.5f, 0.5f);
                //圖片重建
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
                            .transform(new RotateTransformation(this,rotate))
                            .into(photoShow);
                });
            }).start();
        }else {
            Toasty.info(TemperEditActivity.this, getString(R.string.camera_not_action), Toast.LENGTH_SHORT, true).show();
        }
    }

    //旋轉圖片
    private void rotateBitmap(Bitmap rotateOrg) {
        Matrix matrix = new Matrix();
        matrix.postRotate(readPictureDegree(mPath));
        Bitmap rotateAfter = Bitmap.createBitmap(rotateOrg,0,0, rotateOrg.getWidth(), rotateOrg.getHeight(), matrix, true);
        saveBitmap(rotateAfter);
    }

    //圖檔存至本地端
    private void saveBitmap(Bitmap bitmap){
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/6,bitmap.getHeight()/6,true);
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE); //放照片的目錄
        tmpPhoto = new File(directory, "newPicture" + ".jpg");
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(tmpPhoto);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //獲取圖片旋轉角度
    private static int readPictureDegree(String path){
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //如果檔案存在就要砍掉
        if(file.exists()) file.delete();
    }
}