package com.example.yhyhealthydemo.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yhyhealthydemo.R;
import com.example.yhyhealthydemo.datebase.TempDataApi;
import com.example.yhyhealthydemo.datebase.TemperatureData;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/***  *****************
 * 查詢觀測對象列表-配適器
 * 資料來源 : TemperatureData.SuccessBean & TemperatureData
 * 介面:
 *     onBleConnect:觀測者-藍芽連線
 *     onDelUser : 刪除觀測者
 *     onBleChart : 觀測者藍芽體溫即時圖表
 * create Date : 2021/03/20
 * ************************/

public class TemperMainAdapter extends RecyclerView.Adapter<TemperMainAdapter.ViewHolder>{
    private static final String TAG = "TemperMainAdapter";

    private Context context;

    private List<TempDataApi.SuccessBean> dataList;

    private TemperMainAdapter.TemperMainListener listener;

    //建構子
    public TemperMainAdapter(Context context, List<TempDataApi.SuccessBean> dataList, TemperMainListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
    }

    //更新項目
    public void updateItem(TempDataApi.SuccessBean data, int pos) {
        if (dataList.size() != 0) {
            dataList.set(pos, data);
            notifyItemChanged(pos);
        }
    }

    public void clear(TempDataApi.SuccessBean data) {
        Log.d(TAG, "adapter clear: " + data.getTargetId());
    }

    //斷線刷新 2021/04/28
    public void disconnectedDevice(String devMac, String devStatus, String devName){
        if(dataList.size() != 0){
            for(int i = 0; i < dataList.size(); i++){
                TempDataApi.SuccessBean data = dataList.get(i);
                if(!TextUtils.isEmpty(data.getMac())){
                    if(data.getMac().equals(devMac)){
                        data.setStatus(devName + " " + devStatus);
                        notifyItemChanged(i);
                    }
                }
            }
        }
    }

    //找出adapter內的user Name 2021/04/28
    public String findNameByMac(String bleMac){
        if (dataList.size() != 0){
            for(int i = 0; i < dataList.size();i++){
                TempDataApi.SuccessBean data = dataList.get(i);
                if(!TextUtils.isEmpty(data.getMac())){
                   if(data.getMac().equals(bleMac)){
                       return data.getUserName();
                   }
                }
            }
        }
            return null;
    }

    //找出adapter內的Device Name 2021/05/21
    public String findDeviceNameByMac(String bleMac){
        if (dataList.size() != 0){
            for(int i = 0; i < dataList.size();i++){
                TempDataApi.SuccessBean data = dataList.get(i);
                if(!TextUtils.isEmpty(data.getMac())){
                    if(data.getMac().equals(bleMac)){
                        return data.getDeviceName();
                    }
                }
            }
        }
        return null;
    }

    //更新溫度與電量 2021/04/06
    public void updateItemByMac(double degree, String battery, String mac){
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
        String currentDateTime = sdf.format(new Date());  // 目前時間
        if (dataList.size() != 0){
            for(int i = 0; i < dataList.size();i++){
               TempDataApi.SuccessBean data = dataList.get(i);
               if(!TextUtils.isEmpty(data.getMac())){
                    if (data.getMac().equals(mac)) {
                        data.setBattery(battery + "%");
                        data.setDegree(degree, currentDateTime); //溫度與日期
                        notifyItemChanged(i); //刷新
                        updateBefore(mac);
                    }
               }
            }
        }
    }

    //將需要上傳資料的key:targetId傳回MAin
    public void updateBefore(String mac){
        for(int i = 0; i < dataList.size();i++){
            TempDataApi.SuccessBean data = dataList.get(i);
            if(!TextUtils.isEmpty(data.getMac())){
                if (data.getMac().equals(mac)){
                    int targetId = data.getTargetId();
                    double degree = data.getDegree();
                    listener.passTarget(targetId, degree);
                }
            }
        }
    }

    //2021/05/03
    public TempDataApi.SuccessBean getDegreeByMac(String mac){
        for(int i = 0; i < dataList.size();i++){
            TempDataApi.SuccessBean data = dataList.get(i);
            if(!TextUtils.isEmpty(data.getMac())){
                if (data.getMac().equals(mac)){
                    return data;
                }
            }
        }
        return null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.degree_item, parent, false);
        return new TemperMainAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TempDataApi.SuccessBean data = dataList.get(position);
        holder.textName.setText(data.getUserName());                 //姓名
        holder.textBleStatus.setText(data.getStatus());              //連線狀態
        holder.textBleBattery.setText(data.getBattery());            //電量
        holder.textDegree.setText(String.valueOf(data.getDegree())); //體溫

        //base64解碼並顯示在imageView
        Glide.with(context)
                .asBitmap()
                .load(Base64.decode(data.getHeadShot(), Base64.DEFAULT))
                .into(holder.imagePhoto);

        //根據藍芽連線Status變更icon跟功能
        if (data.getStatus() != null){
            String bleConnect = context.getString(R.string.ble_connect_status);
            String bleUnConnect = context.getString(R.string.ble_unconnected);

            //斷線先判斷
            if (data.getStatus().contains(bleUnConnect)){
                holder.textBleBattery.setText(""); //清除電池顯示 2021/04/26
                holder.textDegree.setText("");     //清除溫度顯示 2021/04/26
                data.setBattery("");               //清除電池data 2021/04/26 for 判斷icon用
                holder.bleConnect.setImageResource(R.drawable.ic_add_black_24dp);
                holder.bleConnect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onBleConnect(data, position); //藍芽連線
                    }
                });
            }else if (data.getStatus().contains(bleConnect)){ //已連線
                if(holder.textBleBattery.getText().toString().isEmpty()) {
                    holder.bleConnect.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                    holder.bleConnect.setOnClickListener(new View.OnClickListener() {
                        @Override  //開始量測(play icon show)
                        public void onClick(View view) {
                            listener.onBleMeasuring(data);
                        }
                    });
                }else {  //disconnect icon show
                    holder.bleConnect.setImageResource(R.drawable.ic_baseline_close_24);
                    holder.bleConnect.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listener.onBleDisConnected(data);
                        }
                    });
                }
            }
        }else {
            //啟動藍芽連線
            holder.bleConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onBleConnect(data, position);
                }
            });
        }

        //圖表
        holder.bleChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onBleChart(data);
            }
        });

        //症狀
        holder.SymptomIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onSymptomRecord(data, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }




    public interface TemperMainListener {
        void onBleConnect(TempDataApi.SuccessBean data, int position);
        void onBleChart(TempDataApi.SuccessBean data);
        void onBleMeasuring(TempDataApi.SuccessBean data);
        void onBleDisConnected(TempDataApi.SuccessBean data);
        void onSymptomRecord(TempDataApi.SuccessBean data, int position);
        void passTarget(int targetId, double degree);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imagePhoto;
        TextView textName;
        TextView textDegree;
        TextView textBleStatus;
        TextView textBleBattery;
        ImageView bleConnect, bleChart;
        ImageView SymptomIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imagePhoto = itemView.findViewById(R.id.ivUserShot);
            textName = itemView.findViewById(R.id.tvBleUserName);
            textDegree = itemView.findViewById(R.id.tvBleUserDegree);
            textBleStatus = itemView.findViewById(R.id.tvBleStatus);
            textBleBattery = itemView.findViewById(R.id.tvBleBattery);

            bleConnect = itemView.findViewById(R.id.imgBleConnect);
            bleChart = itemView.findViewById(R.id.imgBleChart);
            SymptomIcon = itemView.findViewById(R.id.ivSymIcon);
        }
    }
}
