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
import com.example.yhyhealthydemo.datebase.DegreeUserData;

import java.util.List;

public class DegreeAdapter extends RecyclerView.Adapter<DegreeAdapter.ViewHolder>{

    private static final String TAG = "DegreeAdapter";

    private Context context;
    private List<DegreeUserData.SuccessBean> list;  //來自後台api
    private DegreeAdapter.DegreeViewListener listener;

    public DegreeAdapter(Context context, List<DegreeUserData.SuccessBean> list/*, DegreeViewListener listener*/) {
        this.context = context;
        this.list = list;
        //this.listener = listener;
    }

    //移除項目
    public void removeItem(int position){
        list.remove(position);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new DegreeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DegreeUserData.SuccessBean userData = list.get(position);
            holder.textName.setText(userData.getName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imagePhoto;
        TextView textName;
        TextView textDegree;
        TextView textBleStatus;
        TextView textBleBattery;
        TextView textBleDeviceName;
        ImageView bleConnect, delUser, bleChart;
        ImageView bleMeasuring;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imagePhoto = itemView.findViewById(R.id.ivUserShot);
            textName = itemView.findViewById(R.id.tvBleUserName);
            textDegree = itemView.findViewById(R.id.tvBleUserDegree);
            textBleStatus = itemView.findViewById(R.id.tvBleStatus);
            textBleBattery = itemView.findViewById(R.id.tvBleBattery);

            bleConnect = itemView.findViewById(R.id.imgBleConnect);
            delUser = itemView.findViewById(R.id.imgDeleteUser);
            bleChart = itemView.findViewById(R.id.imgBleChart);
            bleMeasuring = itemView.findViewById(R.id.ivBleMeasuring);
        }
    }

    public interface DegreeViewListener{

    }
}
