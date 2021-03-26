package com.example.yhyhealthydemo.datebase;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

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
        private String birthday;
        private double height;
        private double weight;
        private String headShot;  //使用者大頭貼

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

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public String getHeadShot() {
            return headShot;
        }

        public void setHeadShot(String headShot) {
            this.headShot = headShot;
        }

        //以下是本地端的
        private double degree;       //體溫
        private String Status;       //連線狀態
        private String battery;      //電量
        private String mac;         //ble's mac
        private String deviceName;  //ble's name
        private List<Degree> degreeList = new ArrayList<>();

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
