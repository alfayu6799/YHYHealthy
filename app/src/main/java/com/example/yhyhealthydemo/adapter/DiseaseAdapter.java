package com.example.yhyhealthydemo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthydemo.R;

/**   *** ***
 * 症狀配適器
 * create 2021/04/07
 * *  *****/

public class DiseaseAdapter extends RecyclerView.Adapter<DiseaseAdapter.ViewHolder>{
    private static final String TAG = "DiseaseAdapter";

    private Context context;

    public DiseaseAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.disease_item, parent, false);
        return new DiseaseAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            holder.headName.setText("頭痛");
            holder.symptom.setChecked(false);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView headName;
        Switch   symptom;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            headName = itemView.findViewById(R.id.tvSymptomItem);
            symptom = itemView.findViewById(R.id.swSymptom);
        }
    }
}
