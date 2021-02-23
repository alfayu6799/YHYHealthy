package com.example.yhyhealthydemo.tools;

import com.example.yhyhealthydemo.datebase.CycleRecord;

import java.util.Arrays;
import java.util.List;

import sun.bob.mcalendarview.vo.DateData;

public class Math {
    private CycleRecord.SuccessBean date;
    private int YEAR = 0;
    private int MONTH = 1;
    private int DAY = 2;
    private int BLANK = 0;
    private int SINGLE = 0;
    private int FILLED = 0;
    private int DOTTED = 1;
    private int resID = 1235678;

    public Math(CycleRecord.SuccessBean date) {
        this.date = date;
    }

    public DateData getDateData()
    {
        String dateTxt = date.getTestDate();
        List<String> items = Arrays.asList(dateTxt.split("-"));
        int dYear = Integer.parseInt(items.get(YEAR));
        int dMonth = Integer.parseInt(items.get(MONTH));
        int dDay = Integer.parseInt(items.get(DAY));
        return new DateData(dYear, dMonth, dDay);
    }

    public int getCalenderBgResID()
    {
        List<Integer> statusCode = date.getCycleStatus();

        //陣列個數判斷
        if(statusCode.size()==1)
        {
            if (statusCode.get(SINGLE)==1)
                return 1;
            if (statusCode.get(SINGLE)==2)
                return 2;
            if (statusCode.get(SINGLE)==3)
                return 3;
            if (statusCode.get(SINGLE)==4)
                return 4;
            if (statusCode.get(SINGLE)==5)
                return 5;
            if (statusCode.get(SINGLE)==6)
                return 6;
            if (statusCode.get(SINGLE)==7)
                return 7;
        }

        else if(statusCode.size()==2)
        {
            if (statusCode.get(DOTTED)==5)
            {
                if (statusCode.get(FILLED)==0)
                    return 8;
                if (statusCode.get(FILLED)==1)
                    return 9;
                if (statusCode.get(FILLED)==2)
                    return 10;
                if (statusCode.get(FILLED)==3)
                    return 11;
                if (statusCode.get(FILLED)==4)
                    return 12;
            }

            if (statusCode.get(DOTTED)==6)
            {
                if (statusCode.get(FILLED)==0)
                    return 13;
                if (statusCode.get(FILLED)==1)
                    return 14;
                if (statusCode.get(FILLED)==2)
                    return 15;
                if (statusCode.get(FILLED)==3)
                    return 16;
                if (statusCode.get(FILLED)==4)
                    return 17;
            }

            if (statusCode.get(DOTTED)==7)
            {
                if (statusCode.get(FILLED)==0)
                    return 18;
                if (statusCode.get(FILLED)==1)
                    return 19;
                if (statusCode.get(FILLED)==2)
                    return 20;
                if (statusCode.get(FILLED)==3)
                    return 21;
                if (statusCode.get(FILLED)==4)
                    return 22;
            }

            if (statusCode.get(DOTTED)==4)
            {
                if (statusCode.get(FILLED)==0)
                    return 23;
                if (statusCode.get(FILLED)==1)
                    return 24;
                if (statusCode.get(FILLED)==2)
                    return 25;
                if (statusCode.get(FILLED)==3)
                    return 26;
                if (statusCode.get(FILLED)==4)
                    return 27;
            }

        }
        //default
        return BLANK;
    }
}
