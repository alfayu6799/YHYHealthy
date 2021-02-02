package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.google.android.material.textfield.TextInputLayout;

public class UserChangeVerifiActivity extends AppCompatActivity {

    private TextInputLayout mailStyleLayout, phoneStyleLayout, mobileStyleLayout;
    private RadioGroup StyleGroup;
    private EditText   editMail, editTelCode, editMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_change_verifi);

        initView();
    }

    private void initView() {
        editMail = findViewById(R.id.edtEmailStyle);  //信箱
        editTelCode = findViewById(R.id.edtTelCode);  //國際編碼
        editMobile = findViewById(R.id.edtMobile);    //手機號碼

        mailStyleLayout = findViewById(R.id.emailStyleLayout);
        phoneStyleLayout = findViewById(R.id.phoneStyleLayout);
        mobileStyleLayout = findViewById(R.id.mobileStyleLayout);

        StyleGroup = findViewById(R.id.rdoGroupStyle);
        StyleGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rtoBtnMailStyle){
                    mailStyleLayout.setVisibility(View.VISIBLE);
                    phoneStyleLayout.setVisibility(View.GONE);
                    mobileStyleLayout.setVisibility(View.GONE);
                }else {
                    mailStyleLayout.setVisibility(View.GONE);
                    phoneStyleLayout.setVisibility(View.VISIBLE);
                    mobileStyleLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}