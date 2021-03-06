package com.example.yhyhealthy.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.example.yhyhealthy.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

/** ***************
 * 自定義 MakerView
 * ** *************/

public class LineChartMarkView extends MarkerView {
    private TextView tvDate;
    private TextView tvValue;
    private IAxisValueFormatter xAxisValueFormatter;

    public LineChartMarkView(Context context, IAxisValueFormatter xAxisValueFormatter) {
        super(context, R.layout.layout_markview);
        this.xAxisValueFormatter = xAxisValueFormatter;

        tvDate = findViewById(R.id.tv_date);
        tvValue = findViewById(R.id.tv_value);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
//        String dateStr = xAxisValueFormatter.getFormattedValue(e.getX(), null);
        tvDate.setText(xAxisValueFormatter.getFormattedValue(e.getX(), null));  //日期
//        tvDate.setText("" + dateStr);\
        tvValue.setText("" + e.getY() + "\u2103");  //溫度
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
