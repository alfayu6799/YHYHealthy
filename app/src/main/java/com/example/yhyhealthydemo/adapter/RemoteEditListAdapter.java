package com.example.yhyhealthydemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthydemo.R;

public class RemoteEditListAdapter extends RecyclerView.Adapter<RemoteEditListAdapter.ViewHolder>{

    private Context context;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.remote_edit_item, parent, false);
        return new RemoteEditListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public interface RemoteEditListListener{
        void onUpdateClick();
        void onDeleteClick();
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
