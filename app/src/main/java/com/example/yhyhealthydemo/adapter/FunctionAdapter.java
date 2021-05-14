package com.example.yhyhealthydemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthydemo.R;

public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.ViewHolder>{

    private Context context;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.functions_item, parent, false);
        return new FunctionAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView  iconName;
        ImageView iconImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            iconName = itemView.findViewById(R.id.tvFunction);
            iconImage = itemView.findViewById(R.id.ivFunction);
        }
    }
}
