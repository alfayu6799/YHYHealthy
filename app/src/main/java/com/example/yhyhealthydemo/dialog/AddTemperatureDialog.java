package com.example.yhyhealthydemo.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.example.yhyhealthydemo.R;
import com.zm.library.CircularImageView;

import java.util.Calendar;

public class AddTemperatureDialog extends Dialog {

    private final static String TAG = "AddTemperatureDialog";
    private Context context;
    private Button save;
    private ImageView cancel;
    private EditText userName , userBirthday;
    private ImageView takePhoto;
    private CircularImageView photoShow;

    private RadioGroup rdGroup;
    private String Gender = "F";

    /**
    * 自定義 Dialog listener
    * **/
    public interface PriorityListener{
        void setActivity(String name, String gender, String birthday);
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

        takePhoto = view.findViewById(R.id.ivTakePhoto);
        photoShow = view.findViewById(R.id.circularImageView);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: ");
            }
        });

        userName = view.findViewById(R.id.edtInputName);

        rdGroup = view.findViewById(R.id.rdGroup);
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

        userBirthday = view.findViewById(R.id.edtInputBirthay);
        userBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        cancel = view.findViewById(R.id.imageCancel);
        save = view.findViewById(R.id.bt_add_user_sure);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Name = userName.getText().toString().trim();
                String Birthday = userBirthday.getText().toString();

                if(TextUtils.isEmpty(Name)){
                    Toast.makeText(getContext(), "請填寫名稱", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(Birthday)){
                    Toast.makeText(getContext(), "請填寫出生", Toast.LENGTH_SHORT).show();
                }else {
                    listener.setActivity(Name, Gender, Birthday);
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
}
