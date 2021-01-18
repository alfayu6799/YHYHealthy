package com.example.yhyhealthydemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/***** ****
 * 設定 - 個人設定 - 基本資料
 * * ***** *****/
public class UserBasicActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UserBasicActivity";

    TextView accountInfo;
    TextView genderInfo;
    EditText accountName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_basic);

        initView();
    }

    private void initView() {
        accountInfo = findViewById(R.id.textUserAccount);
        accountName = findViewById(R.id.edtChangeName);
        genderInfo = findViewById(R.id.textGender);

        genderInfo.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.textGender:
                dialogGender(); //性別採用彈跳視窗
                break;
        }
    }

    //性別採用彈跳視窗選擇
    private void dialogGender() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.please_input_gender));
        String[] genderItems = {getString(R.string.female), getString(R.string.male)};
        int checkedItem = 0;
        builder.setSingleChoiceItems(genderItems, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: //女性
                        genderInfo.setText(getString(R.string.female));
                        dialog.dismiss();
                        break;
                    case 1: //男性
                        genderInfo.setText(getString(R.string.male));
                        dialog.dismiss();
                        break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }
}