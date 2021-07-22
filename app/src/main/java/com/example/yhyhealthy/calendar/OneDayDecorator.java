package com.example.yhyhealthy.calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.example.yhyhealthy.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import org.threeten.bp.LocalDate;

public class OneDayDecorator implements DayViewDecorator {

    private CalendarDay date;
    private final Drawable drawable;

    @SuppressLint("UseCompatLoadingForDrawables")
    public OneDayDecorator(Context context) {
        date = CalendarDay.today();
        drawable = context.getResources().getDrawable(R.drawable.my_selector);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return date != null && day.equals(date);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setSelectionDrawable(drawable);
        view.addSpan(new StyleSpan(Typeface.BOLD));            //text font's size
        view.addSpan(new RelativeSizeSpan(1.2f));   //text's size
        view.addSpan(new ForegroundColorSpan(Color.WHITE));   //text's color
    }

    public void setDate(LocalDate date) {
        this.date = CalendarDay.from(date);
    }
}
