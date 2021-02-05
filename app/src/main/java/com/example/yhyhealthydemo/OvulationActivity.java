package com.example.yhyhealthydemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
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
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthydemo.datebase.CycleRecord;
import com.example.yhyhealthydemo.datebase.Menstruation;
import com.example.yhyhealthydemo.datebase.MenstruationRecord;
import com.example.yhyhealthydemo.module.ApiProxy;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.joda.time.DateTime;
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

public class OvulationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "OvulationActivity";

    private LinearLayout calendarLayout, chartLayout;
    private ScrollView scrollViewLayout;
    private Button calendar, chart;

    //月曆
    private ExpCalendarView calendarView;
    private TextView YearMonthTv;
    private TextView menstruationPeriodDay; //週期顯示
    private String periodDate = "";     //從api獲取日期
    private String periodStatus = "";   //從api獲取狀態
    private String periodDegree = "";   //從api獲取體溫
    private String onClickDay = "";

    private String firstday = "";   //這個月第一天
    private String lastday = "";    //這個月最後一天
    private String firstMonth= "";  //這個月份

    private TextView oveuSetting; //經期設定click
    private TextView oveuEdit;    //經期編輯click

    private TextView temperature;
    private TextView ovulResult;

    private RatingBar bodySalivaRate, bodyDegreeRate;

    private SimpleDateFormat sdf;  //日期格式
    private DateData selectedDate;

    //圖表
    LineChart lineChart;
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

        //時間格式
        sdf = new SimpleDateFormat("yyyy-MM-dd");

        initData(); //初始化

        initView();
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
        preMonth.setOnClickListener(this);            //前一個月
        nextMonth.setOnClickListener(this);           //後一個月

        //月曆區先顯示
        calendar.setBackgroundResource(R.drawable.rectangle_button);
        calendar.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
        calendarLayout.setVisibility(View.VISIBLE);
        scrollViewLayout.setVisibility(View.VISIBLE);
    }

    //圖表資料顯示
    private void initChartData() {

        getThisMonth();  //取得當月份的第一天與最後一天日期

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

        //set 月曆月份Title
        YearMonthTv.setText(Calendar.getInstance().get(Calendar.YEAR) + "年" + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "月");

        //calendarView.getMarkedDates().removeAdd(); //清除掉之前餘留的MarkedDate

        //2021/02/05
        setCycleRecord();

        DateTime today = new DateTime(new Date());  //今天
        String todayStr = today.toString("yyyy-MM-dd");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4*1000);
                    //api查詢本日是否有資料
                    checkDataFromApi(todayStr);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //監聽日期
        calendarView.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onDateClick(View view, DateData date) {
                calendarView.getMarkedDates().removeAdd();
                calendarView.markDate(date);
                selectedDate = date;

//                //今天日期Mark
//                calendarView.markDate(
//                        new DateData(date.getYear(), date.getMonth(), date.getDay()).setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.rgb(255,0,0))));

                String choseDay = String.format("%d-%d-%d", date.getYear(), date.getMonth(), date.getDay());

                Toasty.info(OvulationActivity.this, "您選擇的日期為" + choseDay, Toast.LENGTH_SHORT,true).show();

                //使用者選擇的日期若是未來日期則禁用編輯紀錄和經期設定
                try {
                    Date toDay = new Date();               //Today
                    Date selectDay = sdf.parse(choseDay);  //user select day
                    if(toDay.before(selectDay)){
                        oveuSetting.setEnabled(false);
                        oveuEdit.setEnabled(false);
                    }else {
                        oveuSetting.setEnabled(true);
                        oveuEdit.setEnabled(true);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //轉換日期格式
                try {
                    Date date1 = sdf.parse(choseDay);
                    onClickDay = sdf.format(date1);
                    checkDataFromApi(onClickDay); //當使用者自己選擇日期時則向後台Api詢問是否有資料
                } catch (ParseException e) {
                    e.printStackTrace();
                }

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

//        selectedDate = new DateData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
//        calendarView.markDate(selectedDate);
    }

    //去跟後台要單一日的排卵資料
    private void checkDataFromApi(String day) {
        JSONObject json = new JSONObject();
        try {
            json.put("testDate", day);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //排卵資料後端
        Log.d(TAG, "排卵資料後端Api: " + json.toString());
        proxy.buildPOST(RECORD_INFO, json.toString(), requestListener);
    }

    //排卵資料監聽
    private ApiProxy.OnApiListener requestListener = new ApiProxy.OnApiListener() {
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
    private void parserJson(JSONObject result) {
        record = MenstruationRecord.newInstance(result.toString());
        //唾液辨識結果
        String paramName = record.getSuccess().getMeasure().getParamName();
        if (!paramName.equals("")){
            oveuEdit.setText("編輯\n紀錄");
            if (paramName.equals("Ovulation")){
                ovulResult.setText(getString(R.string.param_name) + " 排卵期");
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

    private void periodEdit(String strDay) {
        //如果使用者沒有選擇任何一天就點擊編輯經期按鈕,其日期則以今天為主
        if (strDay.equals("")){
            strDay = sdf.format(new Date());
        }

        //將所選擇的日期帶到PeriodActivity頁面
        Intent intent = new Intent();
        intent.setClass(OvulationActivity.this, PeriodRecordActivity.class);
        intent.putExtra("DAY", strDay);
        startActivity(intent);
    }

    //經期設定對話框
    private void showPeriod(String clickDay) {
        //如果使用者沒有選擇任何一天就點擊經期設定按鈕,其開始日期則以今天為主
        if (clickDay.equals("")){
            clickDay = sdf.format(new Date());
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
                //參數 startDate & endDate
                JSONObject json = new JSONObject();
                try {
                    json.put("startDate", toDate.getText().toString());
                    json.put("endDate", fromDate.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                proxy.buildPOST(PERIOD_DELETE, json.toString(), deletePeriodListener);
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

    //後端資料填滿月曆
    private void setCycleRecord() {
        JSONObject json = new JSONObject();
        try {
            json.put("startDate", "2021-01-31");
            json.put("endDate","2021-03-06");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "setCycleRecord: " + json.toString());
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
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(result.toString());
                        int errorCode = jsonObject.getInt("errorCode");
                        if (errorCode == 0){
                            parserCycleData(result);
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

    //解析週期資料
    private void parserCycleData(JSONObject result) {
        cycleRecord = CycleRecord.newInstance(result.toString());
        for (int i = 0; i < cycleRecord.getSuccess().size(); i ++){


        }

    }

}
