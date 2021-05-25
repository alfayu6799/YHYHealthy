package com.example.yhyhealthydemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthydemo.R;
import com.example.yhyhealthydemo.datebase.CycleRecord;

import org.joda.time.DateTime;

import java.util.List;

public class OvulationRecordAdapter extends RecyclerView.Adapter<OvulationRecordAdapter.ViewHolder>{

    private Context context;
    private List<CycleRecord.SuccessBean> dataList;

    public OvulationRecordAdapter(Context context, List<CycleRecord.SuccessBean> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.ovulation_item, parent, false);
        return new OvulationRecordAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DateTime dateTime = new DateTime(dataList.get(position).getTestDate());
        holder.txtDay.setText(dateTime.toString("yyyy/MM/dd"));

        holder.txtDegree.setText(String.valueOf(dataList.get(position).getTemperature()));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView txtDay;
        TextView txtIdentify;
        TextView txtDegree;
        TextView txtNote;
        TextView txtCycleStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtDay = itemView.findViewById(R.id.tvOvulDay);
            txtIdentify = itemView.findViewById(R.id.tvOualIdentify);
            txtDegree = itemView.findViewById(R.id.tvOualDegree);
            txtNote = itemView.findViewById(R.id.tvNotice);
            txtCycleStatus = itemView.findViewById(R.id.tvCycleStatus);
        }
    }
}
