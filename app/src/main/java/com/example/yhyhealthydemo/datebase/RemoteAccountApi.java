package com.example.yhyhealthydemo.datebase;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RemoteAccountApi {

    /**
     * success : [{"celsius":25,"fahrenheit":77,"measuredTime":"2021-03-84 15:48:58","name":"Leona","headShot":"base64Str"}]
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
         * celsius : 25.0  (攝氏)
         * fahrenheit : 77.0 (華氏)
         * measuredTime : 2021-03-84 15:48:58
         * name : Leona
         * headShot : base64Str
         */

        @SerializedName("celsius")
        private double RemoteCelsius;

        @SerializedName("fahrenheit")
        private double RemoteFahrenheit;

        private String measuredTime;

        @SerializedName("name")
        private String remoteName;

        @SerializedName("headShot")
        private String remoteHeadShot;

        public double getRemoteCelsius() {
            return RemoteCelsius;
        }

        public void setRemoteCelsius(double remoteCelsius) {
            RemoteCelsius = remoteCelsius;
        }

        public double getRemoteFahrenheit() {
            return RemoteFahrenheit;
        }

        public void setRemoteFahrenheit(double remoteFahrenheit) {
            RemoteFahrenheit = remoteFahrenheit;
        }

        public String getMeasuredTime() {
            return measuredTime;
        }

        public void setMeasuredTime(String measuredTime) {
            this.measuredTime = measuredTime;
        }

        public String getRemoteName() {
            return remoteName;
        }

        public void setRemoteName(String remoteName) {
            this.remoteName = remoteName;
        }

        public String getRemoteHeadShot() {
            return remoteHeadShot;
        }

        public void setRemoteHeadShot(String remoteHeadShot) {
            this.remoteHeadShot = remoteHeadShot;
        }
    }

    /**
     * JSON 字串轉 RemoteAccountApi 物件
     *
     * @param jsonString json 格式的資料
     * @return 物件
     */
    public static RemoteAccountApi newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new RemoteAccountApi();
        }

        Gson gson = new Gson();
        RemoteAccountApi item;

        try {
            item = gson.fromJson(jsonString, RemoteAccountApi.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new RemoteAccountApi();
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
