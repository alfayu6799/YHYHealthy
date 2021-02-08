package com.example.yhyhealthydemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthydemo.ArticleActivity;
import com.example.yhyhealthydemo.R;
import com.example.yhyhealthydemo.datebase.ArticleData;
import com.example.yhyhealthydemo.datebase.EducationData;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EducationAdapter extends RecyclerView.Adapter<EducationAdapter.EducationViewHolder>{

    //衛教icon
    private static String URL_IMG = "http://192.168.1.120:8080/health_education/iconImg/";

    private Context context;
    private List<EducationData.ServiceItemListBean> dataList;

    public EducationAdapter(Context context, List<EducationData.ServiceItemListBean> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public EducationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_edu, parent, false);
        return new EducationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EducationViewHolder holder, int position) {
            holder.name.setText(dataList.get(position).getName());
            Picasso.get().load(URL_IMG + dataList.get(position).getIconImg()).into(holder.icon);
            holder.recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
            holder.recyclerView.setHasFixedSize(true);
            holder.recyclerView.setAdapter(new EducationSubAdapter (context,dataList.get(position).getAttrlist()));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class EducationViewHolder extends RecyclerView.ViewHolder{

        ImageView icon;
        TextView name;
        RecyclerView recyclerView;

        public EducationViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_title_icon);
            name = itemView.findViewById(R.id.tv_title_name);
            recyclerView = itemView.findViewById(R.id.rv_sub_item);
        }
    }

}
