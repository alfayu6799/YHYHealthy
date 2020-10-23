package com.example.yhyhealthydemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button loginButton;
    EditText account, password;
    TextView register, forget;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
    }

    private void initView() {
        account = findViewById(R.id.et_account);
        password = findViewById(R.id.et_password);
        register = findViewById(R.id.tv_register);
        forget = findViewById(R.id.tv_forget);
        loginButton = findViewById(R.id.bt_login);

        loginButton.setOnClickListener(this);
        forget.setOnClickListener(this);
        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_login:
                //要判斷account & password是否正確(還沒寫)
                Intent intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.tv_register: //註冊
                dialogRegister();
                break;
            case R.id.tv_forget:  //忘記密碼
                dialigForget();
                break;
        }
    }

    //帳號註冊fxn
    private void dialogRegister() {
        AlertDialog.Builder alertDialogRegister = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View registerView = inflater.inflate(R.layout.dialog_register, null);
        alertDialogRegister.setView(registerView);

        alertDialogRegister.setCancelable(false)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        dialogInterface.dismiss();
                    }
                });

        AlertDialog dialogRegister = alertDialogRegister.create();
        dialogRegister.show();
    }

    //忘記密碼fxn
    private void dialigForget() {
        AlertDialog.Builder alertDialogForget = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View mView = layoutInflater.inflate(R.layout.dialog_forget_password, null);
        alertDialogForget.setView(mView);

        EditText email = mView.findViewById(R.id.et_email);
        alertDialogForget.setCancelable(false)
                .setPositiveButton("送出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                            dialogInterface.dismiss();
                    }
                });

        AlertDialog dialog = alertDialogForget.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailStr = email.getText().toString();
                Boolean wantToCloseDialog = (email.getText().toString().trim().isEmpty());
                if (wantToCloseDialog){
                    Toast.makeText(LoginActivity.this, "信箱帳號不得為空", Toast.LENGTH_SHORT).show();
                }else{
                    dialog.dismiss();
                    Toast.makeText(LoginActivity.this, "確認函已經發送至信箱 : " + emailStr, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
