package com.example.yhyhealthy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthy.adapter.FunctionsAdapter;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import es.dmoral.toasty.Toasty;
import ru.slybeaver.slycalendarview.SlyCalendarDialog;

/**   ***************
 * 歷史紀錄首頁
 * 目前只會有四個功能
 * create 2021/05/21
 * ******************* **/

public class RecordFragment extends Fragment implements View.OnClickListener, FunctionsAdapter.OnRecycleItemClickListener {

    private static final String TAG = RecordFragment.class.getSimpleName();

    private View view;

    private Button btnFunction;
    private Button btnDate;

    private List<String> fxnList;

    private TextView textHint;   //提示文字

    private String startDay = "";   //開始日
    private String endDay = "";     //結束日

    private RecyclerView recordResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;

        view = inflater.inflate(R.layout.fragment_record, container, false);

        textHint = view.findViewById(R.id.tvSelectFunDate);

        btnFunction = view.findViewById(R.id.btSelectFuns);
        btnDate = view.findViewById(R.id.btSelectDate);

        btnFunction.setOnClickListener(this);
        btnDate.setOnClickListener(this);

        recordResult = view.findViewById(R.id.rvRecordResult);

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
            if(fxnList.isEmpty()){
                Toasty.warning(getActivity(), getString(R.string.history_functions_less_one) , Toast.LENGTH_SHORT, true).show();
            }
        });

        dialogBuilder.setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.dismiss());

        AlertDialog alert = dialogBuilder.create();
        alert.setCanceledOnTouchOutside(false); //dismiss the dialog with click on outside of the dialog
        alert.show();

        //Button內的英文小寫
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

                    DateTime dtFirst = new DateTime(firstDate);
                    startDay = dtFirst.toString("yyyy-MM-dd");
                    endDay = dtFirst.toString("yyyy-MM-dd");
                }else{ //多日
//                    SelectDate = getString(
//                            R.string.period,
//                            new SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault()).format(firstDate.getTime()),
//                            new SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault()).format(secondDate.getTime())
//                    );

                    DateTime dtFirst = new DateTime(firstDate);
                    DateTime dtSecond = new DateTime(secondDate);
                    startDay = dtFirst.toString("yyyy-MM-dd");
                    endDay = dtSecond.toString("yyyy-MM-dd");
                }
            }
            //提示文字隱藏
            textHint.setVisibility(View.INVISIBLE);
            recordResult.setVisibility(View.VISIBLE);
            setData();
        }
    };

    //執行
    private void setData() {
        if (null != fxnList && !fxnList.isEmpty()){ //將功能跟日期傳給adapter
            FunctionsAdapter functionsAdapter = new FunctionsAdapter(getActivity(), startDay, endDay, fxnList,this);
            recordResult.setHasFixedSize(true);
            recordResult.setAdapter(functionsAdapter);
            recordResult.setLayoutManager(new LinearLayoutManager(getActivity()));
            recordResult.addItemDecoration(new SpacesItemDecoration(20));
        }else { //功能不得空白
            Toasty.error(getActivity(), getString(R.string.function_is_not_allow_empty), Toast.LENGTH_SHORT,true).show();
        }
    }


    @Override
    public void onClick(int functionName, String startDay, String endDay) {

//        Intent intent = new Intent();
//        Bundle bundle = new Bundle();
//        bundle.putString("startDay", startDay);
//        bundle.putString("endDay", endDay);

        switch (functionName){
            case 0:
            case 1:
                Toasty.info(getContext(), getString(R.string.fxn_is_coming_soon), Toast.LENGTH_SHORT,true).show();
                //intent = new Intent(getActivity(),OvulationRecordActivity.class);
                break;
            //intent = new Intent(getActivity(), TempRecordActivity.class);
        }
//        intent.putExtras(bundle);
//        startActivity(intent);
    }
}
