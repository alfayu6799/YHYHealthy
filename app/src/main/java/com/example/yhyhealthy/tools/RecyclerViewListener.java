package com.example.yhyhealthy.tools;

import com.example.yhyhealthy.datebase.Member;

public interface RecyclerViewListener {
    void onBleConnect(Member member);
    void onDelUser(Member member);
    void onBleChart(Member member);
    void onBleMeasuring(Member member);
}
