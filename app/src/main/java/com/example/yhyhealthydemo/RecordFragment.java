package com.example.yhyhealthydemo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

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
import es.dmoral.toasty.Toasty;
import ru.slybeaver.slycalendarview.SlyCalendarDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = RecordFragment.class.getSimpleName();

    private View view;

    private Button btnFunction;
    private Button btnDate;

    private List<String> fxnList;

    private TextView textHint;   //提示文字

    private String data = ""; //功能選擇的資料字串

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;

        view = inflater.inflate(R.layout.fragment_record1, container, false);

        textHint = view.findViewById(R.id.tvSelectFunDate);

        btnFunction = view.findViewById(R.id.btSelectFuns);
        btnDate = view.findViewById(R.id.btSelectDate);

        btnFunction.setOnClickListener(this);
        btnDate.setOnClickListener(this);

        return view;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.btSelectFuns:  //功能選擇
                btnFunction.setBackgroundResource(R.drawable.rectangle_button);
                btnDate.setBackgroundResource(R.drawable.relative_shape);
                dialogSelectFunction();  //彈跳視窗for功能選擇
                break;
            case R.id.btSelectDate:  //日期範圍
                btnFunction.setBackgroundResource(R.drawable.relative_shape);
                btnDate.setBackgroundResource(R.drawable.rectangle_button);
                selectDateRange();     //彈跳視窗for日期範圍
                break;
        }
    }

    /**********************
     * 功能彈跳視窗 Dialog
     * *********************/
    private void dialogSelectFunction(){
        fxnList = new ArrayList<>();
        //資料來源
        final String array[] = getActivity().getResources().getStringArray(R.array.select_function);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.please_chose_less_one);
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
        dialogBuilder.setPositiveButton(getString(R.string.history_dialog_confirm), (dialog, which) ->{
            data = fxnList.toString().replace("[", "").replace("]", "");
            if (data.equals("")){
                Toasty.warning(getActivity(), getString(R.string.history_functions_less_one) , Toast.LENGTH_SHORT, true).show();
            }else{
//                Toast.makeText(getActivity(), "您選擇的功能有:" + data, Toast.LENGTH_SHORT).show();

            }
        });

        dialogBuilder.setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.dismiss());

        AlertDialog alert = dialogBuilder.create();
        alert.setCanceledOnTouchOutside(false); //dismiss the dialog with click on outside of the dialog
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
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
                    String oneDate = new SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault()).format(firstDate.getTime());
                    //date_range.setText("範圍:" + oneDate); //顯示所選擇的日期區間
                }else{
                    String SelectDate = getString(
                            R.string.period,
                            new SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault()).format(firstDate.getTime()),
                            new SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault()).format(secondDate.getTime())
                    );
                    //顯示所選擇的日期區間
                    Log.d(TAG, "onDataSelected: " + SelectDate);
                }
            }
            //提示文字隱藏
            textHint.setVisibility(View.INVISIBLE);
        }
    };

}
