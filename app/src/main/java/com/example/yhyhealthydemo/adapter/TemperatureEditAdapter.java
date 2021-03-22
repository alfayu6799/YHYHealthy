package com.example.yhyhealthydemo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthydemo.R;
import com.example.yhyhealthydemo.datebase.TemperatureData;

import java.util.List;

public class TemperatureEditAdapter extends RecyclerView.Adapter<TemperatureEditAdapter.ViewHolder>{

    private static final String TAG = "TemperatureEditAdapter";

    private Context context;
    private List<TemperatureData.SuccessBean> dataList;
    private TemperatureEditAdapter.TemperatureEditListener listener;

    public TemperatureEditAdapter(Context context, List<TemperatureData.SuccessBean> dataList, TemperatureEditListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.degree_edit_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TemperatureData.SuccessBean data = dataList.get(position);
        holder.name.setText(data.getName());

        if (data.getGender().equals("F")) {
            holder.gender.setText(R.string.female);
        }else{
            holder.gender.setText(R.string.male);
        }

        holder.birthday.setText(data.getBirthday());

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRemoveClick(data);
            }
        });

        holder.revise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onEditClick(data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public interface TemperatureEditListener{
        void onEditClick(TemperatureData.SuccessBean data);
        void onRemoveClick(TemperatureData.SuccessBean data);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView editPhoto;
        private TextView name, gender, birthday;
        private TextView revise, remove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            editPhoto = itemView.findViewById(R.id.ivEditPhoto);
            name = itemView.findViewById(R.id.tvEditName);
            gender = itemView.findViewById(R.id.tvEditGender);
            birthday = itemView.findViewById(R.id.tvEditBirthday);
            revise = itemView.findViewById(R.id.tvEditRevise);   //編輯
            remove = itemView.findViewById(R.id.tvEditRemove);   //移除
        }
    }
}
