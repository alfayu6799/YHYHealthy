package com.example.yhyhealthy.tools;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import sun.bob.mcalendarview.utils.ExpCalendarUtil;

/***********************
 *
 *  月曆-週顯示 (被景色 , text顏色)
 *
 ***********************/
public class WeekColumnView extends LinearLayout {

    private int backgroundColor = Color.rgb(105, 75, 125);
    private int startendTextColor = Color.rgb(188, 150, 211);
    private int midTextColor = Color.WHITE;

    public WeekColumnView(Context context) {
        super(context);
        init();
    }

    public WeekColumnView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        initParams();
        initLayout();
        initViews();
    }

    private void initParams() {
        backgroundColor = Color.TRANSPARENT;
        startendTextColor = Color.rgb(126,126,127); //週六&週日文字顏色
        midTextColor = Color.rgb(126,126,127);      //周一到周五文字顏色
    }

    private void initLayout() {
        this.setOrientation(HORIZONTAL);
        this.setBackgroundColor(backgroundColor);
    }


    private TextView[] textView = new TextView[7];

    private void initViews() {
        LayoutParams lp = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);

        textView[0] = new TextView(getContext());
        textView[0].setText(ExpCalendarUtil.number2Week(7));
        textView[0].setTextColor(startendTextColor);
        textView[0].setGravity(Gravity.CENTER);
        this.addView(textView[0], lp);

        for (int i = 1; i < 6; i++) {
            textView[i] = new TextView(getContext());
            textView[i].setGravity(Gravity.CENTER);
            textConfig(textView[i], ExpCalendarUtil.number2Week(i), midTextColor);
            this.addView(textView[i], lp);
        }

        textView[6] = new TextView(getContext());
        textView[6].setText(ExpCalendarUtil.number2Week(6));
        textView[6].setTextColor(startendTextColor);
        textView[6].setGravity(Gravity.CENTER);
        this.addView(textView[6], lp);
    }

    /**
     * is this nessesary ?
     *
     * @param textview
     * @param text
     * @param textColor
     */
    private void textConfig(TextView textview, String text, int textColor) {
        textview.setText(text);
        textview.setTextColor(textColor);
    }
}
