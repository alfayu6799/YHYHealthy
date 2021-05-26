package com.example.yhyhealthy.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthy.R;
import com.example.yhyhealthy.datebase.Record;

import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder>{
    private List<Record> recordList;

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class RecordViewHolder extends RecyclerView.ViewHolder{
        TextView nameText;
        TextView dateText;
        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.tv_record_name);
            dateText = itemView.findViewById(R.id.tv_record_date);
        }
    }
}
