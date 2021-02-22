package com.example.yhyhealthydemo.datebase;

import android.text.TextUtils;

import com.google.gson.Gson;

import static android.content.ContentValues.TAG;

/*********************************
 * 排卵月曆-資料結構DataBean
 * 日期,體溫,經期狀態
 ******************************/
public class Menstruation {

    /**
     * testDate : 2020/08/01
     * temperature : 37.82542905043851
     * cycleStatus : 1
     */

    private String testDate;
    private double temperature;
    private int cycleStatus;

    public String getTestDate() {
        return testDate;
    }

    public void setTestDate(String testDate) {
        this.testDate = testDate;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getCycleStatus() {
        return cycleStatus;
    }

    public void setCycleStatus(int cycleStatus) {
        this.cycleStatus = cycleStatus;
    }

    /**
     * JSON 字串轉 Menstruation 物件
     *
     * @param jsonString json 格式的資料
     * @return 物件
     */
    public static Menstruation newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new Menstruation();
        }

        Gson gson = new Gson();
        Menstruation item;

        try {
            item = gson.fromJson(jsonString, Menstruation.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new Menstruation();
        }

        return item;
    }

    /**
     * SignInAPI 物件轉 JSON字串
     *
     * @return json 格式的資料
     */
    public String toJSONString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
