package com.example.yhyhealthydemo.datebase;

import android.text.TextUtils;

import com.google.gson.Gson;

/*****
 * 使用者經期設定 dataBean
 * */
public class PeriodData {

    /**
     * success : {"type":"","userId":"","cycle":28,"period":5,"lastDate":"2019-09-30","endDate":"2019-10-04"}
     * errorCode : 0
     */

    private SuccessBean success;
    private int errorCode;

    public SuccessBean getSuccess() {
        return success;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public static class SuccessBean {
        /**
         * type :
         * userId :
         * cycle : 28
         * period : 5
         * lastDate : 2019-09-30
         * endDate : 2019-10-04
         */

        private String type;
        private String userId;
        private int cycle;
        private int period;
        private String lastDate;
        private String endDate;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

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
    }

    /**
     * JSON 字串轉物件
     *
     * @param jsonString json 格式的資料
     * @return TemperatureReceives 物件
     */
    public static PeriodData newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new PeriodData();
        }

        Gson gson = new Gson();
        PeriodData item;

        try {
            item = gson.fromJson(jsonString, PeriodData.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new PeriodData();
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
