package com.example.yhyhealthy.tools;

import android.content.Context;
import android.graphics.Color;

import com.example.yhyhealthy.datebase.CycleRecord;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.List;

/**** 圖表管理類  *****/

public class MPAChartCreator {

    private static final String TAG = "MPAChartCreator";

    private Context context;

    private BarChart barChart;          //直條圖
    private XAxis xAxis;                //X轴
    private YAxis leftYAxis;            //左侧Y轴
    private YAxis rightYAxis;           //右侧Y轴 自定义XY轴值

    //建構子

    public MPAChartCreator(Context context, BarChart barChart) {
        this.context = context;
        this.barChart = barChart;
        xAxis = barChart.getXAxis();
        leftYAxis = barChart.getAxisLeft();
        rightYAxis = barChart.getAxisRight();

        initChart(barChart);
    }

    //初始化直條圖
    private void initChart(BarChart barChart) {
        //顯示格線
        barChart.setDrawGridBackground(true);
        //不顯示邊線
        barChart.setDrawBorders(false);
        //雙擊不進行縮放
        barChart.setDoubleTapToZoomEnabled(false);
        //不用描述
        barChart.getDescription().setEnabled(false);
        //不用圖例
        barChart.getLegend().setEnabled(false);

        //是否繪製X軸網格線
        xAxis.setDrawGridLines(true);
        xAxis.setGridLineWidth(1.5f);
        //xAxis.enableGridDashedLine(10f, 10f, 0f); //網格線為虛線

        //是否繪製Y軸網格線
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setGridLineWidth(1.5f);
        //leftYAxis.enableGridDashedLine(10f, 10f, 0f); //網格線為虛線

        //Y軸右側隱藏
        rightYAxis.setEnabled(false);

        //Y軸右側網格線不顯示
        rightYAxis.setDrawGridLines(false);

        //X軸設置顯示位置在底部
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        //最小刻度27
        leftYAxis.setAxisMinimum(25f);

        //最大刻度40
        leftYAxis.setAxisMaximum(40f);

        //X軸最小間距
        xAxis.setGranularity(1f);

        //繪製X軸線
        xAxis.setDrawAxisLine(false);

        //custom markerView
//        LineChartMarkView mv = new LineChartMarkView(context, xAxis.getValueFormatter());
//        mv.setChartView(barChart);
//        barChart.setMarker(mv);
    }

    /**
     * 顯示直條圖
     */

    public void showBarChart(final List<CycleRecord.SuccessBean> dataList, int color){
        ArrayList<String> label = new ArrayList<>(); //日期

        List<BarEntry> entries = new ArrayList<BarEntry>();

        List<BarEntry> entries2 = new ArrayList<BarEntry>();

        for (int i = 0; i < dataList.size(); i++) {

            CycleRecord.SuccessBean data = dataList.get(i);

            String[] str = data.getTestDate().split("-");
            String thisMonth = str[1];      //Month
            String DayOfMonth = str[2];     //day
            //String testDay = thisMonth + "/" + DayOfMonth;  // Month/Day

            if (data.getCycleStatus().contains(4)) {
                data.setTemperature(40);
                entries.add(new BarEntry(i, (float) data.getTemperature()));
            } else if (data.getCycleStatus().contains(6)) {
                data.setTemperature(40);
                entries2.add(new BarEntry(i, (float) data.getTemperature()));
            }

            label.add(DayOfMonth);   //Day

        }

        xAxis.setValueFormatter(new IndexAxisValueFormatter(label));
//        xAxis.setLabelCount(label.size());

        //X軸最多顯示10筆(日期)
        xAxis.setLabelCount(10, false);

        //Y軸的最多顯示7筆(體溫)
        leftYAxis.setLabelCount(7, false);

        //數據源
        BarDataSet barDataSet = new BarDataSet(entries, "經期"); // add entries to dataset
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        BarDataSet barDataSet2 = new BarDataSet(entries2, "排卵期"); // add entries to dataset
        barDataSet2.setAxisDependency(YAxis.AxisDependency.LEFT);

        //設置長條圖顏色
        barDataSet.setColor(Color.rgb(225,63,174));
        barDataSet2.setColor(Color.rgb(225,186,63));

        barDataSet.setDrawValues(false);
        barDataSet2.setDrawValues(false);

        List<IBarDataSet> dataSets=new ArrayList<IBarDataSet>();
        dataSets.add(barDataSet);
        dataSets.add(barDataSet2);
        //柱状图数据集
        BarData barData = new BarData(dataSets);

        //设置柱子宽度
        barData.setBarWidth(1.2f);
        barChart.setData(barData);//装载数据

        barChart.setFitBars(true); //X轴自适应所有柱形图
        barChart.invalidate();//刷新
    }
}
