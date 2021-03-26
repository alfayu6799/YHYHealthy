package com.example.yhyhealthydemo.adapter;

import android.content.Context;
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
    public void updateItem(TempDataApi.SuccessBean data, int pos){
        if (dataList.size() != 0){
            dataList.set(pos, data);
            notifyItemChanged(pos);
        }
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
            if (data.getStatus().contains("已連線")){
                holder.bleConnect.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                holder.bleConnect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onBleMeasuring(data, position);
                    }
                });
            }else if (data.getStatus().contains("已斷開")){
                holder.bleConnect.setImageResource(R.drawable.ic_add_black_24dp);
                holder.bleConnect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onBleConnect(data, position);
                    }
                });
            }
        }else {

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
                listener.onBleChart(data, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public interface TemperMainListener {
        void onBleConnect(TempDataApi.SuccessBean data, int position);
        void onBleChart(TempDataApi.SuccessBean data, int position);
        void onBleMeasuring(TempDataApi.SuccessBean data, int position);
        //void onDelUser(TempDataApi.SuccessBean data, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imagePhoto;
        TextView textName;
        TextView textDegree;
        TextView textBleStatus;
        TextView textBleBattery;
        TextView textBleDeviceName;
        ImageView bleConnect, delUser, bleChart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imagePhoto = itemView.findViewById(R.id.ivUserShot);
            textName = itemView.findViewById(R.id.tvBleUserName);
            textDegree = itemView.findViewById(R.id.tvBleUserDegree);
            textBleStatus = itemView.findViewById(R.id.tvBleStatus);
            textBleBattery = itemView.findViewById(R.id.tvBleBattery);

            bleConnect = itemView.findViewById(R.id.imgBleConnect);
            delUser = itemView.findViewById(R.id.imgDeleteUser);
            bleChart = itemView.findViewById(R.id.imgBleChart);
        }
    }
}
