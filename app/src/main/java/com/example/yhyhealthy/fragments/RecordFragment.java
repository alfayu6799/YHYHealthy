package com.example.yhyhealthy.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.PatternMatcher;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthy.LoginActivity;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.TemperatureActivity;
import com.example.yhyhealthy.adapter.FunctionsAdapter;
import com.example.yhyhealthy.adapter.ObserverAdapter;
import com.example.yhyhealthy.datebase.TempDataApi;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import es.dmoral.toasty.Toasty;
import pl.droidsonroids.gif.GifImageView;
import ru.slybeaver.slycalendarview.SlyCalendarDialog;

import static com.example.yhyhealthy.module.ApiProxy.BLE_USER_LIST;
import static com.example.yhyhealthy.module.ApiProxy.HISTORY_RECORD;
import static com.example.yhyhealthy.module.ApiProxy.HISTORY_TARGET;

/**   ***************
 * 歷史紀錄首頁
 * 目前只會有四個功能
 * create 2021/05/21
 * ******************* **/

public class RecordFragment extends Fragment implements View.OnClickListener, ObserverAdapter.onItemClickListener {

    private static final String TAG = RecordFragment.class.getSimpleName();

    private View view;

    private Button    btnFunction;
    private Button    btnDate;
    private ImageView download;

    //private List<String> fxnList;

    private TextView textHint;   //提示文字

    private String startDay = "";   //開始日
    private String endDay = "";     //結束日

    private RecyclerView recordResult;

    private int selectObserverId;
    private String selectObserverName;

    //api
    private ApiProxy proxy;

    //進度條
    private ProgressDialog progressDialog;

    //背景動畫
    private GifImageView gifImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;

        view = inflater.inflate(R.layout.fragment_record, container, false);

        textHint = view.findViewById(R.id.tvSelectFunDate);

        btnFunction = view.findViewById(R.id.btSelectFuns);
        btnDate = view.findViewById(R.id.btSelectDate);
        download = view.findViewById(R.id.ivDownload);

        //動畫background
        gifImageView = view.findViewById(R.id.game_gif);
        gifImageView.setBackgroundResource(R.mipmap.yhy_new_background);

        btnFunction.setOnClickListener(this);
        btnDate.setOnClickListener(this);
        download.setOnClickListener(this);

        recordResult = view.findViewById(R.id.rvRecordResult);

        //初始化觀測者資料
        initObserver();

        //使用者資訊按鈕提示
        btnFunction.setBackgroundResource(R.drawable.rectangle_button);
        btnFunction.setTextColor(getResources().getColor(R.color.white));

        return view;
    }

    //初始化觀測者資料
    private void initObserver() {
        proxy = ApiProxy.getInstance();
        proxy.buildPOST(HISTORY_TARGET, "" ,observerListListener);
    }

    private ApiProxy.OnApiListener observerListListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(getActivity(), getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            parser(result);
                        }else if (errorCode == 6){
                            Toasty.error(getActivity(), getString(R.string.no_date), Toast.LENGTH_SHORT, true).show();
                        }else if (errorCode == 23) {  //token失效
                            Toasty.error(getActivity(), getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(getActivity(), LoginActivity.class)); //重新登入
                            getActivity().finish();
                        }else if (errorCode == 31){
                            Toasty.error(getActivity(), getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(getActivity(), LoginActivity.class)); //重新登入
                            getActivity().finish();
                        }else {
                            Toasty.error(getActivity(), getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //解析後台回來的觀測者資料
    private void parser(JSONObject result) {
        TempDataApi tempDataApi = TempDataApi.newInstance(result.toString());
        List<TempDataApi.SuccessBean> dataList = tempDataApi.getSuccess();

        //資料配置到adapter
        ObserverAdapter adapter = new ObserverAdapter(getActivity(), dataList, RecordFragment.this);
        recordResult.setAdapter(adapter);
        recordResult.setLayoutManager(new LinearLayoutManager(getActivity()));
        recordResult.setHasFixedSize(true);
        recordResult.addItemDecoration(new SpacesItemDecoration(10));
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.btSelectFuns:  //觀測者選擇
                btnFunction.setBackgroundResource(R.drawable.rectangle_button);
                btnFunction.setTextColor(getResources().getColor(R.color.white));
                btnDate.setBackgroundResource(R.drawable.relative_shape);
                btnDate.setTextColor(getResources().getColor(R.color.black));
                getObserverInfoApi();      //取得此帳號底下觀測者List 2021/06/17
                //dialogSelectFunction();  //彈跳視窗for功能選擇
                break;
            case R.id.btSelectDate:  //日期範圍
                btnFunction.setBackgroundResource(R.drawable.relative_shape);
                btnFunction.setTextColor(getResources().getColor(R.color.black));
                btnDate.setBackgroundResource(R.drawable.rectangle_button);
                btnDate.setTextColor(getResources().getColor(R.color.white));
                checkObserverOK();        //檢查是否已先擇觀測者在進行日期選擇
                //selectDateRange();     //彈跳視窗for日期範圍
                break;
            case R.id.ivDownload:  //下載歷史紀錄 2021/08/04
                showDownloadDialog();
                break;
        }
    }

    //下載歷史紀錄 輸入email
    private void showDownloadDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater  = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_download, null);
        alertDialog.setView(view);
        alertDialog.show();

        EditText emailAddress = view.findViewById(R.id.edtMailDownload);
        Button   cancel = view.findViewById(R.id.btnCancelDownload);
        Button   download = view.findViewById(R.id.btnSureDownload);

        //取消
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        //確定
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailToText = emailAddress.getText().toString();
                if (TextUtils.isEmpty(emailToText) || !emailToText.contains("@")){
                    Toasty.error(getContext(), getString(R.string.email_address_error), Toast.LENGTH_SHORT, true).show();
                    return;
                }

                //將mail address上傳到後台
                sendToApi();
                //關閉視窗
                alertDialog.dismiss();
            }
        });
    }

    //將mail address上傳到後台 2021/08/05
    private void sendToApi() {
        //需要後台api串接
        Toasty.success(getContext(), getString(R.string.update_success), Toast.LENGTH_SHORT,true).show();
    }

    //檢查是否已先擇觀測者在進行日期選擇
    private void checkObserverOK() {
        if (TextUtils.isEmpty(selectObserverName)){
            Toasty.info(getActivity(), getString(R.string.please_select_fun_and_date), Toast.LENGTH_SHORT, true).show();
        }else { //進行日期選擇
//            Toasty.info(getActivity(),getString(R.string.you_are_chose_observer_is) + selectObserver, Toast.LENGTH_SHORT,true).show();
            selectDateRange();
        }
    }

    //取得此帳號底下觀測者List
    private void getObserverInfoApi() {
        initObserver();
        textHint.setVisibility(View.INVISIBLE);   //提示文字隱藏
        recordResult.setVisibility(View.VISIBLE); //recyclerView顯示
    }

    /**********************
     * 功能彈跳視窗 Dialog
     * *********************/
    /*
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
    */
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
            updateToApi();  //選好日期後將資料往後端傳
        }
    };

    //將查詢用的資料傳至後台請求資料
    private void updateToApi() {
        if (!TextUtils.isEmpty(startDay) && (!TextUtils.isEmpty(endDay))) {
            //將參數傳給後台並取資料回來
//            Log.d(TAG, "updateToApi: startDay:" + startDay + ",endDay" + endDay + ",targetId:" + selectObserverId);
            getObserverInfoFromApi(startDay, endDay, selectObserverId, selectObserverName);
        }else {
            Toasty.info(getActivity(), getString(R.string.please_select_observer_date), Toast.LENGTH_SHORT, true).show();
        }
    }

    //將參數傳給後台並取資料回來
    private void getObserverInfoFromApi(String startDay, String endDay, int targetId, String UserName){
        recordResult.setVisibility(View.GONE);
        JSONObject json = new JSONObject();
        try {
            json.put("targetId", targetId);
            json.put("startDate", startDay);
            json.put("endDate",endDay);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildPOST(HISTORY_RECORD, json.toString(), getInfoListener);
    }

    private ApiProxy.OnApiListener getInfoListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(getActivity(), getString(R.string.title_process), getString(R.string.process), true);
            }else {
                progressDialog.show();
            }
        }

        @Override
        public void onSuccess(JSONObject result) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            parserHistoryById(result);  //解析後台回復的資料
                        }else if (errorCode == 23) {  //token失效
                            Toasty.error(getActivity(), getString(R.string.request_failure), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(getActivity(), LoginActivity.class)); //重新登入
                            getActivity().finish();
                        }else if (errorCode == 31){ //登入重複
                            Toasty.error(getActivity(), getString(R.string.login_duplicate), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(getActivity(), LoginActivity.class)); //重新登入
                            getActivity().finish();
                        }else {
                            Toasty.error(getActivity(), getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //
    private void parserHistoryById(JSONObject result) {
        Log.d(TAG, "歷史資料後台回: " + result.toString());
    }

    @Override
    public void onItemClick(TempDataApi.SuccessBean data) {
        selectObserverId = data.getTargetId();   //使用者targetId
        selectObserverName = data.getUserName(); //使用者名稱
    }


}
