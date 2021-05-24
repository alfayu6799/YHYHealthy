package com.example.yhyhealthydemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.yhyhealthydemo.calendar.MyEventDecorator;
import com.example.yhyhealthydemo.calendar.OneDayDecorator;
import com.example.yhyhealthydemo.datebase.CycleRecord;
import com.example.yhyhealthydemo.datebase.MenstruationRecord;
import com.example.yhyhealthydemo.datebase.PeriodData;
import com.example.yhyhealthydemo.module.ApiProxy;
import com.example.yhyhealthydemo.tools.MPAChartManager;
import com.example.yhyhealthydemo.tools.Math;
import com.github.mikephil.charting.charts.CombinedChart;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.temporal.TemporalAdjusters;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthydemo.module.ApiProxy.CYCLE_RECORD;
import static com.example.yhyhealthydemo.module.ApiProxy.MENSTRUAL_RECORD_INFO;
import static com.example.yhyhealthydemo.module.ApiProxy.PERIOD_DELETE;
import static com.example.yhyhealthydemo.module.ApiProxy.PERIOD_UPDATE;
import static com.example.yhyhealthydemo.module.ApiProxy.RECORD_INFO;

public class OvulationActivity extends AppCompatActivity implements View.OnClickListener, OnDateSelectedListener {

    private static final String TAG = "OvulationActivity";

    private LinearLayout chartLayout;
    private ScrollView scrollViewLayout;
    private Button calendar, chart;

    //月曆
    private MaterialCalendarView widget;
    private OneDayDecorator oneDayDecorator;
    private TextView menstruationPeriodDay;        //週期顯示TextView
    private String onClickDay;
    private String firstDayOfThisMonth;
    private String lastDayOfThisMonth;

    private Button btnSetting; //經期設定click
    private Button btnEdit;    //經期編輯click

    private TextView textDegreeResult;
    private TextView textMenstruationResult;

    private RatingBar bodySalivaRate, bodyDegreeRate;

    private List<CycleRecord.SuccessBean> dataList;
    private Math math;

    //圖表
    private CombinedChart combinedChart;     //折線圖+長條圖
    private TextView periodRangDate;        //查詢日期範圍
    private ImageView preMonth, nextMonth;  //上個月&下個月

    //api
    private MenstruationRecord record; //dataBean
    private ApiProxy proxy;
    private CycleRecord cycleRecord;
    private PeriodData period;

    //Other
    private ProgressDialog progressDialog;
    private AlertDialog dialog;
    private static final int PERIOD_RECORD = 1;
    private PopupWindow popupWindow;
    private LinearLayout linearLayout;

    //ble
    private yhyBleService mBluetoothLeService;

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

        firstDayOfThisMonth = String.valueOf(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).plusDays(-5)); //起始日-5天
        lastDayOfThisMonth = String.valueOf(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(5));    //結束日+5天

        oneDayDecorator = new OneDayDecorator(this);
    }

    private void initView() {
        calendar = findViewById(R.id.btnSwitchCalendar);
        chart = findViewById(R.id.btnSwitchChart);
        scrollViewLayout = findViewById(R.id.lyScrollView);
        chartLayout = findViewById(R.id.lyChart);

        menstruationPeriodDay = findViewById(R.id.tvShowPeriodDay); //今天是週期的第?天
        textMenstruationResult = findViewById(R.id.tvIdResult);        //唾液辨識Result
        textDegreeResult = findViewById(R.id.tvDegreeResult);       //基礎體溫
        bodySalivaRate = findViewById(R.id.rtSaliva);         //唾液辨識機率
        bodyDegreeRate = findViewById(R.id.rtBt);              //基礎體溫機率

        btnSetting = findViewById(R.id.btnPeriodSetting);  //經期設定
        btnEdit = findViewById(R.id.btnPeriodEdit);        //編輯紀錄

        calendar.setOnClickListener(this);               //月曆
        chart.setOnClickListener(this);                  //圖表
        btnSetting.setOnClickListener(this);            //經期設定
        btnEdit.setOnClickListener(this);               //編輯紀錄

        //月曆重構 2021/02/25
        widget = findViewById(R.id.calendar);
        widget.setOnDateChangedListener(this);
        widget.setShowOtherDates(MaterialCalendarView.SHOW_ALL);
        final LocalDate instance = LocalDate.now();
        widget.setSelectedDate(instance);

        //圖表
        combinedChart = findViewById(R.id.chart);
        periodRangDate = findViewById(R.id.tvMMDD);   //經期範圍
        preMonth = findViewById(R.id.imgPreMonth);    //前一個月
        nextMonth = findViewById(R.id.imgNextMonth);  //後一個月
        preMonth.setOnClickListener(this);            //前一個月
        nextMonth.setOnClickListener(this);           //後一個月

        //月曆區先顯示
        calendar.setBackgroundResource(R.drawable.rectangle_button);
        calendar.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
        scrollViewLayout.setVisibility(View.VISIBLE);
    }

    //初始化日曆
    private void initCalendar() {

        Log.d(TAG, "初始化日曆fxn: initCalendar");

//        widget.addDecorators(
//                new MySelectorDecorator(OvulationActivity.this), //點擊日期後的背景
//                oneDayDecorator
//        );

        widget.addDecorator(oneDayDecorator);

        //經期月曆 (起始日&結束日)
        setCycleRecord(firstDayOfThisMonth, lastDayOfThisMonth);

        //自動檢查今天是否有資料 2021/02/25
        checkTodayInfo(String.valueOf(LocalDate.now()));

        //顯示今天是周期的第幾天
        checkPeriodDayOfThisMonth(LocalDate.now());

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
                firstDayOfThisMonth = firstDay;
                lastDayOfThisMonth = lastDay;

                setCycleRecord(firstDayOfThisMonth, lastDayOfThisMonth);  //read 週期資料
                widget.removeDecorators();            //清掉所有的makerDay不然圖層會累積跑版
                widget.addDecorator(oneDayDecorator); //重新繪製
            }
        });

    }

    //檢查單日是否有資料
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
                            parserJson(result);
                        }else if (errorCode == 23){  //token失效
                            Toasty.error(OvulationActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(OvulationActivity.this, LoginActivity.class)); //重新登入
                            finish();
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
            btnEdit.setText(R.string.edit_cycle);
            if (paramName.equals("Ovulation")){
                textMenstruationResult.setText(getString(R.string.in_period));
            }else if(paramName.equals("General")){
                textMenstruationResult.setText(getString(R.string.non_period));
            }else if(paramName.equals("FollicularORLutealPhase")) {      //低濾泡
                textMenstruationResult.setText(getString(R.string.in_low_cell));
                textMenstruationResult.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_FollicularORLutealPhase));
            }else if (paramName.equals("HighFollicularORLutealPhase")){   //高濾泡
                textMenstruationResult.setText(getString(R.string.in_high_cell));
                textMenstruationResult.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_HighFollicularORLutealPhase));
            }else if(paramName.equals("Unrecognizable")){
                textMenstruationResult.setText(getString(R.string.unknow));
            }
        }else {
            btnEdit.setText(R.string.add_cycle);
            textMenstruationResult.setText("");
        }

        //基礎體溫
        String bodyDegree = String.valueOf(record.getSuccess().getMeasure().getTemperature());
        textDegreeResult.setText(bodyDegree + " \u2103");

        //根據唾液辨識結果得到機率
        int salivaRate = record.getSuccess().getOvuRate().getSalivaRate();
        bodySalivaRate.setRating(salivaRate);

        //根據基礎體溫結果得到的機率
        int btRate = record.getSuccess().getOvuRate().getBtRate();
        bodyDegreeRate.setRating(btRate);

    }

    //紀錄按鈕
    private void periodEdit(String strDay) {
        //如果使用者沒有選擇任何一天就點擊編輯經期按鈕,其日期則以今天為主
        if (strDay == null){
            strDay = String.valueOf(LocalDate.now()); //今天
        }

        //將所選擇的日期帶到PeriodActivity頁面
        Intent intent = new Intent();
        intent.setClass(OvulationActivity.this, PeriodRecordActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("DAY" , strDay);
        intent.putExtras(bundle);
        startActivityForResult(intent, PERIOD_RECORD);
    }

    //經期設定對話框
    private void showPeriod(String clickDay) {

        //如果使用者沒有選擇任何一天就點擊經期設定按鈕,其開始日期則以今天為主
        if (clickDay == null){
            clickDay = String.valueOf(LocalDate.now()); //今天
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

        //取得使用者經期天數及週期天數 2021/03/03
        checkPeriodRecordInfo();

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
                SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
                String startDate = pref.getString("BEGIN", "");
                String endDate = pref.getString("END", "");

                //使用者點擊的日期須與sharePref內的日期符合才可以砍掉經期
                if(!toDate.getText().toString().equals(startDate)){
                    Toasty.error(OvulationActivity.this, getString(R.string.please_chose_really_day) + startDate
                            + getString(R.string.delete_really_day), Toast.LENGTH_SHORT, true).show();
                }else {
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

    //查詢經期長度天數及週期天數
    private void checkPeriodRecordInfo() {
        proxy.buildPOST(MENSTRUAL_RECORD_INFO, "", periodDayListener);
    }

    private ApiProxy.OnApiListener periodDayListener = new ApiProxy.OnApiListener() {
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
                int errorCode = object.getInt("errorCode");
                if (errorCode == 0){
                    period = PeriodData.newInstance(result.toString());
                    //將資料寫入sharePref
                    SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
                    pref.edit().putInt("PERIOD", period.getSuccess().getPeriod()).apply();
                    pref.edit().putInt("CYCLE" , period.getSuccess().getCycle()).apply();
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
                int errorCode = object.getInt("errorCode");
                if(errorCode ==0){
                    Toasty.success(OvulationActivity.this,getString(R.string.delete_success), Toast.LENGTH_SHORT, true).show();

                    //砍掉SharedPreferences內的內容
                    deleteSharePref();

                    //重刷資料前先清除之前的mark
                    widget.removeDecorators();

                    //重刷資料
                    initCalendar();

                    //關閉對話框
                    dialog.dismiss();
                }else if (errorCode == 23){ //token失效
                    Toasty.error(OvulationActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                    startActivity(new Intent(OvulationActivity.this, LoginActivity.class)); //重新登入
                    finish();
                }else {
                    Toasty.error(OvulationActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //砍掉SharedPreferences內經期第一天與最後一天的內容
    private void deleteSharePref() {
        SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
        pref.edit().putString("BEGIN", "").apply();
        pref.edit().putString("END", "").apply();
    }

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
                        int errorCode = object.getInt("errorCode");
                        if(errorCode == 0){
                            Toasty.success(OvulationActivity.this,getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();

                            //重刷資料前先清除之前的mark
                            widget.removeDecorators();

                            //重刷資料
                            initCalendar();

                            //關閉對話框
                            dialog.dismiss();
                        }else if (errorCode == 23){ //token失效
                            Toasty.error(OvulationActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(OvulationActivity.this, LoginActivity.class)); //重新登入
                            finish();
                        }else {
                            Toasty.error(OvulationActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
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
        //Log.d(TAG, "startDate: " + startDay + " endDate:" + endDay);
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
                        if (errorCode == 0) {
                            parserCycleData(result); //解析週期資料
                        }else if (errorCode == 23){  //token失效
                            Toasty.error(OvulationActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(OvulationActivity.this, LoginActivity.class)); //重新登入
                            finish();
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

        List<String> list = new ArrayList<>(); //經期第一天陣列用

        dataList = cycleRecord.getSuccess(); //獲取數據

        for (int i = 0; i < dataList.size(); i ++){

            math = new Math(this, dataList.get(i));

            //月曆
            if (math.getCalenderDrawable() != null)
                widget.addDecorator(new MyEventDecorator(math.getCalenderDrawable(), Collections.singletonList(math.getDateData())));

            //經期第一天
            boolean isFirstDay = dataList.get(i).isFirstDay();
            if (isFirstDay){
                String day = dataList.get(i).getTestDate();
                list.add(day);
            }

        }

        //圖表初始化
        MPAChartManager chartManager = new MPAChartManager(this, combinedChart);
        chartManager.showCombinedChart(dataList);

        //經期第一天寫入sharePref
        if(list.get(0) != null){
            SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
            pref.edit().putString("BEGIN", list.get(0)).apply();
        }
    }

    //日期被選到時的動作 2021/02/25
    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        LocalDate choseDay = LocalDate.from(date.getDate());
        Toasty.info(OvulationActivity.this, getString(R.string.you_are_chose_day_is) + choseDay, Toast.LENGTH_SHORT,true).show();
        oneDayDecorator.setDate(date.getDate());
        widget.invalidateDecorators();  //重新繪製

        //根據使用者點擊的日期去跟後台要資料
        checkTodayInfo(String.valueOf(choseDay));

        if(choseDay.equals(LocalDate.now())){ //使用者點擊的日期與今天同一天
            btnSetting.setEnabled(true);
            btnEdit.setEnabled(true);
        }else{
            checkEditEnable(choseDay); //根據使用者點擊的日期去判斷經期設定是否禁止
        }

        //將使用者點擊的日期之值帶給onClick
        onClickDay = String.valueOf(choseDay);

        //根據使用者點擊的日期去計算週期是第幾天? 2021/03/02
        checkPeriodDayOfThisMonth(choseDay);
    }

    //週期第幾天Fxn 2021/03/02
    @SuppressLint("SetTextI18n")
    private void checkPeriodDayOfThisMonth(LocalDate choseDay) {
        String beginStr = getSharedPreferences("yhyHealthy", Context.MODE_PRIVATE).getString("BEGIN", "");

        if (TextUtils.isEmpty(beginStr)){
            menstruationPeriodDay.setText(getString(R.string.period_day_is_empty)); //週期沒有資料
        }else {
            LocalDate begin = LocalDate.parse(beginStr);
            long numOfDays = ChronoUnit.DAYS.between(begin, choseDay);
           if(numOfDays >= 0){
               numOfDays = numOfDays + 1;
               menstruationPeriodDay.setText(getString(R.string.period_day) + " " + numOfDays + " " + getString(R.string.day));
           }else {
               menstruationPeriodDay.setText(getString(R.string.period_day_out_order)); //超出本月份周期
           }
        }
    }

    //經期設定是否禁止
    private void checkEditEnable(LocalDate choseDay) {
        boolean flag = LocalDate.now().isAfter(choseDay);
        btnSetting.setEnabled(flag);
        btnEdit.setEnabled(flag);
    }


    //圖表資料顯示 2021/03/05 redesign
    @SuppressLint("SetTextI18n")
    private void initChartData() {
        //經期顯示期間
        periodRangDate.setText(firstDayOfThisMonth + " ~ " + lastDayOfThisMonth);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSwitchCalendar: //月曆
                calendar.setBackgroundResource(R.drawable.rectangle_button);
                chart.setBackgroundResource(R.drawable.relative_shape);
                calendar.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
                chart.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.black));
                scrollViewLayout.setVisibility(View.VISIBLE);
                chartLayout.setVisibility(View.GONE);
                break;
            case R.id.btnSwitchChart:  //圖表
                calendar.setBackgroundResource(R.drawable.relative_shape);
                chart.setBackgroundResource(R.drawable.rectangle_button);
                calendar.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.black));
                chart.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
                scrollViewLayout.setVisibility(View.GONE);
                chartLayout.setVisibility(View.VISIBLE);
                initChartData(); //獲取數據
                break;
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnPeriodSetting:  //經期設定
                showPeriod(onClickDay); //日期格式:2021-01-04
                break;
            case R.id.btnPeriodEdit:     //經期編輯
                periodEdit(onClickDay);  //日期格式:2021-01-04
                break;
            case R.id.imgPreMonth:    //上一個月
                PreMonthListener();
                break;
            case R.id.imgNextMonth:   //下一個月
                nextMonthListener();
                break;
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void nextMonthListener() { //下個月
        String endNextMonth = String.valueOf(LocalDate.parse(lastDayOfThisMonth).plusDays(30));
        String startNextMonth = String.valueOf(LocalDate.parse(endNextMonth).plusDays(-40));
        lastDayOfThisMonth = endNextMonth;
        firstDayOfThisMonth = startNextMonth;
        initChartData();    //圖表範圍顯示
        widget.goToNext();  //下個月月曆
    }

    private void PreMonthListener() { //上個月
        String startLastMonth = String.valueOf(LocalDate.parse(firstDayOfThisMonth).plusDays(-30));
        String endLastMonth = String.valueOf(LocalDate.parse(startLastMonth).plusDays(40));
        firstDayOfThisMonth = startLastMonth;
        lastDayOfThisMonth = endLastMonth;
        initChartData();         //圖表範圍顯示
        widget.goToPrevious();  //上個月月曆
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERIOD_RECORD && resultCode == -1){
            widget.removeDecorators();
            initCalendar();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
