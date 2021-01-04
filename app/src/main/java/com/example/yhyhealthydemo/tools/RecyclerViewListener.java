package com.example.yhyhealthydemo.tools;

import com.example.yhyhealthydemo.datebase.Member;

public interface RecyclerViewListener {
    void onBleConnect(Member member);
    void onDelUser(Member member);
    void onBleChart(Member member);
    void onBleMeasuring(Member member);
}
