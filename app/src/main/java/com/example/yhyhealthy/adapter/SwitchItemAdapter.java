package com.example.yhyhealthy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthy.R;
import com.example.yhyhealthy.datebase.SymptomData;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**   *** ***
 * 症狀-switch配適器
 * create 2021/04/07
 * *  *****/

public class SwitchItemAdapter extends RecyclerView.Adapter<SwitchItemAdapter.ViewHolder>{
    private static final String TAG = "SwitchItemAdapter";

    private Context context;
    private List<SymptomData.SwitchItemBean> dataList = new ArrayList<>();

    //建構子
    public SwitchItemAdapter(Context context, List<SymptomData.SwitchItemBean> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.disease_switch_item, parent, false);
        return new SwitchItemAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Dictionary dictionary = getDictionary();

        holder.textName.setText((CharSequence) dictionary.get(dataList.get(position).getKey()));

        holder.aSwitch.setChecked(dataList.get(position).isValue());
        holder.aSwitch.setText(R.string.off);
        holder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    dataList.get(position).setValue(true);  //有選擇才會變成true
                    holder.aSwitch.setText(R.string.on);
                }else {
                    dataList.get(position).setValue(false);
                    holder.aSwitch.setText(R.string.off);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView textName;
        Switch aSwitch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.tvSymptomItem);
            aSwitch = itemView.findViewById(R.id.swSymptom);
        }
    }

    private Dictionary getDictionary() {
        Dictionary dictionary = new Hashtable();
        dictionary.put("fever",context.getString(R.string.fever));
        dictionary.put("headache", context.getString(R.string.headache));
        dictionary.put("fatigue",context.getString(R.string.fatigue));
        dictionary.put("soreMusclesJoints",context.getString(R.string.soreMusclesJoints));
        dictionary.put("soreThroat",context.getString(R.string.soreThroat));
        dictionary.put("cough",context.getString(R.string.cough));
        dictionary.put("runnyNose",context.getString(R.string.runnyNose));
        dictionary.put("diarrhea",context.getString(R.string.diarrhea));
        dictionary.put("chestTightness",context.getString(R.string.chestTightness));
        dictionary.put("shortnessBreath",context.getString(R.string.shortnessBreath));
        dictionary.put("taste",context.getString(R.string.taste));
        dictionary.put("vomiting",context.getString(R.string.vomiting));
        dictionary.put("chills",context.getString(R.string.chills));
        return dictionary;
    }
}
