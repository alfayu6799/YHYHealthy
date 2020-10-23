package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.example.yhyhealthydemo.adapter.OvualationPager;
import com.google.android.material.tabs.TabLayout;

public class OvulationActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private OvualationPager ovualationPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ovulation);

        tabLayout = findViewById(R.id.tableLayout);
        viewPager = findViewById(R.id.viewPager);

        tabLayout.addTab(tabLayout.newTab().setText("月曆"));
        tabLayout.addTab(tabLayout.newTab().setText("紀錄"));
        tabLayout.addTab(tabLayout.newTab().setText("圖表"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        ovualationPager = new OvualationPager(this, getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(ovualationPager);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}
