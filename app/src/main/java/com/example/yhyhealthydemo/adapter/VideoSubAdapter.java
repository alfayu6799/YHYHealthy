package com.example.yhyhealthydemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yhyhealthydemo.R;
import com.example.yhyhealthydemo.VideoDetailActivity;
import com.example.yhyhealthydemo.datebase.VideoData;
import com.squareup.picasso.Picasso;
import java.util.List;

public class VideoSubAdapter extends RecyclerView.Adapter<VideoSubAdapter.VideoSubViewHolder>{

    private static final String TAG = "VideoSubAdapter";

    private Context context;
    private List<VideoData.ServiceItemListBean.AttrlistBean> list;

    public VideoSubAdapter(Context context, List<VideoData.ServiceItemListBean.AttrlistBean> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VideoSubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_sub_item_vedio, parent, false);
        return new VideoSubViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoSubViewHolder holder, int position) {
        holder.itemName.setText(list.get(position).getAttrName());
        Picasso.get().load(list.get(position).getIconImg()).into(holder.itemIcon);
        holder.itemIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = list.get(position).getAttrId();
                String ItemId = list.get(position).getServiceItemId();
                String name = list.get(position).getAttrName();

                Intent intent = new Intent(context, VideoDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("AttrID",id);
                bundle.putString("ServiceItemId", ItemId);
                bundle.putString("AttName", name);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class VideoSubViewHolder extends RecyclerView.ViewHolder{

        ImageView itemIcon;
        TextView  itemName;

        public VideoSubViewHolder(@NonNull View itemView) {
            super(itemView);

            itemIcon = itemView.findViewById(R.id.videoItemIcon);
            itemName = itemView.findViewById(R.id.videoItemName);
        }
    }
}
