package com.example.yhyhealthy.datebase;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.List;

/*********************************
 * 排卵月曆-資料結構DataBean
 * 日期,體溫,經期狀態
 ******************************/
public class Menstruation {

    /**
     * errorCode : 0
     * success : [{"cycleStatus":[1,4],"firstDay":true,"temperature":35.05,"testDate":"2021-02-05"},{"cycleStatus":[1,4],"firstDay":false,"temperature":35.05,"testDate":"2021-02-06"},{"cycleStatus":[1,4],"firstDay":false,"temperature":35.02,"testDate":"2021-02-07"},{"cycleStatus":[1,4],"firstDay":false,"temperature":35.03,"testDate":"2021-02-08"},{"cycleStatus":[1,4],"firstDay":false,"temperature":35.02,"testDate":"2021-02-09"},{"cycleStatus":[5],"firstDay":false,"temperature":35.92,"testDate":"2021-02-14"},{"cycleStatus":[5],"firstDay":false,"temperature":35.05,"testDate":"2021-02-15"},{"cycleStatus":[5],"firstDay":false,"temperature":35.04,"testDate":"2021-02-16"},{"cycleStatus":[6],"firstDay":false,"temperature":34,"testDate":"2021-02-17"},{"cycleStatus":[6],"firstDay":false,"temperature":33,"testDate":"2021-02-18"},{"cycleStatus":[6],"firstDay":false,"temperature":34,"testDate":"2021-02-19"},{"cycleStatus":[5],"firstDay":false,"temperature":36,"testDate":"2021-02-20"},{"cycleStatus":[5],"firstDay":false,"temperature":36.08,"testDate":"2021-02-21"},{"cycleStatus":[5],"firstDay":false,"temperature":36.9,"testDate":"2021-02-22"},{"cycleStatus":[5],"firstDay":false,"temperature":36.23,"testDate":"2021-02-23"},{"cycleStatus":[4],"firstDay":true,"temperature":35.5,"testDate":"2021-02-24"},{"cycleStatus":[4],"firstDay":false,"temperature":35.4,"testDate":"2021-02-28"}]
     */

    private int errorCode;
    private List<SuccessBean> success;

    public int getErrorCode() {
        return errorCode;
    }

    public List<SuccessBean> getSuccess() {
        return success;
    }

    public static class SuccessBean {
        /**
         * cycleStatus : [1,4]
         * firstDay : true
         * temperature : 35.05
         * testDate : 2021-02-05
         */

        private boolean firstDay;
        private double temperature;
        private String testDate;
        private List<Integer> cycleStatus;

        public boolean isFirstDay() {
            return firstDay;
        }

        public void setFirstDay(boolean firstDay) {
            this.firstDay = firstDay;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public String getTestDate() {
            return testDate;
        }

        public void setTestDate(String testDate) {
            this.testDate = testDate;
        }

        public List<Integer> getCycleStatus() {
            return cycleStatus;
        }

        public void setCycleStatus(List<Integer> cycleStatus) {
            this.cycleStatus = cycleStatus;
        }
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
