package com.example.yhyhealthydemo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthydemo.R;
import com.example.yhyhealthydemo.datebase.SymptomData;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class CheckBoxSubAdapter extends RecyclerView.Adapter<CheckBoxSubAdapter.ViewHolder>{

    private static final String TAG = "CheckBoxSubAdapter";

    private Context context;
    private List<String> value;
    private int subPos;

    private List<SymptomData.CheckBoxGroup> checkBoxGroupList = new ArrayList<>();

    public CheckBoxSubAdapter(Context context, List<String> value, int subPos, List<SymptomData.CheckBoxGroup> checkBoxGroupList) {
        this.context = context;
        this.value = value;
        this.subPos = subPos;
        this.checkBoxGroupList = checkBoxGroupList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.disase_checkbox_sub_item, parent, false);
        return new CheckBoxSubAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Dictionary dictionary = getDictionary();

        holder.checkBox.setText((CharSequence) dictionary.get(value.get(position)));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                checkBoxGroupList.get(subPos).setChecked(value.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return checkBoxGroupList.get(subPos).getValue().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    private Dictionary getDictionary(){
        Dictionary dictionary = new Hashtable();

        dictionary.put("none",context.getString(R.string.symptom_none));
        dictionary.put("green",context.getString(R.string.symptom_green));
        dictionary.put("yellow",context.getString(R.string.symptom_yellow));
        dictionary.put("white",context.getString(R.string.symptom_white));
        dictionary.put("rustColor",context.getString(R.string.symptom_rest_color));
        dictionary.put("grayBlack",context.getString(R.string.symptom_gray_black));
        dictionary.put("foamy",context.getString(R.string.symptom_foamy));
        dictionary.put("slimy",context.getString(R.string.symptom_slimy));
        dictionary.put("transparent",context.getString(R.string.symptom_transparent));
        dictionary.put("milky",context.getString(R.string.symptom_milky));
        dictionary.put("yellowGreen",context.getString(R.string.symptom_yellow_green));
        dictionary.put("pink",context.getString(R.string.symptom_pink));
        dictionary.put("brown",context.getString(R.string.symptom_brown));
        dictionary.put("black",context.getString(R.string.symptom_black));
        dictionary.put("waterLike",context.getString(R.string.symptom_waterLike));
        dictionary.put("stickyPus",context.getString(R.string.symptom_stickyPus));
        return dictionary;
    }
}
