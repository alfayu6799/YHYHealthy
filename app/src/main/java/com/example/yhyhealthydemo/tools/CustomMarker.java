package com.example.yhyhealthydemo.tools;

import android.content.Context;
import android.widget.TextView;

import com.example.yhyhealthydemo.R;
import com.example.yhyhealthydemo.datebase.CycleRecord;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.List;

/**   ***************
 * 圖表MakerView客製
 * create 2021/04/20
 * * *****************  **/
public class CustomMarker extends MarkerView {

    private TextView tvDate;
    private TextView tvValue;
    private List<CycleRecord.SuccessBean> dataList;

    public CustomMarker(Context context, List<CycleRecord.SuccessBean> dataList) {
        //布局文件
        super(context, R.layout.layout_markview);
        this.dataList = dataList;
        tvDate = findViewById(R.id.tv_date);
        tvValue = findViewById(R.id.tv_value);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        CycleRecord.SuccessBean item = dataList.get((int) e.getX());
        String[] dateStr = item.getTestDate().split("-");
        String testDay = dateStr[1]+"/"+dateStr[2];  //2021/04/20 加上月/日 markerView
//        tvDate.setText(item.getTestDate());
        tvDate.setText(testDay);                     //日期
        tvValue.setText("" + e.getY() + "\u2103");  //溫度
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
