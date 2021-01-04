package com.example.yhyhealthydemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.yhyhealthydemo.data.IncomeBean;
import com.example.yhyhealthydemo.data.LineChartBean;
import com.example.yhyhealthydemo.data.LineChartManager;
import com.example.yhyhealthydemo.datebase.Menstruation;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.listeners.OnDateClickListener;
import sun.bob.mcalendarview.listeners.OnMonthChangeListener;
import sun.bob.mcalendarview.views.ExpCalendarView;
import sun.bob.mcalendarview.vo.DateData;

public class OvulationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "OvulationActivity";

    private LinearLayout calendarLayout, chartLayout;
    private ScrollView scrollViewLayout;
    private Button calendar, chart;

    //月曆
    private ExpCalendarView calendarView;
    private TextView YearMonthTv;
    private TextView menstruationPeriodDay; //週期顯示
    private String choseDay;
    private String periodDate = "";    //從api獲取日期
    private String periodStatus = "";   //從api獲取狀態
    private String periodDegree = ""; //從api獲取體溫

    private String firstday = "";   //這個月第一天
    private String lastday = "";    //這個月最後一天
    private String firstMonth= "";  //這個月份

    private TextView oveuSetting; //經期設定click
    private TextView oveuEdit;    //經期編輯click

    private TextView temperature;
    private TextView ovulResult;

    //圖表
    LineChart lineChart;
    private TextView periodRangDate;
    private ImageView preMonth, nextMonth;

    private ArrayList<Menstruation> menstruationArray;
    private Menstruation menstruation;

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

        menstruationPeriodDay = findViewById(R.id.tv_ovul_period); //今天是週期的第?天

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

        //月曆
        calendarView = findViewById(R.id.calendar);
        YearMonthTv = findViewById(R.id.main_YYMM_Tv);
        //月曆樣式
        setCalendar();

        //圖表
        lineChart = findViewById(R.id.lineChart);
        periodRangDate = findViewById(R.id.tvMMDD);   //經期範圍
        preMonth = findViewById(R.id.imgPreMonth);    //前一個月
        nextMonth = findViewById(R.id.imgNextMonth);  //後一個月
        preMonth.setOnClickListener(this);
        nextMonth.setOnClickListener(this);

        //月曆區先顯示
        calendar.setBackgroundResource(R.drawable.rectangle_button);
        calendar.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
        calendarLayout.setVisibility(View.VISIBLE);
        scrollViewLayout.setVisibility(View.VISIBLE);
    }

    //圖表資料顯示
    private void initChartData() {

        getThisMonth();

        periodRangDate.setText(firstday + " ~ " + lastday); //經期顯示期間

        menstruationArray = new ArrayList<>();

        String myJSONStr = loadJSONFromAsset("menstruation_12.json");
        try {
            JSONObject obj = new JSONObject(myJSONStr);
            String status = obj.getString("status");
            if (status.equals("Success")) {
                JSONArray array = obj.getJSONArray("data");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject objdata = array.getJSONObject(i);
                    periodDate = objdata.getString("testDate");      //日期(X軸)
                    periodDegree = objdata.getString("temperature"); //體溫(y軸)

                    menstruation = new Menstruation(); //實體化

                    //時間:年月日
                    String[] str = periodDate.split("/");
                    String day = str[2];  //只需日期當X軸
                    menstruation.setTestDate(day);
                    menstruation.setTemperature(Double.parseDouble(periodDegree));
                    menstruationArray.add(menstruation);

                    ArrayList<String> label = new ArrayList<>();    //X軸(時間)
                    ArrayList<Entry> entries = new ArrayList<>();   //Y軸(體溫)

                    for (int j = 0; j < menstruationArray.size(); j++ ) {
                        String xValues = menstruationArray.get(j).getTestDate();
                        double yValues = menstruationArray.get(j).getTemperature();
                        entries.add(new Entry(j, (float) yValues));
                        label.add(xValues);

                        LineDataSet lineDataSet = new LineDataSet(entries, "");
                        lineDataSet.setColor(Color.RED);  //軸線顏色
                        LineData data = new LineData(lineDataSet);
                        lineChart.setData(data);

                        XAxis xAxis = lineChart.getXAxis(); //取得X軸
                        xAxis.setValueFormatter(new IndexAxisValueFormatter(label)); //x軸放入自定義的時間
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); //日期顯示在底層
                        xAxis.setGranularity(1f);                      //x軸最小間隔
                        xAxis.setLabelCount(label.size());             //X軸的數量來自資料集
                        xAxis.setGridColor(Color.BLACK);               //設置X軸的網格線的顏色
                        xAxis.setAxisLineColor(Color.BLACK);
                        xAxis.enableGridDashedLine(10f, 10f, 0f); //X軸格線虛線

                        YAxis rightAxis = lineChart.getAxisRight();         //獲取右側的Y軸
                        rightAxis.setEnabled(false);                        //不顯示右側Y軸
                        YAxis leftAxis = lineChart.getAxisLeft();           //獲取左側的Y軸線
                        leftAxis.setDrawGridLines(false);                   //隱藏Y軸的格線

                        leftAxis.setLabelCount(7);    //體溫最多7階
                        xAxis.setLabelCount(10);      //日期做多10階

                        lineChart.getLegend().setEnabled(false);            //隱藏圖例
                        lineChart.getDescription().setEnabled(false);       //隱藏描述
                        lineChart.invalidate();                             //重新刷圖表
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //圖表日期顯示範圍(一整個月)
    private void getThisMonth() {
        Calendar cale = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MM/dd" );
        SimpleDateFormat format1 = new SimpleDateFormat("MM");

        // 獲取前月的第一天
        cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 0);
        cale.set(Calendar.DAY_OF_MONTH, 1);
        firstday = format.format(cale.getTime());
        firstMonth = format1.format(cale.getTime());

        // 獲取前月的最後一天
        cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 1);
        cale.set(Calendar.DAY_OF_MONTH, 0);
        lastday = format.format(cale.getTime());

    }

    //月曆init
    private void setCalendar() {

        //取得這個月的第一天與最後一天
        getThisMonth();

        //從json讀取資料
        gatDataFromJson();

        //計算週期
//        calculationPeriod();

        //set 月曆月份Title
        YearMonthTv.setText(Calendar.getInstance().get(Calendar.YEAR) + "年" + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "月");

        //監聽日期
        calendarView.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onDateClick(View view, DateData date) {
                choseDay = String.format("%d/%d/%d", date.getYear(), date.getMonth(), date.getDay());
                Toast.makeText(OvulationActivity.this, "您選擇的日期為 : " + choseDay, Toast.LENGTH_SHORT).show();
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

    }

    //從Api or Local取得需要的資料集
    private void gatDataFromJson() {

        String calendStr = loadJSONFromAsset("menstruation.json");

//        menstruation = Menstruation.newInstance(calendStr);
//        menstruationArray.add(menstruation);

        try {
            JSONObject obj = new JSONObject(calendStr);
            String status = obj.getString("status");
            if (status.equals("Success")) {
                JSONArray array = obj.getJSONArray("data");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject objdata = array.getJSONObject(i);
                    periodDate = objdata.getString("testDate");      //日期
                    periodStatus = objdata.getString("cycleStatus"); //狀態
                    periodDegree = objdata.getString("temperature"); //體溫

                    //時間:年月日
                    String[] str = periodDate.split("/");
                    int yaer = Integer.parseInt(str[0]);    //ex:2020
                    int month = Integer.parseInt(str[1]);   //ex:12
                    int day = Integer.parseInt(str[2]);     //ex:25

                    if (periodStatus.equals("1")){ //月經日
                        calendarView.markDate(
                                new DateData(yaer, month, day).setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.rgb(207,97,148))));
                    }

                    if (periodStatus.equals("2")){  //排卵日
                        calendarView.markDate(
                                new DateData(yaer, month, day).setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.parseColor("#D5AD45"))));
                    }

                    if(periodStatus.equals("5")){  //預計經期
                        calendarView.markDate(
                                new DateData(yaer, month, day).setMarkStyle(new MarkStyle(MarkStyle.PREIOD, Color.rgb(207,97,148))));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //讀取local json file
    public String loadJSONFromAsset(String fileName)
    {
        String json;
        try
        {
            InputStream is = getApplicationContext().getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex)
        {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSwitchCalendar: //月曆
                calendar.setBackgroundResource(R.drawable.rectangle_button);
                chart.setBackgroundResource(R.drawable.relative_shape);
                calendar.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
                chart.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.black));
                calendarLayout.setVisibility(View.VISIBLE);
                scrollViewLayout.setVisibility(View.VISIBLE);
                chartLayout.setVisibility(View.GONE);
                break;
            case R.id.btnSwitchChart:  //圖表
                calendar.setBackgroundResource(R.drawable.relative_shape);
                chart.setBackgroundResource(R.drawable.rectangle_button);
                calendar.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.black));
                chart.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
                calendarLayout.setVisibility(View.GONE);
                scrollViewLayout.setVisibility(View.GONE);
                chartLayout.setVisibility(View.VISIBLE);
                initChartData(); //獲取數據
                break;
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.tv_ovul_setting:  //經期設定
                dialogPickDate();
                break;
            case R.id.tv_ovul_edit:     //經期編輯
                periodEdit(choseDay);
                break;
            case R.id.imgPreMonth:    //上一個月
                Toast.makeText(this, "還沒寫好", Toast.LENGTH_SHORT).show();
                break;
            case R.id.imgNextMonth:   //下一個月
                Toast.makeText(this, "沒有資料", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void periodEdit(String strDay) {
        //今天日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

        if (strDay == null){
            strDay = sdf.format(new Date());
        }


        Intent intent = new Intent();
        intent.setClass(OvulationActivity.this, PeriodActivity.class);
        intent.putExtra("DAY", strDay);
        startActivity(intent);
    }

    //經期設定對話框
    private void dialogPickDate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        final View view = inflater.inflate(R.layout.dialog_datepicker,null);
        builder.setView(view);

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
