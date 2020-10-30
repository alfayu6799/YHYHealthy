package com.example.yhyhealthydemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button loginButton;
    EditText account, password;
    TextView register, forget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().hide(); //hide ActionBar
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
//                userLoginApi(); //將使用者的資料傳給後台
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

        //init editText
        EditText account = registerView.findViewById(R.id.edtAccountInput);
        EditText password = registerView.findViewById(R.id.edtPasswordInput);
        EditText email = registerView.findViewById(R.id.edtEmailInput);
        EditText confirm = registerView.findViewById(R.id.edtPasswordConfirm);

        alertDialogRegister.setCancelable(false) //disable touch other screen will close dialog
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

        dialogRegister.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Account = Objects.requireNonNull(account.getEditableText().toString().trim());
                String Password = Objects.requireNonNull(password.getEditableText().toString().trim());
                String Email = Objects.requireNonNull(email.getEditableText().toString().trim());
                String Confirm = Objects.requireNonNull(confirm.getEditableText().toString().trim());
                // Check for empty data in the form
                if (!Account.isEmpty() && !Password.isEmpty() && !Email.isEmpty()){
                    if (!Password.equals(Confirm)){
                        Toast.makeText(getApplicationContext(), "密碼不一致", Toast.LENGTH_LONG).show();
                    }else if (isValidEmailAddress(Email)){
//                                registerUserApi(Account, Password, Email); //將使用者註冊後拿到的資料傳到後台去
                        Toast.makeText(getApplicationContext(), "註冊成功", Toast.LENGTH_LONG).show();
                        dialogRegister.dismiss();
                    }else {
                        Toast.makeText(getApplicationContext(), "信箱無效", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "請補齊資料", Toast.LENGTH_LONG).show();
                }
            }
        });

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
//                    forgetPasswordApi(); //傳給後台去做發送確認信函給使用者
                    Toast.makeText(LoginActivity.this, "確認函已經發送至信箱 : " + emailStr, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }

    //check email is Valid
    public static boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
