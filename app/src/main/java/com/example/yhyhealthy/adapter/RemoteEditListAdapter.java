package com.example.yhyhealthy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthy.R;

import java.util.ArrayList;
import java.util.List;

public class RemoteEditListAdapter extends RecyclerView.Adapter<RemoteEditListAdapter.ViewHolder>{

    private Context context;
    private List<String> dataList = new ArrayList<String>();
    private RemoteEditListAdapter.RemoteEditListListener listener;

    //建構子


    public RemoteEditListAdapter(Context context, List<String> dataList, RemoteEditListListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.remote_edit_item, parent, false);
        return new RemoteEditListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
           holder.accountInfo.setText(dataList.get(position));

           //更新
           holder.accountUpdate.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   listener.onUpdateClick(dataList.get(position), position);
               }
           });

           //移除
            holder.accountDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onDeleteClick(dataList.get(position), position);
                }
            });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public interface RemoteEditListListener{
        void onUpdateClick(String dataStr, int position);
        void onDeleteClick(String dataStr, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView accountInfo;
        TextView accountUpdate;
        TextView accountDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            accountInfo = itemView.findViewById(R.id.tvRemoteAccount);
            accountUpdate = itemView.findViewById(R.id.tvRemoteUpdate);
            accountDelete = itemView.findViewById(R.id.tvRemoteDelete);
        }
    }
}
