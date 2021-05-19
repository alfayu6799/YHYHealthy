package com.example.yhyhealthydemo.dialog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.yhyhealthydemo.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.Manifest.permission.CAMERA;

/****************************
 * 藍芽體溫 - 新增觀測者 Dialog
 * 照片base64
 * 名稱,性別,生日,身高,體重
******************************/
public class AddTemperatureDialog extends Dialog {

    private final static String TAG = "AddTemperatureDialog";
    private Context context;
    private Activity activity;

    private Button save;
    private ImageView cancel;
    private EditText userName , userBirthday;
    private EditText userHeight, userWeight;
    private ImageView takePhoto;

    private RadioGroup rdGroup;
    private String Gender = "F";

    private String mPath;

    /**
    * 自定義 Dialog listener
    * **/
    public interface PriorityListener{
        void setActivity(String name, String gender, String birthday, String weight, String height);
    }

    private PriorityListener listener;

    public AddTemperatureDialog(Context context, int theme, PriorityListener listener){
        super(context, theme);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_supervise, null);
        setContentView(view);

        //設置dialog大小
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics(); //獲取螢幕的寬跟高

        layoutParams.width = (int) (displayMetrics.widthPixels * 0.8); //寬度設置為螢幕的0.8
        window.setAttributes(layoutParams);

        takePhoto = view.findViewById(R.id.ivTakePhoto);  //拍照

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

        userName = view.findViewById(R.id.edtInputName);

        rdGroup = view.findViewById(R.id.rdGroup);  //性別
        rdGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if(checkedId == R.id.rdMale){
                    Gender = "M";
                }else{
                    Gender = "F";
                }
            }
        });

        userBirthday = view.findViewById(R.id.edtInputBirthday);  //生日
        userBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        userHeight = view.findViewById(R.id.edtInputHeight);  //身高
        userWeight = view.findViewById(R.id.edtInputWeight);  //體重

        cancel = view.findViewById(R.id.imageCancel);       //取消onClick
        save = view.findViewById(R.id.bt_add_user_sure);    //上傳到後台onClick

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        //上傳資料到後台
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Name = userName.getText().toString().trim();  //名稱
                String Birthday = userBirthday.getText().toString(); //生日
                String Height = userHeight.getText().toString();     //身高
                String Weight = userWeight.getText().toString();     //體重

                if(TextUtils.isEmpty(Name)){
                    Toast.makeText(getContext(), R.string.please_input_name, Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(Birthday)){
                    Toast.makeText(getContext(), R.string.please_input_birthday, Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(Height)) {
                    Toast.makeText(getContext(), R.string.please_input_height, Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(Weight)) {
                    Toast.makeText(getContext(), R.string.please_input_weight, Toast.LENGTH_SHORT).show();
                }else{
                    listener.setActivity(Name, Gender, Birthday, Weight, Height);
                    //update to Api
                    updateToApi();
                    dismiss();
                }
            }
        });
    }

    //日期的設定
    public void showDatePickerDialog() {
        //設定初始日期
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR) - 12;
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        // 跳出日期選擇器
        DatePickerDialog dpd = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {

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

    //上傳到後台
    private void updateToApi() {
    }

    //開啟相機功能
    private void openCamera() {
        if(ActivityCompat.checkSelfPermission(getContext(), CAMERA) == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //呼叫原生相機
            File imageFile = getImageFile(); //取得相片位置
            if (imageFile == null) return;
            //取得相片檔案的本機位置
            Uri imageUri = FileProvider.getUriForFile(getContext(),"com.example.yhyhealthydemo.fileprovider", imageFile);
            //通知相機照片儲存位置
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            //將照片帶回
            AddTemperatureDialog.this.activity.startActivityForResult(intent, Activity.DEFAULT_KEYS_DIALER);
        }
    }

    //取得相片檔案的URL
    private File getImageFile(){
        String time = new SimpleDateFormat("yyMMdd").format(new Date());
        String fileName = time + "_";
        File dir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile = File.createTempFile(fileName, ".jpg", dir);
            mPath = imageFile.getAbsolutePath(); //照片檔案位置
            return imageFile;
        } catch (IOException e) {
            return null;
        }
    }



}
