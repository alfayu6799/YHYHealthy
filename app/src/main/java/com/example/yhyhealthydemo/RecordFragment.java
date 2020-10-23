package com.example.yhyhealthydemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import ru.slybeaver.slycalendarview.SlyCalendarDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = RecordFragment.class.getSimpleName();

    private View view;

    private Button select_function;
    private Button select_date;
    private Button result_start;

    private List<String> fxnList;

    private TextView select_title;   //提示文字
    private TextView date_range;     //日期範圍結果文字顯示
    private TextView fun_result;     //功能選擇結果文字顯示

    private RecyclerView recyclerView;

    private String data = ""; //功能選擇的資料字串

    public RecordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;

        view = inflater.inflate(R.layout.fragment_record, container, false);

        select_title = view.findViewById(R.id.tv_select_fun_date);
        fun_result = view.findViewById(R.id.tv_select_function);
        date_range = view.findViewById(R.id.tv_date_range);
        result_start = view.findViewById(R.id.bt_result_start);

        recyclerView = view.findViewById(R.id.recyclerView_record);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        recyclerView.setAdapter(adapter);

        select_function = view.findViewById(R.id.bt_select_fun);
        select_date = view.findViewById(R.id.bt_select_date);

        select_function.setOnClickListener(this);
        select_date.setOnClickListener(this);
        result_start.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.bt_select_fun:
                dialogSelectFunction();  //彈跳視窗for功能選擇
                break;
            case R.id.bt_select_date:
                selectDateRange();     //彈跳視窗for日期範圍
                break;
            case R.id.bt_result_start:
                resultList();          //顯示結果(RecyclerView)
                break;
        }
    }

    /**********************************
     * 設備+日期以RecyclerView方式呈現
     * *******************************/
    private void resultList() {
        Toast.makeText(getActivity(), "查詢開始!", Toast.LENGTH_SHORT).show();
    }

    /**********************
     * 功能彈跳視窗 Dialog
     * *********************/
    private void dialogSelectFunction(){
        fxnList = new ArrayList<>();
        final String array[] = getActivity().getResources().getStringArray(R.array.select_function);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle("請選擇功能:");
        dialogBuilder.setMultiChoiceItems(R.array.select_function, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                if(isChecked){
                    // If the user checked the item, add it to the selected items
                    fxnList.add(array[which]);
                }else{
                    // Else, if the item is already in the array, remove it
                    fxnList.remove(array[which]);
                }
            }
        });

        // Set the action buttons
        dialogBuilder.setPositiveButton("確定", (dialog, which) ->{
            data = fxnList.toString().replace("[", "").replace("]", "");
            if (data.equals("")){
                Toast.makeText(getActivity(), "請務必選擇功能!!" , Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity(), "您選擇的功能有:" + data, Toast.LENGTH_SHORT).show();
                fun_result.setText(getActivity().getString(R.string.select_function) + " " +data);
                //要把勾選的資料存儲供RecyclerView使用?
            }
        });

        dialogBuilder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        AlertDialog alert = dialogBuilder.create();
        alert.setCanceledOnTouchOutside(false); //dismiss the dialog with click on outside of the dialog
        alert.show();
    }

    /*********************************
     * 日期彈跳視窗 Dialog採用第三方庫件
     * *******************************/
    private void selectDateRange(){
        new SlyCalendarDialog()
                .setSingle(false)
                .setCallback(listener)
                .show(getActivity().getSupportFragmentManager(), "TAG_SLYCALENDAR");
    }

    //使用第三方套件:slycalendarview
    SlyCalendarDialog.Callback listener = new SlyCalendarDialog.Callback(){

        @Override
        public void onCancelled() {

        }

        @Override
        public void onDataSelected(Calendar firstDate, Calendar secondDate, int hours, int minutes) {
            if (firstDate != null) {
                if (secondDate == null) { //單一日
                    firstDate.set(Calendar.HOUR_OF_DAY, hours);
//                    firstDate.set(Calendar.MINUTE, minutes);

                    String oneDate = new SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault()).format(firstDate.getTime());
                    date_range.setText("範圍:" + oneDate); //顯示所選擇的日期區間
                }else{
                    String SelectDate = getString(
                            R.string.period,
                            new SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault()).format(firstDate.getTime()),
                            new SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault()).format(secondDate.getTime())
                    );
                    //顯示所選擇的日期區間
                    date_range.setText("範圍: " + SelectDate);
                }
            }
            //提示文字隱藏
            select_title.setVisibility(View.INVISIBLE);
            result_start.setVisibility(View.VISIBLE);
        }
    };


}
