package com.example.yhyhealthydemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthydemo.ArticleActivity;
import com.example.yhyhealthydemo.R;
import com.example.yhyhealthydemo.datebase.EducationData;
import com.squareup.picasso.Picasso;
import java.util.List;

import static com.example.yhyhealthydemo.module.ApiProxy.URL_IMG;

public class EducationSubAdapter extends RecyclerView.Adapter<EducationSubAdapter.EducationSubViewHolder>{

    private static final String TAG = "EducationSubAdapter";

    private Context context;
    private List<EducationData.ServiceItemListBean.AttrlistBean> list;

    public EducationSubAdapter(Context context, List<EducationData.ServiceItemListBean.AttrlistBean> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public EducationSubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_sub_item_edu, parent, false);
        return new EducationSubAdapter.EducationSubViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EducationSubViewHolder holder, int position) {
            holder.articleName.setText(list.get(position).getAttrName());
            Picasso.get().load(URL_IMG + list.get(position).getIconImg()).into(holder.articleIcon);

            holder.articleIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String attrID = list.get(position).getAttrId();
                    String ServiceItemId = list.get(position).getServiceItemId();
                    String AttrName = list.get(position).getAttrName();
                    //將點擊的icon資料傳到另一個頁面
                    Intent intent = new Intent(context, ArticleActivity.class); //文章頁面
                    Bundle bundle = new Bundle();
                    bundle.putString("AttrID", attrID);
                    bundle.putString("ServiceItemId", ServiceItemId);
                    bundle.putString("AttName", AttrName);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class EducationSubViewHolder extends RecyclerView.ViewHolder{

        TextView articleName;
        ImageView articleIcon;

        public EducationSubViewHolder(@NonNull View itemView) {
            super(itemView);

            articleName = itemView.findViewById(R.id.tv_item_name);
            articleIcon = itemView.findViewById(R.id.iv_item_icon);
        }
    }

}
