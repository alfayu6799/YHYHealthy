package com.example.yhyhealthydemo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yhyhealthydemo.R;
import com.example.yhyhealthydemo.datebase.Member;
import com.example.yhyhealthydemo.tools.RecyclerViewListener;
import java.util.List;

/**********************
 * 藍芽使用者 Adapter
 * DATA from Member
 * layout : recycler_item
 * *********************/

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";

    private Context context;
    private List<Member> memberList;

    private RecyclerViewListener listener;

    public RecyclerViewAdapter(Context context, List<Member> memberList, RecyclerViewListener listener) {
        this.context = context;
        this.memberList = memberList;
        this.listener = listener;
    }

    public void clearInfo(){
        this.memberList.clear();
        notifyDataSetChanged();
    }

    //更新參數 : 會員,位置
    public void updateItem(Member member ,int pos) {
        if (memberList.size() != 0) {
            memberList.set(pos, member);
            notifyItemChanged(pos);
        }
    }

    // 刪除項目
    public void removeItem(int position){
        memberList.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.degree_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Member member = memberList.get(position);
        holder.imagePhoto.setImageResource(member.getImage());
        holder.textName.setText(member.getName());
        holder.textDegree.setText(String.valueOf(member.getDegree()));
        holder.textBleStatus.setText(member.getStatus());
        holder.textBleBattery.setText(member.getBattery());

        Log.d(TAG, "onBindViewHolder: " + member.getBattery());

        if(member.getStatus().contains("已連線")) {
            holder.bleConnect.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
            holder.bleConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onBleMeasuring(member); //量測
                }
            });
        }else if (member.getBattery() != null) {
            Log.d(TAG, "onBindViewHolder: ????");
        }else {
            holder.bleConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onBleConnect(member);  //搜尋
                }
            });
        }

//        holder.bleMeasuring.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                listener.onBleMeasuring(member);
//            }
//        });

        holder.delUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem(position);
                listener.onDelUser(member);
            }
        });

        holder.bleChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onBleChart(member);
            }
        });
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imagePhoto;
        TextView textName;
        TextView textDegree;
        TextView textBleStatus;
        TextView textBleBattery;
        TextView textBleDeviceName;
        ImageView bleConnect, delUser, bleChart;
//        ImageView bleMeasuring;

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
//            bleMeasuring = itemView.findViewById(R.id.ivBleMeasuring);
        }
    }
}
