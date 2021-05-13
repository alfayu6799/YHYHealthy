package com.example.yhyhealthydemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.container_home, new MeasureFragment()).commit();
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener OnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()){
                case R.id.nav_home:     //首頁
                    fragment = new MeasureFragment();
                    break;
                case R.id.nav_edu:      //衛教
                    fragment = new EducationFragment();
                    break;
                case R.id.nav_record:   //歷史紀錄
                    fragment = new RecordFragment();
                    break;
                case R.id.nav_setting:  //設定
                    fragment = new SettingFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.container_home, fragment).commit();

            return true;
        }
    };
}
