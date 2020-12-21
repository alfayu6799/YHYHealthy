package com.example.yhyhealthydemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthydemo.data.IncomeBean;
import com.example.yhyhealthydemo.data.LineChartBean;
import com.example.yhyhealthydemo.data.LineChartManager;
import com.example.yhyhealthydemo.tools.LocalJsonAnalyzeUtil;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import sun.bob.mcalendarview.MCalendarView;
import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.listeners.OnDateClickListener;
import sun.bob.mcalendarview.listeners.OnMonthChangeListener;
import sun.bob.mcalendarview.views.ExpCalendarView;
import sun.bob.mcalendarview.vo.DateData;


public class OvulationActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout calendarLayout, chartLayout;
    private ScrollView scrollViewLayout;
    private Button calendar, chart;

    //月曆
    private ExpCalendarView calendarView;
//    private MCalendarView calendarView;
    private TextView YearMonthTv;

    private TextView oveuSetting; //經期設定click
    private TextView oveuEdit;    //經期編輯click

    private TextView temperature;
    private TextView ovulResult;

    //圖表
    LineChart lineChart;
    XAxis xAxis;        //X軸:日期
    YAxis leftYAxis;    //左側Y軸:溫度
    YAxis rightYaxis;   //右側Y軸要隱藏
    Legend legend;
    LineChartBean lineChartBean;        //假資料
    List<IncomeBean> incomeBeanList;    //假資料
    LineChartManager lineChartManager1; //假資料

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().hide(); //hide ActionBar
        setContentView(R.layout.activity_ovulation);

        initView();

    }

    private void initView() {
        calendar = findViewById(R.id.btnSwitchCalendar);
        chart = findViewById(R.id.btnSwitchChart);
        calendarLayout = findViewById(R.id.ly_calender);
        scrollViewLayout = findViewById(R.id.ly_scroll_comment);
        chartLayout = findViewById(R.id.lychart);

        ovulResult = findViewById(R.id.tv_ovul_result_1);
        ovulResult.setText("唾液辨識 : 濾泡/黃體期");

        temperature = findViewById(R.id.tv_ovul_temp_1);
        temperature.setText("基礎體溫 : 36.55" + "\u2103 ");

        oveuSetting = findViewById(R.id.tv_ovul_setting);
        oveuEdit = findViewById(R.id.tv_ovul_edit);

        calendar.setOnClickListener(this);
        chart.setOnClickListener(this);
        oveuSetting.setOnClickListener(this);
        oveuEdit.setOnClickListener(this);

        calendarView = findViewById(R.id.calendar);

        YearMonthTv = findViewById(R.id.main_YYMM_Tv);

        setCalendar(); //月曆init

        //圖表
        lineChart = findViewById(R.id.lineChart);
        initChartData(); //獲取數據
        initChart();     //圖表樣式

        //月曆區先顯示
        calendar.setBackgroundResource(R.drawable.rectangle_button);
        calendar.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
        calendarLayout.setVisibility(View.VISIBLE);
        scrollViewLayout.setVisibility(View.VISIBLE);
    }

    private void initChart() {
        lineChartManager1 = new LineChartManager(lineChart);
        //圖表樣式
        lineChartManager1.showLineChart(incomeBeanList, "我的體溫", getResources().getColor(R.color.orange));
        lineChartManager1.setMarkerView(this); //MarkView
    }


    private void initChartData() {
        //獲取數據from json
        lineChartBean = LocalJsonAnalyzeUtil.JsonToObject(this, "line_chart.json", LineChartBean.class);
        incomeBeanList = lineChartBean.getGRID0().getResult().getClientAccumulativeRate();
    }


    //月曆init
    private void setCalendar() {
        //set 月曆月份Title
        YearMonthTv.setText(Calendar.getInstance().get(Calendar.YEAR) + "年" + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "月");
//        //監聽日期
        calendarView.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onDateClick(View view, DateData date) {
                Toast.makeText(OvulationActivity.this, "您選擇的日期為 : "+String.format("%d/%d", date.getMonth(), date.getDay()), Toast.LENGTH_SHORT).show();
            }
        });

        //監聽月曆
        calendarView.setOnMonthChangeListener(new OnMonthChangeListener(){

            @Override
            public void onMonthChange(int year, int month) {
                YearMonthTv.setText(String.format("%d年%d月", year, month));
            }
        });

        //監聽日期
        calendarView.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onDateClick(View view, DateData date) {
                Toast.makeText(OvulationActivity.this, "您選擇的日期為 : "+String.format("%d/%d", date.getMonth(), date.getDay()), Toast.LENGTH_SHORT).show();
            }
        });

        //監聽月曆
        calendarView.setOnMonthChangeListener(new OnMonthChangeListener(){

            @Override
            public void onMonthChange(int year, int month) {
                YearMonthTv.setText(String.format("%d年%d月", year, month));
            }
        });

        Calendar calendar = Calendar.getInstance();
        //今天日期的背景顏色
        calendarView.markDate(new DateData(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE))
                .setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.rgb(192,192,192))));

        //mark special day
        calendarView.markDate(
                new DateData(2020, 12, 20)); //default's color is blue
        calendarView.markDate(
                new DateData(2020, 12, 21).setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.GREEN)));
        calendarView.markDate(
                new DateData(2020, 12, 22).setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.GREEN)));
        calendarView.markDate(
                new DateData(2020, 12, 23).setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.CYAN)));
        calendarView.markDate(
                new DateData(2020, 12, 24).setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.rgb(255,78,173))));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSwitchCalendar:
                calendar.setBackgroundResource(R.drawable.rectangle_button);
                chart.setBackgroundResource(R.drawable.relative_shape);
                calendar.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
                chart.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.black));
                calendarLayout.setVisibility(View.VISIBLE);
                scrollViewLayout.setVisibility(View.VISIBLE);
                chartLayout.setVisibility(View.GONE);
                break;
            case R.id.btnSwitchChart:
                calendar.setBackgroundResource(R.drawable.relative_shape);
                chart.setBackgroundResource(R.drawable.rectangle_button);
                calendar.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.black));
                chart.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
                calendarLayout.setVisibility(View.GONE);
                scrollViewLayout.setVisibility(View.GONE);
                chartLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.tv_ovul_setting:  //經期設定
                dialogPickDate();
                break;
            case R.id.tv_ovul_edit:     //經期編輯
                startActivity(new Intent(OvulationActivity.this, PeriodActivity.class));
                break;
        }
    }

    //經期設定對話框
    private void dialogPickDate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        final View view = inflater.inflate(R.layout.dialog_datepicker,null);
        builder.setView(view);
        //init dialog function
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        EditText toDate = view.findViewById(R.id.et_to_date);     //起始日期
        EditText fromDate = view.findViewById(R.id.et_from_date); //結束日期
        Button sendDate = view.findViewById(R.id.bt_send_date);   //儲存

        AlertDialog dialog = builder.create();

        toDate.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(OvulationActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                //todo
                                Calendar newDate = Calendar.getInstance();
                                newDate.set(year, month, dayOfMonth);
                                toDate.setText(dateFormatter.format(newDate.getTime()));

                            }
                        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        fromDate.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(OvulationActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                //todo
                                Calendar newDate = Calendar.getInstance();
                                newDate.set(year, month, dayOfMonth);
                                fromDate.setText(dateFormatter.format(newDate.getTime()));
                            }
                        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        sendDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ToDate = toDate.getText().toString();
                String FromDate = fromDate.getText().toString();
                Toast.makeText(OvulationActivity.this, "您設定的範圍為: " + ToDate + "~" + FromDate, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
