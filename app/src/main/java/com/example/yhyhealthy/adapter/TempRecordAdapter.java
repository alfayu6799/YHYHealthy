package com.example.yhyhealthy.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TempRecordAdapter {

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView headShot;
        TextView  txtUserName;
        TextView  startMeasureDay;
        TextView  endMeasureDay;
        TextView  startDegree;
        TextView  endDegree;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
