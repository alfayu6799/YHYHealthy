package com.example.yhyhealthy.datebase;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class TempDataApi {

    private int errorCode;
    private List<TempDataApi.SuccessBean> success;

    public int getErrorCode() {
        return errorCode;
    }

        public List<SuccessBean> getSuccess() {
        return success;
    }

    public static class SuccessBean {
        private int targetId;

        @SerializedName("name")
        private String userName;      //使用者姓名

        private String gender;

        @SerializedName("birthday")
        private String TempBirthday;

        @SerializedName("height")
        private double TempHeight;

        @SerializedName("weight")
        private double TempWeight;

        //private String imgId;  //使用者大頭貼
        private String headShot;

        //private String wifiId;

        public int getTargetId() {
            return targetId;
        }

        public void setTargetId(int targetId) {
            this.targetId = targetId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getTempBirthday() {
            return TempBirthday;
        }

        public void setTempBirthday(String tempBirthday) {
            TempBirthday = tempBirthday;
        }

        public double getTempHeight() {
            return TempHeight;
        }

        public void setTempHeight(double tempHeight) {
            TempHeight = tempHeight;
        }

        public double getTempWeight() {
            return TempWeight;
        }

        public void setTempWeight(double tempWeight) {
            TempWeight = tempWeight;
        }

        public String getHeadShot() {
            return headShot;
        }

        public void setHeadShot(String headShot) {
            this.headShot = headShot;
        }

        //        public String getImgId() {
//            return imgId;
//        }
//
//        public void setImgId(String imgId) {
//            this.imgId = imgId;
//        }
//
//        public String getWifiId() {
//            return wifiId;
//        }

//        public void setWifiId(String wifiId) {
//            this.wifiId = wifiId;
//        }

        //以下是本地端的
        private double degree;       //ble 體溫
        private String Status;       //ble 連線狀態
        private String battery;      //ble 電量
        private String mac;          //ble mac
        private String deviceName;   //ble name
        private List<Degree> degreeList = new ArrayList<>();

        private DateTime alertDateTime; //2021/06/24 紀錄時間

        public void setDegree(double degree , String date){
            this.degree = degree;
            Degree degree1 = new Degree(degree, date);
            degreeList.add(degree1);
        }

        public double getDegree() {
            return degree;
        }

        public List<Degree> getDegreeList() {
            return degreeList;
        }

        public void setDegreeList(List<Degree> degreeList) {
            this.degreeList = degreeList;
        }

        public String getStatus() {
            return Status;
        }

        public void setStatus(String status) {
            Status = status;
        }

        public String getBattery() {
            return battery;
        }

        public void setBattery(String battery) {
            this.battery = battery;
        }

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public DateTime getAlertDateTime() {
            return alertDateTime;
        }

        public void setAlertDateTime(DateTime alertDateTime) {
            this.alertDateTime = alertDateTime;
        }
    }

    /**
     * JSON 字串轉 TempDataApi 物件
     *
     * @param jsonString json 格式的資料
     * @return 物件
     */
    public static TempDataApi newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new TempDataApi();
        }

        Gson gson = new Gson();
        TempDataApi item;

        try {
            item = gson.fromJson(jsonString, TempDataApi.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new TempDataApi();
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
