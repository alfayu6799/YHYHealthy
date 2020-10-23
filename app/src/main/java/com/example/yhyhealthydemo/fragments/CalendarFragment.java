package com.example.yhyhealthydemo.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthydemo.R;
import com.skyhope.eventcalenderlibrary.CalenderEvent;
import com.skyhope.eventcalenderlibrary.listener.CalenderDayClickListener;
import com.skyhope.eventcalenderlibrary.model.DayContainerModel;
import com.skyhope.eventcalenderlibrary.model.Event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendarFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = CalendarFragment.class.getSimpleName();

    private View view;

    private CalenderEvent calenderEvent;
    private Event event;

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

        //經期設定init
        oveuSetting = view.findViewById(R.id.tv_ovul_setting);
        oveuSetting.setOnClickListener(this);

        //月曆套件採用第三方庫
        calenderEvent = view.findViewById(R.id.calender_event);
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.add(Calendar.DAY_OF_MONTH,1);
//        Event event = new Event(mCalendar.getTimeInMillis(), null, Color.RED);
//        calenderEvent.addEvent(event);
//        calenderEvent.initCalderItemClickCallback(new CalenderDayClickListener() {
//            @Override
//            public void onGetDay(DayContainerModel dayContainerModel) {
//                Log.d(TAG, "onGetDay: " + dayContainerModel.getDate());
//            }
//        });

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
        Button sendDate = view.findViewById(R.id.bt_send_date);
        toDate.setInputType(InputType.TYPE_NULL);               //防止軟鍵開啟
        fromDate.setInputType(InputType.TYPE_NULL);             //防止軟鍵開啟
        toDate.requestFocus();

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
