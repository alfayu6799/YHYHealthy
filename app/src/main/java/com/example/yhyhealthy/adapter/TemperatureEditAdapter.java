package com.example.yhyhealthy.adapter;

import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.datebase.TempDataApi;

import java.util.List;

/**  ******
 * 體溫列表編輯配適器
 * 資料來源:TempDataApi.SuccessBean
 * 介面
 *     刪除onRemoveClick
 * create 2021/04/10
 * * **************** *********/
public class TemperatureEditAdapter extends RecyclerView.Adapter<TemperatureEditAdapter.ViewHolder>{

    private Context context;
    private List<TempDataApi.SuccessBean> dataList;
    private TemperatureEditAdapter.TemperatureEditListener listener;

    //建構子
    public TemperatureEditAdapter(Context context, List<TempDataApi.SuccessBean> dataList, TemperatureEditListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
    }

    public TemperatureEditAdapter(Context context, List<TempDataApi.SuccessBean> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.degree_edit_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TempDataApi.SuccessBean data = dataList.get(position);
        holder.name.setText(data.getUserName());

        if (data.getGender().equals("F")) {
            holder.gender.setText(R.string.female);
        }else{
            holder.gender.setText(R.string.male);
        }

        holder.birthday.setText(data.getTempBirthday());

        //base64解碼並顯示在imageView
        Glide.with(context)
                .asBitmap()
                .load(Base64.decode(data.getHeadShot(), Base64.DEFAULT))
                .into(holder.editPhoto);

        //依據使用者點擊的position刪除單一個體
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRemoveClick(data, position);
            }
        });

        //依據使用者點擊的position取得單一個體編輯
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
        void onEditClick(TempDataApi.SuccessBean data);
        void onRemoveClick(TempDataApi.SuccessBean data, int position);
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
