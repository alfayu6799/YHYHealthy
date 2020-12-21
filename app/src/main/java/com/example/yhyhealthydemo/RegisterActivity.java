package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private Button register;
    private EditText account, password, confirmpass, email;

    private RadioGroup registerGroup;
    private String verification = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
    }

    private void initView() {
        account = findViewById(R.id.edtAccountInput);
        password = findViewById(R.id.edtPasswordInput);
        confirmpass = findViewById(R.id.edtPasswordConfirm);
        email = findViewById(R.id.edtEmailInput);

        registerGroup = findViewById(R.id.rdGroupRegister);
        registerGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rdoBtnEmail){
                    verification = "email";
                }else{
                    verification = "phone";
                }
            }
        });

        register = findViewById(R.id.btnRegisterSend);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Account = Objects.requireNonNull(account.getEditableText().toString().trim());
                String Password = Objects.requireNonNull(password.getEditableText().toString().trim());
                String Confirm = Objects.requireNonNull(confirmpass.getEditableText().toString().trim());
                String Email = Objects.requireNonNull(email.getEditableText().toString().trim());
                if (!Account.isEmpty() && !Password.isEmpty() && !Email.isEmpty()){
                    if (!Password.equals(Confirm)){
                        Toast.makeText(getApplicationContext(),getString(R.string.password_confirm_error), Toast.LENGTH_SHORT).show();
                    }else if (isValidEmailAddress(Email)){ //信箱驗證成功
                        if (verification.equals("email")){
                            //將資料傳到後台去要token : acoount
//                            RegisterApi();
                        }else{
                            Toast.makeText(getApplicationContext(),getString(R.string.phone_verification), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(getApplicationContext(),getString(R.string.email_is_vaild), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),getString(R.string.plase_fill_info), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //mail address的判斷
    private static boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
