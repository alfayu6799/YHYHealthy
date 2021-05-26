package com.example.yhyhealthy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.datebase.Observation;
import java.util.List;

public class ObserverViewAdapter extends RecyclerView.Adapter<ObserverViewAdapter.ViewHolder>{

    private Context context;
    private List<Observation> observationList;

    public ObserverViewAdapter(Context context, List<Observation> observationList) {
        this.context = context;
        this.observationList = observationList;
    }

    //刪除項目
    public void removeItem(int position){
        observationList.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.observation_item, parent, false);
        return new ObserverViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Observation observation = observationList.get(position);
        holder.imageView.setImageResource(observation.getObserverImage());
        holder.textName.setText(observation.getObserverName());
        holder.textGender.setText(observation.getObserverGender());
        holder.textBirthday.setText(observation.getObserverBirthday());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return observationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView  textName;
        TextView  textGender;
        TextView  textBirthday;
        TextView  edit, delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.ivObserverShot);
            textName = itemView.findViewById(R.id.tvObserverName);
            textGender = itemView.findViewById(R.id.tvObserverGender);
            textBirthday = itemView.findViewById(R.id.tvObserverBirthday);
            edit = itemView.findViewById(R.id.tvObserverEdit);
            delete = itemView.findViewById(R.id.tvObserverDel);
        }
    }
}
