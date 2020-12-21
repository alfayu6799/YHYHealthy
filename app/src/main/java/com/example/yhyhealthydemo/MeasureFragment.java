package com.example.yhyhealthydemo;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MeasureFragment extends Fragment implements View.OnClickListener {

    private View view;

    private Button ovulation, temperature, pregnancy,monitor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;

        view = inflater.inflate(R.layout.fragment_measure, container, false);

        ovulation = view.findViewById(R.id.bt_ovulation);
        temperature = view.findViewById(R.id.bt_temperature);
        pregnancy = view.findViewById(R.id.bt_pregnancy);
//        pregnancy.setVisibility(View.INVISIBLE);
        monitor = view.findViewById(R.id.bt_monitor);
//        monitor.setVisibility(View.INVISIBLE);

        ovulation.setOnClickListener(this);
        temperature.setOnClickListener(this);
        pregnancy.setOnClickListener(this);
        monitor.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.bt_ovulation:
                Intent intent_o = new Intent(getActivity(), OvulationActivity.class);
                startActivity(intent_o);
                break;
            case R.id.bt_temperature:
                Intent intent_t = new Intent(getActivity(), TemperatureActivity.class);
                startActivity(intent_t);
                break;
            case R.id.bt_pregnancy:
                Intent intent_p = new Intent(getActivity(), PregnancyActivity.class);
                startActivity(intent_p);
                break;
            case R.id.bt_monitor:

                break;
        }
    }
}
