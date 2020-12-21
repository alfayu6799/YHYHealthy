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
import com.example.yhyhealthydemo.data.Remote;

import java.util.List;

public class RemoteViewAdapter extends RecyclerView.Adapter<RemoteViewAdapter.ViewHolder>{
    private Context context;
    private List<Remote> remoteList;

    public RemoteViewAdapter(Context context, List<Remote> remoteList) {
        this.context = context;
        this.remoteList = remoteList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.remoto_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Remote remote = remoteList.get(position);
        holder.imageView.setImageResource(remote.getImage());
        holder.textName.setText(remote.getName());
        holder.textDegree.setText(String.valueOf(remote.getDegree()));
    }

    @Override
    public int getItemCount() {
        return remoteList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView textName;
        TextView textDegree;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.ivRemoteShot);
            textName = itemView.findViewById(R.id.tvRemoteUserName);
            textDegree = itemView.findViewById(R.id.tvRemoteUserDegree);

        }
    }
}
