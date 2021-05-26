package com.example.yhyhealthy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.datebase.VideoData;
import com.example.yhyhealthy.tools.SpacesItemDecoration;
import com.squareup.picasso.Picasso;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder>{

    private static final String TAG = "VideoAdapter";
    private Context context;
    private List<VideoData.ServiceItemListBean> videoList;

    public VideoAdapter(Context context, List<VideoData.ServiceItemListBean> videoList) {
        this.context = context;
        this.videoList = videoList;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.videoName.setText(videoList.get(position).getName());
        Picasso.get().load(videoList.get(position).getIconImg()).into(holder.videoIcon);
        int spacingInPixels = 10;  //設定item間距的距離
        holder.recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
        holder.recyclerView.setHasFixedSize(true);
        holder.recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        holder.recyclerView.setAdapter(new VideoSubAdapter (context,videoList.get(position).getAttrlist()));
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder{

        ImageView videoIcon;
        TextView videoName;
        RecyclerView recyclerView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);

            videoIcon = itemView.findViewById(R.id.video_title_icon);
            videoName = itemView.findViewById(R.id.video_title_name);
            recyclerView = itemView.findViewById(R.id.rv_sub_item_video);
        }
    }
}
