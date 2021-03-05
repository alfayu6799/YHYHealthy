package com.example.yhyhealthydemo.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.example.yhyhealthydemo.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class CustomMarkerView extends MarkerView {

    private TextView tvContent;

    private MPPointF mOffset;

    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = (TextView) findViewById(R.id.tvContent);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        tvContent.setText("" + e.getX());
    }


    @Override
    public MPPointF getOffset() {
        //设置MarkerView的偏移量，就是提示框显示的位置
        if (mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
        }

        return mOffset;
    }
}
