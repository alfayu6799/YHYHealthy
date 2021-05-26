package com.example.yhyhealthy.calendar;

import android.graphics.drawable.Drawable;
import android.text.style.RelativeSizeSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.List;

/**** ****  **
* 月經日期 
* ***  ******* */

public class MyEventDecorator implements DayViewDecorator {

    private final Drawable drawable;
    private final List<CalendarDay> dayList;

    public MyEventDecorator(Drawable drawable, List<CalendarDay> dayList) {
        this.drawable = drawable;
        this.dayList = dayList;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dayList.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setSelectionDrawable(drawable);
        view.addSpan(new RelativeSizeSpan(1.2f)); //text's size
    }
}
