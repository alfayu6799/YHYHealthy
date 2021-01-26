package com.example.yhyhealthydemo.datebase;

import com.google.gson.Gson;

public class ChangeUserPeriodApi {

    /**
     * cycle : 28
     * period : 5
     * lastDate : 2019-09-30
     * endDate : 2019-10-04
     */

    private int cycle;
    private int period;
    private String lastDate;
    private String endDate;

    public int getCycle() {
        return cycle;
    }

    public void setCycle(int cycle) {
        this.cycle = cycle;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public String getLastDate() {
        return lastDate;
    }

    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
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
