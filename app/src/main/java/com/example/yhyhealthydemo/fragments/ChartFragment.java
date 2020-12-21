package com.example.yhyhealthydemo.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.yhyhealthydemo.R;
import com.github.mikephil.charting.charts.LineChart;

/**********************
* 排卵圖表Page
* 圖表引用第三方庫 : MPAndroidChart
* ********************/
public class ChartFragment extends Fragment {

    private View view;
    private TextView textRange;
    private LineChart chart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;
        view = inflater.inflate(R.layout.fragment_chart, container, false);

        textRange = view.findViewById(R.id.tvMMDD);
        chart = view.findViewById(R.id.lineChart);

        return view;
    }

}
