package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import sun.bob.mcalendarview.MCalendarView;
import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.listeners.OnDateClickListener;
import sun.bob.mcalendarview.vo.DateData;

public class PregnancyActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = PregnancyActivity.class.getSimpleName();

    private Button prenatalRecord, pregnancyRecord;
    private TextView result;

    MCalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregnancy);

        initView();

        calendarView = ((MCalendarView) findViewById(R.id.calendarView));
        ArrayList<DateData> dates = new ArrayList<>();
        dates.add(new DateData(2020,10,23).setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.BLUE)));
        dates.add(new DateData(2020,10,24).setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.BLUE)));
        dates.add(new DateData(2020,10,25).setMarkStyle(new MarkStyle(MarkStyle.BACKGROUND, Color.BLUE)));
        for(int i = 0; i < dates.size(); i++) {
            calendarView.markDate(dates.get(i).getYear(),dates.get(i).getMonth(),dates.get(i).getDay());//mark multiple dates with this code.
        }

        calendarView.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onDateClick(View view, DateData date) {
                String MonthStr = date.getMonthString();
                String DayStr = date.getDayString();
//                Log.d(TAG, "你選的日期是 " + MonthStr + "月" + DayStr + "日");
                result.setText(MonthStr + "月" + DayStr + "日");
            }
        });

    }

    private void initView() {
        prenatalRecord = (Button) findViewById(R.id.bt_preg_prenatal);
        pregnancyRecord = (Button) findViewById(R.id.bt_preg_pregnancy);
        result = findViewById(R.id.tv_pregnancy_result);

        prenatalRecord.setOnClickListener(this);
        pregnancyRecord.setOnClickListener(this);

        prenatalRecord.setBackgroundResource(R.drawable.rectangle_button);  //先顯示產檢紀錄Button
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.bt_preg_prenatal:
                prenatalRecord.setBackgroundResource(R.drawable.rectangle_button);
                pregnancyRecord.setBackgroundResource(R.drawable.relative_shape);
                break;
            case R.id.bt_preg_pregnancy:
                prenatalRecord.setBackgroundResource(R.drawable.relative_shape);
                pregnancyRecord.setBackgroundResource(R.drawable.rectangle_button);
                break;
        }
    }

}
