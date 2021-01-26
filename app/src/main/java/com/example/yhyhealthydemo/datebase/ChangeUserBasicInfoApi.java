package com.example.yhyhealthydemo.datebase;

import android.text.TextUtils;

import com.google.gson.Gson;

public class ChangeUserBasicInfoApi {

    /**
     * birthday : 2009-01-21
     * email :
     * gender : F
     * height : 163.0
     * mobile :
     * name : leona
     * telCode :
     * userAccount : demo23
     * weight : 0.0
     */

    private String birthday;
    private String email;
    private String gender;
    private String mobile;
    private String name;
    private String telCode;
    private String userAccount;
    private double height;
    private double weight;

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelCode() {
        return telCode;
    }

    public void setTelCode(String telCode) {
        this.telCode = telCode;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
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

    /**
     * JSON 字串轉 物件
     *
     * @param jsonString json 格式的資料
     * @return TemperatureReceives 物件
     */
    public static ChangeUserBasicInfoApi newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new ChangeUserBasicInfoApi();
        }

        Gson gson = new Gson();
        ChangeUserBasicInfoApi item;

        try {
            item = gson.fromJson(jsonString, ChangeUserBasicInfoApi.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new ChangeUserBasicInfoApi();
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
