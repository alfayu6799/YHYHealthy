package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class AppPage extends AppCompatActivity {

    public void replaceFragment(int resId, Fragment f){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(resId, f);
        fragmentTransaction.commit();
    }
}
