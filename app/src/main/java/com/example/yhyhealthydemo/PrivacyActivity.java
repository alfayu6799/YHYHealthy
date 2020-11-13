package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class PrivacyActivity extends AppCompatActivity {

    private CheckBox privacy1, privacy2;
    private Button confirm;
    private TextView privacyContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        privacy1 = findViewById(R.id.chkPrivacy1);
        privacy2 = findViewById(R.id.chkPrivacy2);
        confirm = findViewById(R.id.btnPrivacy);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(privacy1.isChecked() && privacy2.isChecked()){
                    startActivity(new Intent(getBaseContext(), LoginActivity.class));
                    finish();
                }else{
                    Toast.makeText(PrivacyActivity.this, getString(R.string.privacy_not_pass), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
