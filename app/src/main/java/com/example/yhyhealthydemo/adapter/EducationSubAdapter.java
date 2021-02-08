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
import com.example.yhyhealthydemo.datebase.EducationData;
import com.squareup.picasso.Picasso;
import java.util.List;

public class EducationSubAdapter extends RecyclerView.Adapter<EducationSubAdapter.EducationSubViewHolder>{

    //衛教icon
    private static String IMG_PATH = "http://192.168.1.120:8080/health_education/iconImg/";

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
            Picasso.get().load(IMG_PATH + list.get(position).getIconImg()).into(holder.articleIcon);
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
