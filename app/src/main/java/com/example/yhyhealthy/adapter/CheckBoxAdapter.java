package com.example.yhyhealthy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthy.R;
import com.example.yhyhealthy.datebase.SymptomData;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class CheckBoxAdapter extends RecyclerView.Adapter<CheckBoxAdapter.ViewHolder>{
    private static final String TAG = "CheckBoxAdapter";

    private Context context;
    private List<SymptomData.CheckBoxGroup> checkBoxGroupList = new ArrayList<>();

    public CheckBoxAdapter(Context context, List<SymptomData.CheckBoxGroup> checkBoxGroupList) {
        this.context = context;
        this.checkBoxGroupList = checkBoxGroupList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.disease_checkbox_item, parent, false);
        return new CheckBoxAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Dictionary dictionary = getDictionary();

        String[] str = checkBoxGroupList.get(position).getKey().split(",");
        holder.tvTitle.setText((CharSequence) dictionary.get(str[0]));
        holder.tvTitleSub.setText((CharSequence) dictionary.get(str[1]));

        //checkBox資料傳到adapter
        CheckBoxSubAdapter adapter = new CheckBoxSubAdapter(context, checkBoxGroupList.get(position).getValue(),position, checkBoxGroupList);
        holder.subRecycler.setAdapter(adapter);
        holder.subRecycler.setHasFixedSize(true);
        holder.subRecycler.setLayoutManager(new GridLayoutManager(context, 2));
    }

    @Override
    public int getItemCount() {
        return checkBoxGroupList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvTitle;
        TextView tvTitleSub;
        RecyclerView subRecycler;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvSympTitlle);
            tvTitleSub = itemView.findViewById(R.id.tvSympSub);
            subRecycler = itemView.findViewById(R.id.rvSub);
        }
    }

    private Dictionary getDictionary(){
        Dictionary dictionary = new Hashtable();
        dictionary.put("sputum",context.getString(R.string.symptom_sputum));
        dictionary.put("nose",context.getString(R.string.symptom_nose));
        dictionary.put("color",context.getString(R.string.symptom_color));
        dictionary.put("type",context.getString(R.string.symptom_type));
        return dictionary;
    }
}
