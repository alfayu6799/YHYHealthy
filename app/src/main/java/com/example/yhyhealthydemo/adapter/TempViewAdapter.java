package com.example.yhyhealthydemo.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthydemo.R;
import com.example.yhyhealthydemo.data.ScannedData;

import java.util.ArrayList;
import java.util.List;

/**********************
 * 藍芽體溫 Adapter
 * DATA from ScanneData
 * layout : device_list
* *********************/
public class TempViewAdapter extends RecyclerView.Adapter<TempViewAdapter.ViewHolder>{

    private final static String TAG = "TempViewAdapter";

    private TempViewAdapter.OnItemClick onItemClick;
    private List<ScannedData> arrayList = new ArrayList<>();

    public void OnItemClick(TempViewAdapter.OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    /**清除搜尋到的裝置列表*/
    public void clearDevice(){
        this.arrayList.clear();
        notifyDataSetChanged();
    }

    /**若有不重複的裝置出現，則加入列表中*/
    public void addDevice(List<ScannedData> arrayList){
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvName.setText(arrayList.get(position).getDeviceName());
        holder.tvAddress.setText(arrayList.get(position).getAddress());
        holder.tvRssi.setText("rssi "+ arrayList.get(position).getRssi());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick.onItemClick(arrayList.get(position));
                Log.d(TAG, "從TempViewAdapter得到使用者點擊的訊息(Address) : " + arrayList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public interface OnItemClick{
        void onItemClick(ScannedData selectedDevice);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName,tvAddress,tvRssi;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBleName);
            tvAddress = itemView.findViewById(R.id.tvBleAddress);
            tvRssi = itemView.findViewById(R.id.tvBleRssi);
        }
    }

}
