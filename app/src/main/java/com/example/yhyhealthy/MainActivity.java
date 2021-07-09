package com.example.yhyhealthy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.yhyhealthy.fragments.EducationFragment;
import com.example.yhyhealthy.fragments.MeasureFragment;
import com.example.yhyhealthy.fragments.RecordFragment;
import com.example.yhyhealthy.fragments.SettingFragment;
import com.example.yhyhealthy.module.ApiProxy;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import pl.droidsonroids.gif.GifImageView;

import static com.example.yhyhealthy.module.ApiProxy.userSetting;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //禁止旋轉

        //取得版本 2021/07/08
        getVersion();

        //檢查是否填過使用者資料 2021/07/08
        checkUserInfoIsComplete();

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.container_home, new MeasureFragment()).commit();
    }

    //檢查是否填寫過使用者資料
    private void checkUserInfoIsComplete() {
        if (!userSetting) //如果沒有填寫則導引至使用者資料填寫頁面
            startActivity(new Intent(MainActivity.this, UserBasicActivity.class));
    }

    //取得版本
    private void getVersion() {
        Context context = getApplicationContext(); // or activity.getApplicationContext()
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        String myVersionName = "not available"; // initialize String

        try {
            myVersionName = packageManager.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "軟體版本: " + myVersionName);
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
