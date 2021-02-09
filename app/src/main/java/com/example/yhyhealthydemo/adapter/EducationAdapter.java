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

import com.example.yhyhealthydemo.R;
import com.example.yhyhealthydemo.datebase.EducationData;
import com.example.yhyhealthydemo.tools.SpacesItemDecoration;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.example.yhyhealthydemo.module.ApiProxy.URL_IMG;

public class EducationAdapter extends RecyclerView.Adapter<EducationAdapter.EducationViewHolder> {

    private static final String TAG = "EducationAdapter";
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
        int spacingInPixels = 10;  //設定item間距的距離
        holder.name.setText(dataList.get(position).getName());
        Picasso.get().load(URL_IMG + dataList.get(position).getIconImg()).into(holder.icon);
        holder.recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
        holder.recyclerView.setHasFixedSize(true);
        holder.recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
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
