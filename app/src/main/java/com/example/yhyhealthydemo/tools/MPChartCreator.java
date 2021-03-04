package com.example.yhyhealthydemo.tools;

import android.content.Context;
import android.graphics.Color;
import android.media.audiofx.AudioEffect;
import android.view.View;

import androidx.legacy.widget.Space;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class MPChartCreator {

    private Context context;

    //傳進來的物件
    private LineChart lineChart;
    private BarChart barChart;
    private CombinedChart combinedChart;

    private ArrayList<ArrayList<Double>> dataArrays = new ArrayList<ArrayList<Double>>();
    private ArrayList<String> dataArrayLineName = new ArrayList<>();
    private ArrayList<Integer> dataArrayLineColor = new ArrayList<>();

    ArrayList<ILineDataSet> dataSets = new ArrayList<>();

    //繪圖模式
    private int mode;

    //線圖模式
    public static int MODE_LINECHART = 0;
    public static int MODE_COMBINEDCHART = 1;
    public static int MODE_BARCHART = 2;

    //初始化
    public MPChartCreator(Context context, int mode, View view){
        this.context = context;
        this.mode = mode;

        if (mode == MODE_LINECHART)
            lineChart = (LineChart) view;

        if (mode == MODE_COMBINEDCHART)
            combinedChart = (CombinedChart) view;

        if (mode == MODE_BARCHART)
            barChart = (BarChart) view;
    }

    /**
     * 設計一個方法為將資料一筆一筆ADD後在call drawLineChart一次劃出
     *
     * @param dataArray 資料陣列
     * */

    public void lineChartAddDate(ArrayList<Double> dataArray, Integer color){
        if (mode == MODE_LINECHART){
            dataArrays.add(dataArray);
            dataArrayLineColor.add(color);
        }
    }

    /**
     * 當資料塞完後畫出折線圖
     * **/
    public void DrawLineChart(){
        if (mode == MODE_LINECHART)
            initLineChart();
    }

    //折線圖
    private void initLineChart() {

        lineChart.setScaleEnabled(false);                   // 取消縮放
        lineChart.getLegend().setEnabled(false);            //隱藏圖例
        lineChart.getDescription().setEnabled(false);       //隱藏描述

        YAxis rightAxis = lineChart.getAxisRight();         //獲取右側的Y軸
        rightAxis.setEnabled(false);                        //不顯示右側Y軸
        YAxis leftAxis = lineChart.getAxisLeft();           //獲取左側的Y軸線
        leftAxis.setDrawGridLines(false);                   //隱藏Y軸的格線
        leftAxis.setLabelCount(7);                           //體溫最多7階

        XAxis xAxis = lineChart.getXAxis(); //取得X軸
        //xAxis.setValueFormatter(new IndexAxisValueFormatter(label)); //x軸放入自定義的時間
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); //日期顯示在底層
        xAxis.setGranularity(1f);                      //x軸最小間隔
        //xAxis.setLabelCount(label.size());             //X軸的數量來自資料集
        xAxis.setGridColor(Color.BLACK);               //設置X軸的網格線的顏色
        xAxis.setAxisLineColor(Color.BLACK);
        xAxis.enableGridDashedLine(10f, 10f, 0f); //X軸格線虛線
        xAxis.setLabelCount(10);                         //日期做多10階




    }
}
