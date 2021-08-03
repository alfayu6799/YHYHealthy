package com.example.yhyhealthy.adapter;

import android.content.Context;
import android.graphics.Color;
import android.service.voice.AlwaysOnHotwordDetector;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.datebase.TempDataApi;

import java.nio.file.ClosedFileSystemException;
import java.util.List;

/***
 *  歷史資料 - 觀測者專用配適器
 *  create 2021/06/17
* ***/
public class ObserverAdapter extends RecyclerView.Adapter<ObserverAdapter.ViewHolder>{

    private static final String TAG = "ObserverAdapter";

    private Context context;
    private List<TempDataApi.SuccessBean> dataList;

    int selectedItemPosition = -1;

    //interface
    private ObserverAdapter.onItemClickListener listener;

    public ObserverAdapter(Context context, List<TempDataApi.SuccessBean> dataList, onItemClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.observer_item, parent, false);
        return new ObserverAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TempDataApi.SuccessBean data = dataList.get(position);

        //姓名
        holder.observerName.setText(data.getUserName());

        //性別
        if (data.getGender().equals("F")) {
            holder.observerGender.setText(R.string.female);
        }else {
            holder.observerGender.setText(R.string.male);
        }

        //生日
        holder.observerBirthday.setText(data.getTempBirthday());

        //圖像
        Glide.with(context)
                .asBitmap()
                .load(Base64.decode(data.getHeadShot(), Base64.DEFAULT))
                .into(holder.observerPhoto);
//        Glide.with(context)
//                .asBitmap()
//                .load(data.getImgId())
//                .into(holder.observerPhoto);
        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(data);
                selectedItemPosition = position;
                notifyDataSetChanged();
            }
        });

        if (selectedItemPosition == position){ //點擊
            holder.observerName.setTextColor(Color.WHITE);
            holder.observerGender.setTextColor(Color.WHITE);
            holder.observerBirthday.setTextColor(Color.WHITE);
            holder.itemView.setBackgroundResource(R.drawable.bg_selector);
        }else {
            holder.observerName.setTextColor(Color.BLACK);
            holder.observerGender.setTextColor(Color.BLACK);
            holder.observerBirthday.setTextColor(Color.BLACK);
            holder.itemView.setBackgroundResource(R.drawable.shape_public_background);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public interface onItemClickListener{
        void onItemClick(TempDataApi.SuccessBean data);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView observerName;
        TextView observerGender;
        TextView observerBirthday;
        ImageView observerPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            observerPhoto = itemView.findViewById(R.id.iv_observer_shut);
            observerName = itemView.findViewById(R.id.tv_observer_name);
            observerGender = itemView.findViewById(R.id.tv_observer_gender);
            observerBirthday = itemView.findViewById(R.id.tv_observer_birthday);
        }
    }
}
