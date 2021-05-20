package com.example.yhyhealthydemo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yhyhealthydemo.R;

import java.util.ArrayList;
import java.util.List;

public class FunctionsAdapter extends RecyclerView.Adapter<FunctionsAdapter.viewHolder>{

    private Context context;
    private String startDay;
    private String endDay;

    private List<String> functionList = new ArrayList<>();

    public FunctionsAdapter(Context context, String startDay, String endDay, List<String> functionList) {
        this.context = context;
        this.startDay = startDay;
        this.endDay = endDay;
        this.functionList = functionList;
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
        holder.textDay.setText(startDay + "~" + endDay);
        holder.textFunction.setText(functionList.get(position));
    }

    @Override
    public int getItemCount() {
        return functionList.size();
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
