package com.example.yhyhealthydemo.datebase;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.List;

public class DegreeUserData {

    private int errorCode;
    private List<SuccessBean> success;

    public int getErrorCode() {
        return errorCode;
    }

    public List<SuccessBean> getSuccess() {
        return success;
    }

    public static class SuccessBean {
        private int targetId;
        private String name;
        private String gender;
        private String birthday;
        private double height;
        private double weight;
        private String headShot;

        public int getTargetId() {
            return targetId;
        }

        public void setTargetId(int targetId) {
            this.targetId = targetId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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
    }

    /**
     * JSON 字串轉 DegreeUserData 物件
     *
     * @param jsonString json 格式的資料
     * @return 物件
     */
    public static DegreeUserData newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new DegreeUserData();
        }

        Gson gson = new Gson();
        DegreeUserData item;

        try {
            item = gson.fromJson(jsonString, DegreeUserData.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new DegreeUserData();
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
