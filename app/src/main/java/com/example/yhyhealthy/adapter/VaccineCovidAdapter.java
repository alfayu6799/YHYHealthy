package com.example.yhyhealthy.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.datebase.SymptomData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import static com.example.yhyhealthy.module.ApiProxy.covid19Select;

public class VaccineCovidAdapter extends RecyclerView.Adapter<VaccineCovidAdapter.ViewHolder>{

    private static final String TAG = "VaccineCovidAdapter";

    private Context context;
    private List<String> arrayList = new ArrayList<>();

    private int lastSelectPosition = -1;

    public VaccineCovidAdapter(Context context, List<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.covid19_item1, parent, false);
        return new VaccineCovidAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Dictionary dictionary = getDictionary();

        //holder.checkBox.setText((CharSequence) dictionary.get(arrayList.get(position)));
        holder.radioButton.setText((CharSequence) dictionary.get(arrayList.get(position)));
        holder.radioButton.setChecked(lastSelectPosition == position);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CheckBox checkBox;
        RadioGroup radioGroup;
        RadioButton radioButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            checkBox = itemView.findViewById(R.id.cb_covid);
            radioGroup = itemView.findViewById(R.id.rdo_gup);
            radioButton = itemView.findViewById(R.id.rdo_vaccine);
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lastSelectPosition = getAdapterPosition();
                    notifyDataSetChanged(); //更新畫面
                    covid19Select = String.valueOf(radioButton.getText()); //選項值傳給covid19Select
                }
            });
        }
    }

    private Dictionary getDictionary() {
        Dictionary dictionary = new Hashtable();
        dictionary.put("BNT",context.getString(R.string.vaccine_bnt));
        dictionary.put("AZ", context.getString(R.string.vaccine_az));
        dictionary.put("Moderna", context.getString(R.string.vaccine_moderna));
        dictionary.put("Johnson", context.getString(R.string.vaccine_johnson));
        dictionary.put("Novavax", context.getString(R.string.vaccine_novavax));
        dictionary.put("Sinopharm", context.getString(R.string.vaccine_sinopharm));
        dictionary.put("Sinovac", context.getString(R.string.vaccine_sinovac));
        return dictionary;
    }
}
