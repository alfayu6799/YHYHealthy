package com.example.yhyhealthydemo.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.yhyhealthydemo.fragments.CalendarFragment;
import com.example.yhyhealthydemo.fragments.ChartFragment;
import com.example.yhyhealthydemo.fragments.DocumentaryFragment;

public class OvualationPager extends FragmentPagerAdapter {

    private Context context;
    private Integer totalTabs;

    public OvualationPager(Context context, @NonNull FragmentManager fm, Integer totalTabs) {
        super(fm);
        this.context = context;
        this.totalTabs = totalTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                CalendarFragment calendarFragment = new CalendarFragment();
                return calendarFragment;
            case 1:
                DocumentaryFragment documentaryFragment = new DocumentaryFragment();
                return documentaryFragment;
            case 2:
                ChartFragment chartFragment = new ChartFragment();
                return chartFragment;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return totalTabs;
    }
}
