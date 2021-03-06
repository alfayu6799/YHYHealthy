package com.example.yhyhealthy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
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
import com.example.yhyhealthy.calendar.MyEventDecorator;
import com.example.yhyhealthy.calendar.OneDayDecorator;
import com.example.yhyhealthy.datebase.CycleRecord;
import com.example.yhyhealthy.datebase.MenstruationRecord;
import com.example.yhyhealthy.datebase.PeriodData;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.MPAChartManager;
import com.example.yhyhealthy.tools.Math;
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
import pl.droidsonroids.gif.GifImageView;

import static com.example.yhyhealthy.module.ApiProxy.CYCLE_RECORD;
import static com.example.yhyhealthy.module.ApiProxy.MENSTRUAL_RECORD_INFO;
import static com.example.yhyhealthy.module.ApiProxy.PERIOD_DELETE;
import static com.example.yhyhealthy.module.ApiProxy.PERIOD_UPDATE;
import static com.example.yhyhealthy.module.ApiProxy.RECORD_INFO;
import static com.example.yhyhealthy.module.ApiProxy.authToken;
import static com.example.yhyhealthy.module.ApiProxy.scepterToken;

public class OvulationActivity extends AppCompatActivity implements View.OnClickListener, OnDateSelectedListener {

    private static final String TAG = "OvulationActivity";

    private LinearLayout chartLayout;
    private ScrollView scrollViewLayout;
    private Button calendar, chart;

    //??????
    private MaterialCalendarView widget;
    private OneDayDecorator oneDayDecorator;
    private TextView menstruationPeriodDay;        //????????????TextView
    private String onClickDay;
    private String firstDayOfThisMonth;            //?????????????????????
    private String lastDayOfThisMonth;             //????????????????????????

    private Button btnSetting; //????????????click
    private Button btnEdit;    //????????????click

    private TextView textDegreeResult;
    private TextView textMenstruationResult;

    private RatingBar bodySalivaRate, bodyDegreeRate;

    private List<CycleRecord.SuccessBean> dataList;
    private Math math;

    //??????
    private CombinedChart combinedChart;     //?????????+?????????
    private TextView periodRangDate;        //??????????????????
    private ImageView preMonth, nextMonth;  //?????????&?????????

    //api
    private MenstruationRecord record; //dataBean
    private ApiProxy proxy;
    private CycleRecord cycleRecord;
    private PeriodData period;

    //Other
    private ProgressDialog progressDialog;
    private AlertDialog dialog;
    private static final int PERIOD_RECORD = 1;
    private String beginPeriodDay;   //???????????????
    private int periodLength;  //????????????
    private List<firstDayOfPeriod> firstDayOfPeriodList = new ArrayList<>(); //?????????????????????

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ovulation1);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //????????????

        //????????????
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initData(); //?????????

        initView();

        //??????????????? 2021/02/25
        initCalendar();
    }

    //?????????dataBean & api
    private void initData() {
        record = new MenstruationRecord();
        cycleRecord = new CycleRecord();
        proxy = ApiProxy.getInstance();

        firstDayOfThisMonth = String.valueOf(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).plusDays(-15)); //????????????????????????????????????5???
        lastDayOfThisMonth = String.valueOf(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(5));    //?????????+5???

        oneDayDecorator = new OneDayDecorator(this);
    }

    private void initView() {
        calendar = findViewById(R.id.btnSwitchCalendar);
        chart = findViewById(R.id.btnSwitchChart);
        scrollViewLayout = findViewById(R.id.lyScrollView);
        chartLayout = findViewById(R.id.lyChart);

        menstruationPeriodDay = findViewById(R.id.tvShowPeriodDay); //?????????????????????????
        textMenstruationResult = findViewById(R.id.tvIdResult);        //????????????Result
        textDegreeResult = findViewById(R.id.tvDegreeResult);       //????????????
        bodySalivaRate = findViewById(R.id.rtSaliva);         //??????????????????
        bodyDegreeRate = findViewById(R.id.rtBt);              //??????????????????

        btnSetting = findViewById(R.id.btnPeriodSetting);  //????????????
        btnEdit = findViewById(R.id.btnPeriodEdit);        //????????????

        calendar.setOnClickListener(this);               //??????
        chart.setOnClickListener(this);                  //??????
        btnSetting.setOnClickListener(this);            //????????????
        btnEdit.setOnClickListener(this);               //????????????

        //???????????? 2021/02/25
        widget = findViewById(R.id.calendar);
        widget.setOnDateChangedListener(this);
        widget.setShowOtherDates(MaterialCalendarView.SHOW_ALL);
        final LocalDate instance = LocalDate.now();
        widget.setSelectedDate(instance);

        //??????
        combinedChart = findViewById(R.id.chart);
        periodRangDate = findViewById(R.id.tvMMDD);   //????????????
        preMonth = findViewById(R.id.imgPreMonth);    //????????????
        nextMonth = findViewById(R.id.imgNextMonth);  //????????????
        preMonth.setOnClickListener(this);            //????????????
        nextMonth.setOnClickListener(this);           //????????????

        //??????????????????
        calendar.setBackgroundResource(R.drawable.rectangle_button);
        calendar.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
        scrollViewLayout.setVisibility(View.VISIBLE);
    }

    //???????????????
    private void initCalendar() {

        widget.addDecorator(oneDayDecorator); //??????Text???????????????

        //???????????? (?????????&?????????)
        setCycleRecord(firstDayOfThisMonth, lastDayOfThisMonth);

        //????????????????????????????????? 2021/02/25
        checkTodayInfo(String.valueOf(LocalDate.now()));

        //??????????????????
        monthListener();

        //??????????????????????????????????????????(????????????????????????????????????) 2021/05/26
        checkPeriodRecordInfo();
    }

    //??????????????????
    private void monthListener() {

        widget.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                String firstDay = String.valueOf(LocalDate.from(date.getDate()).with(TemporalAdjusters.firstDayOfMonth()).plusDays(-5));
                String lastDay = String.valueOf(LocalDate.from(date.getDate()).with(TemporalAdjusters.lastDayOfMonth()).plusDays(+5));
                firstDayOfThisMonth = firstDay;
                lastDayOfThisMonth = lastDay;

                setCycleRecord(firstDayOfThisMonth, lastDayOfThisMonth);  //read ????????????
                widget.removeDecorators();            //???????????????makerDay???????????????????????????
                widget.addDecorator(oneDayDecorator); //????????????
            }
        });

    }

    //???????????????????????????
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
                        }else if (errorCode == 23) {  //token??????
                            Toasty.error(OvulationActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(OvulationActivity.this, LoginActivity.class)); //????????????
                            finish();
                        }else if (errorCode == 31){  //??????????????????
                            Toasty.error(OvulationActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(OvulationActivity.this, LoginActivity.class)); //????????????
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

    /** ?????????????????????????????????????????????????????????????????? **/
    /**??????????????? : ?????????????????? ???????????? ?????????????????? ?????????????????? */
    @SuppressLint("SetTextI18n")
    private void parserJson(JSONObject result) {
        record = MenstruationRecord.newInstance(result.toString());
        //Log.d(TAG, "parserJson: " + record.toJSONString());
        //??????????????????
        String paramName = record.getSuccess().getMeasure().getParamName();
        if (!paramName.equals("")){
            btnEdit.setText(R.string.edit_cycle);
            if (paramName.equals("Ovulation")){
                textMenstruationResult.setText(getString(R.string.in_period));
            }else if(paramName.equals("General")){
                textMenstruationResult.setText(getString(R.string.non_period));
            }else if(paramName.equals("FollicularORLutealPhase")) {      //?????????
                textMenstruationResult.setText(getString(R.string.in_low_cell));
                textMenstruationResult.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_FollicularORLutealPhase));
            }else if (paramName.equals("HighFollicularORLutealPhase")){   //?????????
                textMenstruationResult.setText(getString(R.string.in_high_cell));
                textMenstruationResult.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.font_HighFollicularORLutealPhase));
            }else if(paramName.equals("Unrecognizable")){
                textMenstruationResult.setText(getString(R.string.unknow));
            }else if (paramName.equals("SalivaTooThick")){ //???????????? 2021/08/05
                textMenstruationResult.setText(R.string.saliva_too_thick);
            }else if (paramName.equals("SalivaWet")){       //???????????? 2021/08/06
                textMenstruationResult.setText(R.string.saliva_wet);
            }else if (paramName.equals("BubblesExcessive")){   //???????????? 2021/08/06
                textMenstruationResult.setText(R.string.bubbles_excessive);
            }else if (paramName.equals("Insufficient")){  //????????????
                textMenstruationResult.setText(R.string.insufficient);
            }else if (paramName.equals("Brightness")){  //????????????
                textMenstruationResult.setText(R.string.brightness);
            }
        }else {
            btnEdit.setText(R.string.add_cycle);
            textMenstruationResult.setText("");
        }

        //????????????
        String bodyDegree = String.valueOf(record.getSuccess().getMeasure().getTemperature());
        textDegreeResult.setText(bodyDegree + " \u2103");

        //????????????????????????????????????
        int salivaRate = record.getSuccess().getOvuRate().getSalivaRate();
        bodySalivaRate.setRating(salivaRate);

        //???????????????????????????????????????
        int btRate = record.getSuccess().getOvuRate().getBtRate();
        bodyDegreeRate.setRating(btRate);
    }

    //????????????
    private void periodEdit(String strDay) {
        //??????????????????????????????????????????????????????????????????,???????????????????????????
        if (strDay == null){
            strDay = String.valueOf(LocalDate.now()); //??????
        }

        //???????????????????????????PeriodActivity??????
        Intent intent = new Intent();
        intent.setClass(OvulationActivity.this, PeriodRecordActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("DAY" , strDay);
        intent.putExtras(bundle);
        startActivityForResult(intent, PERIOD_RECORD);
    }

    //?????????????????????
    private void showPeriod(String clickDay) {
        //??????????????????????????????????????????????????????????????????,?????????????????????????????????
        if (clickDay == null){
            clickDay = String.valueOf(LocalDate.now()); //??????
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        final View view = inflater.inflate(R.layout.dialog_datepicker,null);
        builder.setView(view);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        EditText toDate = view.findViewById(R.id.et_to_date);     //????????????
        EditText fromDate = view.findViewById(R.id.et_from_date); //????????????

        toDate.setText(clickDay);        //?????????????????????????????????
        DateTime startDay = new DateTime(clickDay); //ex : 2021-02-03T00:00:00.000Z

        //????????????????????????????????????????????????????????????
        DateTime endDay = startDay.plusDays(periodLength);
        fromDate.setText(endDay.toString("yyyy-MM-dd"));  //????????????????????????

        //Button's onClick
        Button btnSave = view.findViewById(R.id.btnDateSave);     //??????onClick
        Button btnCancel = view.findViewById(R.id.btnDateCancel); //??????onClick
        Button btnDelete = view.findViewById(R.id.btnDateDelete); //??????onClick

        dialog = builder.create();

        //?????????
        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        //?????????
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

        //??????onClick
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //??????onClick
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //?????????????????????????????????????????????  2021/06/21 ??????
                DateTime dt1 = new DateTime(toDate.getText().toString());
                boolean b1 = dt1.isBeforeNow();
                if (b1) {  //??????????????????
                    //?????? startDate & endDate
                    JSONObject json = new JSONObject();
                    try {
                        json.put("startDate", toDate.getText().toString());  //???????????????
                        json.put("endDate", fromDate.getText().toString());  //??????????????????
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    proxy.buildPOST(PERIOD_UPDATE, json.toString(), periodListener);
                }else { //?????????????????????
                    Toasty.error(OvulationActivity.this, getString(R.string.you_cant_chose_future), Toast.LENGTH_SHORT, true).show();
                }
            }
        });

        //??????onClick
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //??????????????????????????????api????????????????????????????????????????????????????????????  2021/05/26 ??????
//                if(!toDate.getText().toString().equals(beginPeriodDay)){
//                    Toasty.error(OvulationActivity.this, getString(R.string.please_chose_really_day), Toast.LENGTH_SHORT,true).show();
//                }else {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("startDate", toDate.getText().toString());
                        json.put("endDate", fromDate.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "Delete: " + json.toString());
                    proxy.buildPOST(PERIOD_DELETE, json.toString(), deletePeriodListener);
//                }
            }
        });

        dialog.show();
    }

    //???????????????????????????????????????
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
                    //2021/05/26 ?????????????????????????????????
                    periodLength = period.getSuccess().getPeriod() -1;
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

    //????????????Api
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
                if(errorCode == 0){
                    Toasty.success(OvulationActivity.this,getString(R.string.delete_success), Toast.LENGTH_SHORT, true).show();

                    //?????????????????????????????????mark
                    widget.removeDecorators();

                    //????????????
                    initCalendar();

                    //???????????????
                    dialog.dismiss();
                }else if (errorCode == 23) { //token??????
                    Toasty.error(OvulationActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                    startActivity(new Intent(OvulationActivity.this, LoginActivity.class)); //????????????
                    finish();
                }else if (errorCode == 31){
                    Toasty.error(OvulationActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                    startActivity(new Intent(OvulationActivity.this, LoginActivity.class)); //????????????
                    finish();
                }else {
                    runOnUiThread(new Runnable() {  //??????crash
                        @Override
                        public void run() {
                            Toasty.error(OvulationActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
                            dialog.dismiss();
                        }
                    });
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

    //????????????Api
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

                            //?????????????????????????????????mark
                            widget.removeDecorators();

                            //????????????
                            initCalendar();

                            //???????????????
                            dialog.dismiss();
                        }else if (errorCode == 23) { //token??????
                            Toasty.error(OvulationActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(OvulationActivity.this, LoginActivity.class)); //????????????
                            finish();
                        }else if (errorCode == 31){ //????????????
                            Toasty.error(OvulationActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(OvulationActivity.this, LoginActivity.class)); //????????????
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

    //?????????????????????????????????
    private void setCycleRecord(String startDay, String endDay) {

        JSONObject json = new JSONObject();
        try {
            json.put("startDate", startDay);
            json.put("endDate", endDay);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.d(TAG, "??????????????????????????????: " + json.toString());
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
                            parserCycleData(result); //??????????????????
                        }else if (errorCode == 23) {  //token??????
                            Toasty.error(OvulationActivity.this, getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(OvulationActivity.this, LoginActivity.class)); //????????????
                            finish();
                        }else if (errorCode == 31){
                            Toasty.error(OvulationActivity.this, getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(OvulationActivity.this, LoginActivity.class)); //????????????
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

        //????????????????????????????????? 2021/02/22
        @SuppressLint("UseCompatLoadingForDrawables")
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void parserCycleData(JSONObject result) {
            cycleRecord = CycleRecord.newInstance(result.toString());
            //Log.d(TAG, "parserCycleData: " + result.toString());

            dataList = cycleRecord.getSuccess(); //????????????

            DateTime dt = new DateTime(); //??????

            for (int i = 0; i < dataList.size(); i++) {

                math = new Math(this, dataList.get(i));

                //??????
                if (math.getCalenderDrawable() != null)
                    widget.addDecorator(new MyEventDecorator(math.getCalenderDrawable(), Collections.singletonList(math.getDateData())));

                //?????????????????????????????????,???????????????????????????FirstDay?????????(?????????????????????????????????????????????????????????)
                if (dataList.get(i).getTestDate().equals(dt.toString("yyyy-MM-dd"))){
                    //????????????????????????????????? 2021/07/28
                    showCyclePeriodDay(dataList.get(i).getFirstDay());
                }

                //??????????????????????????????
                if (dataList.get(i).getFirstDay() == 1 ){
                    beginPeriodDay = dataList.get(i).getTestDate();
                    LocalDate myDay = LocalDate.parse(beginPeriodDay).minusDays(1); //????????????
                    beginPeriodDay = myDay.toString();
                    //Log.d(TAG, "??????Api?????????1???????????????:" + beginPeriodDay);
                }

                //2021/07/27
                firstDayOfPeriodList.add(new firstDayOfPeriod(dataList.get(i).getTestDate(), dataList.get(i).getFirstDay()));
            }

            //???????????????
            MPAChartManager chartManager = new MPAChartManager(this, combinedChart);
            chartManager.showCombinedChart(dataList);
        }

    //2021/07/28 ?????????????????????????????????
    @SuppressLint("SetTextI18n")
    private void showCyclePeriodDay(int firstDay) {

        int numsOfDay = 0;
        int numOfDay = firstDay + 1;

        if (!TextUtils.isEmpty(onClickDay)) {
            for (int i = 0; i < firstDayOfPeriodList.size(); i++) {
                if (firstDayOfPeriodList.get(i).testDate.contains(onClickDay)) {
                    int nums = firstDayOfPeriodList.get(i).firstDay;
                    numsOfDay = nums + 1;
                }
            }
            menstruationPeriodDay.setText(getString(R.string.this_period_day) + " " + numsOfDay + " " + getString(R.string.day));
        }else {
            menstruationPeriodDay.setText(getString(R.string.period_day) + " " + numOfDay + " " + getString(R.string.day));
        }
    }

    //??????????????????????????? 2021/02/25
    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        LocalDate choseDay = LocalDate.from(date.getDate());
        Toasty.info(OvulationActivity.this, getString(R.string.you_are_chose_day_is) + choseDay, Toast.LENGTH_SHORT,true).show();
        oneDayDecorator.setDate(date.getDate());
        widget.invalidateDecorators();  //????????????

        //???????????????????????????????????????????????????
        checkTodayInfo(String.valueOf(choseDay));

        if(choseDay.equals(LocalDate.now())){ //??????????????????????????????????????????
            btnSetting.setEnabled(true);
            btnEdit.setEnabled(true);
        }else{
            checkEditEnable(choseDay); //???????????????????????????????????????????????????????????????
        }

        //???????????????????????????????????????onClick
        onClickDay = String.valueOf(choseDay);

        //?????????????????????????????????????????????????????????? 2021/03/02
        checkPeriodDayOfThisMonth(choseDay);
    }

    //??????????????????????????????????????????????????????? 2021/07/26
    @SuppressLint("SetTextI18n")
    private void checkPeriodDayOfThisMonth(LocalDate choseDay) {
        int numOfDays = 0;

        for (int i = 0; i < firstDayOfPeriodList.size(); i++){
            if (firstDayOfPeriodList.get(i).testDate.equals(choseDay.toString())){
                numOfDays = firstDayOfPeriodList.get(i).firstDay + 1;  //????????????+1???
            }
        }
        menstruationPeriodDay.setText(getString(R.string.this_period_day) + " " + numOfDays + " " + getString(R.string.day));
    }

    //????????????????????????
    private void checkEditEnable(LocalDate choseDay) {
        boolean flag = LocalDate.now().isAfter(choseDay);
        btnSetting.setEnabled(flag);
        btnEdit.setEnabled(flag);
    }


    //?????????????????? 2021/03/05 redesign
    @SuppressLint("SetTextI18n")
    private void initChartData() {
        //??????????????????
        periodRangDate.setText(firstDayOfThisMonth + " ~ " + lastDayOfThisMonth);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSwitchCalendar: //??????
                calendar.setBackgroundResource(R.drawable.rectangle_button);
                chart.setBackgroundResource(R.drawable.relative_shape);
                calendar.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
                chart.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.color_font));
                scrollViewLayout.setVisibility(View.VISIBLE);
                chartLayout.setVisibility(View.GONE);
                break;
            case R.id.btnSwitchChart:  //??????
                calendar.setBackgroundResource(R.drawable.relative_shape);
                chart.setBackgroundResource(R.drawable.rectangle_button);
                calendar.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.color_font));
                chart.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
                scrollViewLayout.setVisibility(View.GONE);
                chartLayout.setVisibility(View.VISIBLE);
                initChartData(); //????????????
                break;
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnPeriodSetting:  //????????????
                showPeriod(onClickDay); //????????????:2021-01-04
                break;
            case R.id.btnPeriodEdit:     //????????????
                periodEdit(onClickDay);  //????????????:2021-01-04
                break;
            case R.id.imgPreMonth:    //????????????
                PreMonthListener();
                break;
            case R.id.imgNextMonth:   //????????????
                nextMonthListener();
                break;
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void nextMonthListener() { //?????????
        String endNextMonth = String.valueOf(LocalDate.parse(lastDayOfThisMonth).plusDays(30));
        String startNextMonth = String.valueOf(LocalDate.parse(endNextMonth).plusDays(-40));
        lastDayOfThisMonth = endNextMonth;
        firstDayOfThisMonth = startNextMonth;
        initChartData();    //??????????????????
        widget.goToNext();  //???????????????
    }

    private void PreMonthListener() { //?????????
        String startLastMonth = String.valueOf(LocalDate.parse(firstDayOfThisMonth).plusDays(-30));
        String endLastMonth = String.valueOf(LocalDate.parse(startLastMonth).plusDays(40));
        firstDayOfThisMonth = startLastMonth;
        lastDayOfThisMonth = endLastMonth;
        initChartData();         //??????????????????
        widget.goToPrevious();  //???????????????
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

    //???????????????model
    public class firstDayOfPeriod {
            String testDate;
            int    firstDay;

        public firstDayOfPeriod(String testDate, int firstDay) {
            this.testDate = testDate;
            this.firstDay = firstDay;
        }
    }
}
