package com.example.yhyhealthydemo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthydemo.calendar.MyEventDecorator;
import com.example.yhyhealthydemo.calendar.MySelectorDecorator;
import com.example.yhyhealthydemo.calendar.OneDayDecorator;
import com.example.yhyhealthydemo.datebase.CycleRecord;
import com.example.yhyhealthydemo.datebase.Menstruation;
import com.example.yhyhealthydemo.datebase.MenstruationRecord;
import com.example.yhyhealthydemo.module.ApiProxy;
import com.example.yhyhealthydemo.tools.Math;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import es.dmoral.toasty.Toasty;
import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.listeners.OnDateClickListener;
import sun.bob.mcalendarview.listeners.OnMonthChangeListener;
import sun.bob.mcalendarview.views.ExpCalendarView;
import sun.bob.mcalendarview.vo.DateData;
import static com.example.yhyhealthydemo.module.ApiProxy.CYCLE_RECORD;
import static com.example.yhyhealthydemo.module.ApiProxy.PERIOD_DELETE;
import static com.example.yhyhealthydemo.module.ApiProxy.PERIOD_UPDATE;
import static com.example.yhyhealthydemo.module.ApiProxy.RECORD_INFO;

public class OvulationActivity extends AppCompatActivity implements View.OnClickListener, OnDateSelectedListener {

    private static final String TAG = "OvulationActivity";

    private LinearLayout calendarLayout, chartLayout;
    private ScrollView scrollViewLayout;
    private Button calendar, chart;

    //月曆
    private MaterialCalendarView widget;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    private TextView menstruationPeriodDay;        //週期顯示
    private String onClickDay = "";

    private String periodDate = "";     //從api獲取日期
    private String periodDegree = "";   //從api獲取體溫

    private TextView oveuSetting; //經期設定click
    private TextView oveuEdit;    //經期編輯click

    private TextView temperature;
    private TextView ovulResult;

    private RatingBar bodySalivaRate, bodyDegreeRate;

    //圖表
    private LineChart lineChart;
    private TextView periodRangDate;
    private ImageView preMonth, nextMonth;
    private ArrayList<Menstruation> menstruationArray;
    private Menstruation menstruation; //dataBean

    //api
    private MenstruationRecord record; //dataBean
    private ApiProxy proxy;
    private CycleRecord cycleRecord;

    private ProgressDialog progressDialog;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().hide(); //hide ActionBar
        setContentView(R.layout.activity_ovulation);

        //休眠禁止
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initData(); //初始化

        initView();

        //月曆初始化 2021/02/25
        initCalendar();
    }

    //初始化dataBean & api
    private void initData() {
        record = new MenstruationRecord();
        cycleRecord = new CycleRecord();
        proxy = ApiProxy.getInstance();
    }

    private void initView() {
        calendar = findViewById(R.id.btnSwitchCalendar);
        chart = findViewById(R.id.btnSwitchChart);
        calendarLayout = findViewById(R.id.ly_calender);
        scrollViewLayout = findViewById(R.id.ly_scroll_comment);
        chartLayout = findViewById(R.id.lychart);

        menstruationPeriodDay = findViewById(R.id.tv_ovul_period); //今天是週期的第?天
        ovulResult = findViewById(R.id.tv_ovul_result_1);  //唾液辨識Result
        temperature = findViewById(R.id.tv_ovul_temp_1);  //基礎體溫
        bodySalivaRate = findViewById(R.id.rtSaliva);         //唾液辨識機率
        bodyDegreeRate = findViewById(R.id.rtBt);         //基礎體溫機率

        oveuSetting = findViewById(R.id.tv_ovul_setting);  //經期設定
        oveuEdit = findViewById(R.id.tv_ovul_edit);        //編輯紀錄

        calendar.setOnClickListener(this);               //月曆
        chart.setOnClickListener(this);                  //圖表
        oveuSetting.setOnClickListener(this);            //經期設定
        oveuEdit.setOnClickListener(this);               //編輯紀錄

        //月曆重構 2021/02/25
        widget = findViewById(R.id.calendar);
        widget.setOnDateChangedListener(this);
        widget.setShowOtherDates(MaterialCalendarView.SHOW_ALL);
        final LocalDate instance = LocalDate.now();
        widget.setSelectedDate(instance);

        //圖表
        lineChart = findViewById(R.id.lineChart);
        periodRangDate = findViewById(R.id.tvMMDD);   //經期範圍
        preMonth = findViewById(R.id.imgPreMonth);    //前一個月
        nextMonth = findViewById(R.id.imgNextMonth);  //後一個月
        preMonth.setOnClickListener(this);            //前一個月
        nextMonth.setOnClickListener(this);           //後一個月

        //月曆區先顯示
        calendar.setBackgroundResource(R.drawable.rectangle_button);
        calendar.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
        calendarLayout.setVisibility(View.VISIBLE);
        scrollViewLayout.setVisibility(View.VISIBLE);
    }

    //初始化日曆
    private void initCalendar() {

        String firstDayOfThisMonth = String.valueOf(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).plusDays(-5)); //起始日-5天
        String lastDayOfThisMonth = String.valueOf(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(5));    //結束日+5天

        widget.addDecorators(
                new MySelectorDecorator(this), //點擊日期後的背景
                oneDayDecorator
        );

        //經期月曆 起始日&結束日 //資料龐大下日後另外開線程處理比較保險
        setCycleRecord(firstDayOfThisMonth, lastDayOfThisMonth);

        //檢查今天是否有資料 2021/02/25
        checkTodayInfo(String.valueOf(LocalDate.now()));

        //監聽月曆滑動
        monthListener();
    }

    //監聽月曆滑動
    private void monthListener() {

        widget.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                String firstDay = String.valueOf(LocalDate.from(date.getDate()).with(TemporalAdjusters.firstDayOfMonth()).plusDays(-5));
                String lastDay = String.valueOf(LocalDate.from(date.getDate()).with(TemporalAdjusters.lastDayOfMonth()).plusDays(+5));
                setCycleRecord(firstDay, lastDay);  //read 週期資料
                widget.removeDecorators();    //
            }
        });

    }

    //檢查今天是否有資料
    private void checkTodayInfo(String dayStr) {
        JSONObject json = new JSONObject();
        try {
            json.put("testDate", dayStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(RECORD_INFO, json.toString(), todayInfoListener);
    }

    private final ApiProxy.OnApiListener todayInfoListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(OvulationActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(result.toString());
                        int errorCode = jsonObject.getInt("errorCode");
                        if(errorCode == 0){
                            parserJson(result);  //2021/01/13 leona
                        }else {
                            Toasty.error(OvulationActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT,true).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {
            progressDialog.dismiss();
        }
    };

    /** 解析單一筆後台來的資料並顯示在月曆下的框框內 **/
    /**共四組資訊 : 唾液辨識結果 基礎體溫 唾液辨識機率 基礎體溫機率 */
    @SuppressLint("SetTextI18n")
    private void parserJson(JSONObject result) {
        record = MenstruationRecord.newInstance(result.toString());
        Log.d(TAG, "parserJson: " + record.toJSONString());
        //唾液辨識結果
        String paramName = record.getSuccess().getMeasure().getParamName();
        if (!paramName.equals("")){
            oveuEdit.setText("編輯\n紀錄");
            if (paramName.equals("Ovulation")){
                ovulResult.setText(getString(R.string.param_name) + " " + getString(R.string.in_period));
            }else if(paramName.equals("General")){
                ovulResult.setText(getString(R.string.param_name) + " " + getString(R.string.non_period));
            }else if(paramName.equals("FollicularORLutealPhase")){
                ovulResult.setText(getString(R.string.param_name) + " " + getString(R.string.in_cell));
            }else if(paramName.equals("Unrecognizable")){
                ovulResult.setText(getString(R.string.param_name) + " " + getString(R.string.unknow));
            }
        }else {
            oveuEdit.setText("新增\n紀錄");
            ovulResult.setText(getString(R.string.param_name) + "");
        }

        //基礎體溫
        String bodyDegree = String.valueOf(record.getSuccess().getMeasure().getTemperature());
        temperature.setText(getString(R.string.body_degree) + " " + bodyDegree + " \u2103");

        //根據唾液辨識結果得到機率
        int salivaRate = record.getSuccess().getOvuRate().getSalivaRate();
        bodySalivaRate.setRating(salivaRate);

        //根據基礎體溫結果得到的機率
        int btRate = record.getSuccess().getOvuRate().getBtRate();
        bodyDegreeRate.setRating(btRate);

        //自動判斷今天是經期的第幾天 2021/02/20
        String testDay = record.getSuccess().getTestDate();
        //經期第一天
        String beginStr = getSharedPreferences("yhyHealthy", Context.MODE_PRIVATE).getString("BEGIN", "");
        //經期長度
        int length = getSharedPreferences("yhyHealthy", Context.MODE_PRIVATE).getInt("CYCLE", 28);
        Log.d(TAG, "經期第一天: " + beginStr + " 使用者單擊的日期:" + testDay +" 長度:" + length);

        //2021/02/22
        if(TextUtils.isEmpty(beginStr)){
            menstruationPeriodDay.setText(getString(R.string.period_day_out_order));
        }else {
            DateTime begin = new DateTime(beginStr); //經期第一天
            DateTime end = new DateTime(testDay);    //使用者單擊的日期
            Period p = new Period(begin, end, PeriodType.days()); //今天與經期第一天比較
            int days = p.getDays() + 1;
            if (days > length || days == 0){
                menstruationPeriodDay.setText(getString(R.string.period_day_out_order));
            }else {
                menstruationPeriodDay.setText(getString(R.string.period_day) + days + getString(R.string.day));
            }
        }

    }

    //圖表資料顯示
    @SuppressLint("SetTextI18n")
    private void initChartData() {
        DateTime dt = new DateTime();

        String firstOfMonth = dt.dayOfMonth().withMinimumValue().toString("MM/dd"); //當月份第一天之月日
        String lastOfMonth = dt.dayOfMonth().withMaximumValue().toString("MM/dd");  //當月份最後一天之月日

        periodRangDate.setText(firstOfMonth + " ~ " + lastOfMonth); //經期顯示期間

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
                showPeriod(onClickDay); //日期格式:2021-01-04
                break;
            case R.id.tv_ovul_edit:     //經期編輯
                periodEdit(onClickDay);  //日期格式:2021-01-04
                break;
            case R.id.imgPreMonth:    //上一個月
                Toast.makeText(this, "還沒寫好", Toast.LENGTH_SHORT).show();
                break;
            case R.id.imgNextMonth:   //下一個月
                Toast.makeText(this, "沒有資料", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    //紀錄按鈕
    private void periodEdit(String strDay) {
        DateTime dt = new DateTime();
        //如果使用者沒有選擇任何一天就點擊編輯經期按鈕,其日期則以今天為主
        if (strDay.equals("")){
            strDay = new DateTime(new Date()).toString("yyyy-MM-dd"); //今天
        }

        //將所選擇的日期帶到PeriodActivity頁面
        Intent intent = new Intent();
        intent.setClass(OvulationActivity.this, PeriodRecordActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("DAY" , strDay);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    //經期設定對話框
    private void showPeriod(String clickDay) {
        DateTime dt = new DateTime();
        //如果使用者沒有選擇任何一天就點擊經期設定按鈕,其開始日期則以今天為主
        if (clickDay.equals("")){
            clickDay =  new DateTime(new Date()).toString("yyyy-MM-dd"); //今天
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        final View view = inflater.inflate(R.layout.dialog_datepicker,null);
        builder.setView(view);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        EditText toDate = view.findViewById(R.id.et_to_date);     //起始日期
        EditText fromDate = view.findViewById(R.id.et_from_date); //結束日期

        toDate.setText(clickDay);        //將使用者選擇的日期帶入,禁用自行輸入,可避免選擇未來日期
        DateTime startDay = new DateTime(clickDay); //ex : 2021-02-03T00:00:00.000Z

        //經由使用者輸入的經期長度自動計算結束日期
        int periodLength = getSharedPreferences("yhyHealthy", MODE_PRIVATE).getInt("PERIOD", 0) - 1;
        DateTime endDay = startDay.plusDays(periodLength);
        fromDate.setText(endDay.toString("yyyy-MM-dd"));  //自動計算結束日期

        //Button's onClick
        Button btnSave = view.findViewById(R.id.btnDateSave);     //更新onClick
        Button btnCancel = view.findViewById(R.id.btnDateCancel); //取消onClick
        Button btnDelete = view.findViewById(R.id.btnDateDelete); //刪除onClick

        dialog = builder.create();

        //結束日
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

        //取消onClick
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //更新onClick
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //將經期第一天寫入  2021/02/22
                SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
                pref.edit().putString("BEGIN", toDate.getText().toString()).apply();
                pref.edit().putString("END", fromDate.getText().toString()).apply();

                //參數 startDate & endDate
                JSONObject json = new JSONObject();
                try {
                    json.put("startDate", toDate.getText().toString());
                    json.put("endDate", fromDate.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                proxy.buildPOST(PERIOD_UPDATE, json.toString(), periodListener);
            }
        });

        //刪除onClick
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //將經期第一天砍掉 2021/02/22
                SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
                String startDate = pref.getString("BEGIN", "");
                String endDate = pref.getString("END", "");

                if(!toDate.getText().toString().equals(startDate)){
                    Toasty.error(OvulationActivity.this, getString(R.string.please_chose_really_day) + startDate
                            + getString(R.string.delete_really_day), Toast.LENGTH_SHORT, true).show();
                }else {
                    //參數 startDate & endDate
                    JSONObject json = new JSONObject();
                    try {
                        json.put("startDate", startDate);
                        json.put("endDate", endDate);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    proxy.buildPOST(PERIOD_DELETE, json.toString(), deletePeriodListener);
                }
            }
        });

        dialog.show();
    }

    //經期刪除Api
    private ApiProxy.OnApiListener deletePeriodListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(OvulationActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            try {
                JSONObject object = new JSONObject(result.toString());
                boolean success = object.getBoolean("success");
                if(success){
                    Toasty.success(OvulationActivity.this,getString(R.string.delete_success), Toast.LENGTH_SHORT, true).show();
                    //要砍掉 SharedPreferences內的內容
                    SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
                    pref.edit().putString("BEGIN", "").apply();
                    pref.edit().putString("END", "").apply();
//                    setCalendar(); //重刷資料
                    dialog.dismiss();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {
            progressDialog.dismiss();
        }
    };
    
    //經期更新Api
    private ApiProxy.OnApiListener periodListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(OvulationActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        boolean success = object.getBoolean("success");
                        if(success) {
                            Toasty.success(OvulationActivity.this,getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
//                            setCalendar(); //重刷資料
                            dialog.dismiss();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {
            progressDialog.dismiss();
        }
    };

    //跟後端要資料來填滿月曆
    private void setCycleRecord(String startDay, String endDay) {
        JSONObject json = new JSONObject();
        try {
            json.put("startDate", startDay);
            json.put("endDate", endDay);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.d(TAG, "搜尋範圍: " + json.toString());
        proxy.buildPOST(CYCLE_RECORD, json.toString(), cycleRecordListener);
    }

    private ApiProxy.OnApiListener cycleRecordListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(OvulationActivity.this, getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(result.toString());
                        int errorCode = jsonObject.getInt("errorCode");
                        if (errorCode == 0){
                            parserCycleData(result); //解析週期資料
                        }else {
                            Toasty.error(OvulationActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {
            progressDialog.dismiss();
        }
    };

    //解析後端回來的週期資料 2021/02/22
    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void parserCycleData(JSONObject result) {
        cycleRecord = CycleRecord.newInstance(result.toString());
        Log.d(TAG, "parserCycleData1: " + cycleRecord.toJSONString());

        List<CycleRecord.SuccessBean> dataList = cycleRecord.getSuccess();

        for (int i = 0; i < dataList.size(); i ++){

            Math math = new Math(this, dataList.get(i));

            if (math.getCalenderDrawable() != null){
                widget.addDecorator(new MyEventDecorator(math.getCalenderDrawable(), Collections.singletonList(math.getDateData())));
            }
        }

    }

    //日期被選到時的動作 2021/02/25
    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        LocalDate choseDay = LocalDate.from(date.getDate());
        Toasty.info(OvulationActivity.this, getString(R.string.you_are_chose_day_is) + choseDay, Toast.LENGTH_SHORT,true).show();
        oneDayDecorator.setDate(date.getDate());
        widget.invalidateDecorators();  //重新繪製

        checkTodayInfo(String.valueOf(choseDay)); //根據使用者點擊的日期去跟後台要資料
        if(choseDay.equals(LocalDate.now())){ //使用者點擊的日期與今天同一天
            oveuSetting.setEnabled(true);
            oveuEdit.setEnabled(true);
        }else{
            checkEditEnable(choseDay); //根據使用者點擊的日期去判斷經期設定是否禁止
        }

        //將使用者點擊的日期之值帶給onClick
        onClickDay = String.valueOf(choseDay);
    }

    //經期設定是否禁止
    private void checkEditEnable(LocalDate choseDay) {
        boolean flag = LocalDate.now().isAfter(choseDay);
        oveuSetting.setEnabled(flag);
        oveuEdit.setEnabled(flag);
    }


}
