package com.example.yhyhealthy.datebase;

import android.text.TextUtils;
import com.google.gson.Gson;
import java.util.List;

public class CycleRecord {

    /**
     * success : [{"testDate":"2021-02-05","temperature":0,"firstDay":0,"cycleStatus":[1,4]},{"testDate":"2021-02-06","temperature":0,"firstDay":false,"cycleStatus":[1,4]},{"testDate":"2021-02-07","temperature":0,"firstDay":false,"cycleStatus":[1,4]},{"testDate":"2021-02-08","temperature":0,"firstDay":false,"cycleStatus":[1,4]},{"testDate":"2021-02-09","temperature":0,"firstDay":false,"cycleStatus":[1,4]},{"testDate":"2021-02-14","temperature":0,"firstDay":false,"cycleStatus":[5]},{"testDate":"2021-02-15","temperature":0,"firstDay":false,"cycleStatus":[5]},{"testDate":"2021-02-16","temperature":0,"firstDay":false,"cycleStatus":[5]},{"testDate":"2021-02-17","temperature":0,"firstDay":false,"cycleStatus":[6]},{"testDate":"2021-02-18","temperature":0,"firstDay":false,"cycleStatus":[6]},{"testDate":"2021-02-19","temperature":0,"firstDay":false,"cycleStatus":[6]},{"testDate":"2021-02-20","temperature":0,"firstDay":false,"cycleStatus":[5]},{"testDate":"2021-02-21","temperature":0,"firstDay":false,"cycleStatus":[5]},{"testDate":"2021-02-22","temperature":0,"firstDay":false,"cycleStatus":[5]},{"testDate":"2021-02-23","temperature":0,"firstDay":false,"cycleStatus":[5]},{"testDate":"2021-03-05","temperature":0,"firstDay":true,"cycleStatus":[4]},{"testDate":"2021-03-06","temperature":0,"firstDay":false,"cycleStatus":[4]}]
     * errorCode : 0
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
         * testDate : 2021-02-05
         * temperature : 0.0
         * firstDay : 0
         * cycleStatus : [1,4]
         */

        private String testDate;
        private double temperature;
        private int firstDay;
        private List<Integer> cycleStatus;

        public String getTestDate() {
            return testDate;
        }

        public double getTemperature() {
            return temperature;
        }

        public int getFirstDay() {
            return firstDay;
        }

        public List<Integer> getCycleStatus() {
            return cycleStatus;
        }

        public void setTestDate(String testDate) {
            this.testDate = testDate;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
    }

    /**
     * JSON 字串轉  物件
     *
     * @param jsonString json 格式的資料
     * @return 物件
     */
    public static CycleRecord newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new CycleRecord();
        }

        Gson gson = new Gson();
        CycleRecord item;

        try {
            item = gson.fromJson(jsonString, CycleRecord.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new CycleRecord();
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
