package com.example.yhyhealthy.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by leona on 2020/12/01.
 * description:日期時間轉換
 */
public class DateUtil {

    //年月日 --> 月日
    public static String formatDateToMD(String str) {
        SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sf2 = new SimpleDateFormat("MMdd");
        String formatStr = "";
        try {
            formatStr = sf2.format(sf1.parse(str));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formatStr;
    }

    //年月日 --> 年-月-日
    public static String formatDateToYMD(String str) {
        SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd");
        String formatStr = "";
        try {
            formatStr = sf2.format(sf1.parse(str));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formatStr;
    }

    //年/月/ 時:分  --> 時:分:秒
    public static String fromDateToTime(String str){
        SimpleDateFormat sf1 = new SimpleDateFormat("MM/dd HH:mm:ss");
        SimpleDateFormat sf2 = new SimpleDateFormat("HH:mm:ss");
        String formatStr = "";
        try {
            formatStr = sf2.format(sf1.parse(str));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formatStr;
    }
}
