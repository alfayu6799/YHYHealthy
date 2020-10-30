package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;

import com.example.yhyhealthydemo.adapter.OvualationPager;
import com.google.android.material.tabs.TabLayout;

public class OvulationActivity extends AppCompatActivity implements View.OnClickListener {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private OvualationPager ovualationPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().hide(); //hide ActionBar
        setContentView(R.layout.activity_ovulation);

        tabLayout = findViewById(R.id.tableLayout);
        viewPager = findViewById(R.id.viewPager);

        View view = findViewById(R.id.btnBack);
        view.setOnClickListener(this);

        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_cal)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_rec)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_chart)));
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnBack:
                onBackPressed();
                break;
        }
    }
}
