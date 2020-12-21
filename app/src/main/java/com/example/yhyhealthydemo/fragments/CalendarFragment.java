package com.example.yhyhealthydemo.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.yhyhealthydemo.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.listeners.OnDateClickListener;
import sun.bob.mcalendarview.listeners.OnMonthChangeListener;
import sun.bob.mcalendarview.views.ExpCalendarView;
import sun.bob.mcalendarview.vo.DateData;

/***********************************
 * 排卵月曆Page
 * 月曆引用第三方庫 : mCalendarView
* *********************************/

public class CalendarFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = CalendarFragment.class.getSimpleName();

    private View view;

    //月曆
    private ExpCalendarView calendarView;
    private TextView YearMonthTv;

    private TextView oveuSetting; //經期設定click

    private TextView temperature;
    private TextView ovulResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;
        view = inflater.inflate(R.layout.fragment_calendar, container, false);

        ovulResult = view.findViewById(R.id.tv_ovul_result_1);
        ovulResult.setText("唾液辨識 : 濾泡/黃體期");

        temperature = view.findViewById(R.id.tv_ovul_temp_1);
        temperature.setText("基礎體溫 : 36.55" + "\u2103 ");

        calendarView = ((ExpCalendarView) view.findViewById(R.id.calendar));
        YearMonthTv = (TextView) view.findViewById(R.id.main_YYMM_Tv);
        //set 月曆月份Title
        YearMonthTv.setText(Calendar.getInstance().get(Calendar.YEAR) + "年" + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "月");
        //監聽日期
        calendarView.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onDateClick(View view, DateData date) {
                Toast.makeText(getActivity(), "您選擇的日期為 : "+String.format("%d/%d", date.getMonth(), date.getDay()), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "您選擇的日期為 : "+String.format("%d/%d", date.getMonth(), date.getDay()), Toast.LENGTH_SHORT).show();
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
        //今天的背景顏色
        calendarView.markDate(new DateData(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE))
                .setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.RED)));

        //mark special day
        ArrayList<DateData> dates = new ArrayList<>();
        dates.add(new DateData(2020,10,21).setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.BLUE)));
        dates.add(new DateData(2020,10,22).setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.BLUE)));
        dates.add(new DateData(2020,10,23).setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.BLUE)));
        dates.add(new DateData(2020,10,24).setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.BLUE)));
        for(int i = 0; i < dates.size(); i++) {
            calendarView.markDate(dates.get(i).getYear(),dates.get(i).getMonth(),dates.get(i).getDay());//mark multiple dates with this code.
        }

        //經期設定init
        oveuSetting = view.findViewById(R.id.tv_ovul_setting);
        oveuSetting.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_ovul_setting:
                dialogPickDate(); //經期設定對話框
                break;
        }
    }

    //經期設定對話框
    private void dialogPickDate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
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
                Toast.makeText(getActivity(), "您設定的範圍為: " + ToDate + "~" + FromDate, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
