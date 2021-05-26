package com.example.yhyhealthy.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yhyhealthy.R;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class FunctionsAdapter extends RecyclerView.Adapter<FunctionsAdapter.viewHolder>{

    private static final String TAG = "FunctionsAdapter";

    private Context context;
    private String startDay;
    private String endDay;
    private List<String> functionList = new ArrayList<>();

    private FunctionsAdapter.OnRecycleItemClickListener listener; //interface

    public FunctionsAdapter(Context context, String startDay, String endDay, List<String> functionList, OnRecycleItemClickListener listener) {
        this.context = context;
        this.startDay = startDay;
        this.endDay = endDay;
        this.functionList = functionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.records_item, parent, false);
        return new FunctionsAdapter.viewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        DateTime dt1 = new DateTime(startDay);
        DateTime dt2 = new DateTime(endDay);
        String startDayStr = dt1.toString("yyyy/MM/dd");
        String endDayStr = dt2.toString("yyyy/MM/dd");

        holder.textDay.setText(startDayStr + "~" + endDayStr);
        holder.textFunction.setText(functionList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //listener.onClick(functionList.get(position),startDay, endDay);
                listener.onClick(position, startDay, endDay);
            }
        });
    }

    @Override
    public int getItemCount() {
        return functionList.size();
    }

    public interface OnRecycleItemClickListener{

        void onClick(int functionName, String start, String end);
    }

    public class viewHolder extends RecyclerView.ViewHolder{

        TextView textDay;
        TextView textFunction;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            textDay = itemView.findViewById(R.id.tvDateRange);
            textFunction = itemView.findViewById(R.id.tvFunctionName);
        }
    }
}
