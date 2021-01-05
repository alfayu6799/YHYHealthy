package com.example.yhyhealthydemo.datebase;

import android.text.TextUtils;

import com.google.gson.Gson;

public class MenstruationRecord {

    /**
     * testDate : 2020/08/03
     * menstruation : S
     * measure : {"param":"0.87","paramName":"ABC","temperature":39.99272209564263,"weight":87}
     * status : {"bleeding":true,"breastPain":true,"intercourse":true}
     * secretions : {"color":"egg-white","secretionType":"viscous","smell":"none","symptom":"none"}
     * ovuRate : {"salivaRate":1,"btRate":3}
     */

    private String testDate;
    private String menstruation;
    private MeasureBean measure;
    private StatusBean status;
    private SecretionsBean secretions;
    private OvuRateBean ovuRate;

    public String getTestDate() {
        return testDate;
    }

    public void setTestDate(String testDate) {
        this.testDate = testDate;
    }

    public String getMenstruation() {
        return menstruation;
    }

    public void setMenstruation(String menstruation) {
        this.menstruation = menstruation;
    }

    public MeasureBean getMeasure() {
        return measure;
    }

    public void setMeasure(MeasureBean measure) {
        this.measure = measure;
    }

    public StatusBean getStatus() {
        return status;
    }

    public void setStatus(StatusBean status) {
        this.status = status;
    }

    public SecretionsBean getSecretions() {
        return secretions;
    }

    public void setSecretions(SecretionsBean secretions) {
        this.secretions = secretions;
    }

    public OvuRateBean getOvuRate() {
        return ovuRate;
    }

    public void setOvuRate(OvuRateBean ovuRate) {
        this.ovuRate = ovuRate;
    }

    public static class MeasureBean {
        /**
         * param : 0.87
         * paramName : ABC
         * temperature : 39.99272209564263
         * weight : 87
         */

        private String param;
        private String paramName;
        private double temperature;
        private int weight;

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }

        public String getParamName() {
            return paramName;
        }

        public void setParamName(String paramName) {
            this.paramName = paramName;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }
    }

    public static class StatusBean {
        /**
         * bleeding : true
         * breastPain : true
         * intercourse : true
         */

        private boolean bleeding;
        private boolean breastPain;
        private boolean intercourse;

        public boolean isBleeding() {
            return bleeding;
        }

        public void setBleeding(boolean bleeding) {
            this.bleeding = bleeding;
        }

        public boolean isBreastPain() {
            return breastPain;
        }

        public void setBreastPain(boolean breastPain) {
            this.breastPain = breastPain;
        }

        public boolean isIntercourse() {
            return intercourse;
        }

        public void setIntercourse(boolean intercourse) {
            this.intercourse = intercourse;
        }
    }

    public static class SecretionsBean {
        /**
         * color : egg-white
         * secretionType : viscous
         * smell : none
         * symptom : none
         */

        private String color;
        private String secretionType;
        private String smell;
        private String symptom;

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getSecretionType() {
            return secretionType;
        }

        public void setSecretionType(String secretionType) {
            this.secretionType = secretionType;
        }

        public String getSmell() {
            return smell;
        }

        public void setSmell(String smell) {
            this.smell = smell;
        }

        public String getSymptom() {
            return symptom;
        }

        public void setSymptom(String symptom) {
            this.symptom = symptom;
        }
    }

    public static class OvuRateBean {
        /**
         * salivaRate : 1
         * btRate : 3
         */

        private int salivaRate;
        private int btRate;

        public int getSalivaRate() {
            return salivaRate;
        }

        public void setSalivaRate(int salivaRate) {
            this.salivaRate = salivaRate;
        }

        public int getBtRate() {
            return btRate;
        }

        public void setBtRate(int btRate) {
            this.btRate = btRate;
        }
    }

    /**
     * JSON 字串轉 Menstruation 物件
     *
     * @param jsonString json 格式的資料
     * @return TemperatureReceives 物件
     */
    public static MenstruationRecord newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new MenstruationRecord();
        }

        Gson gson = new Gson();
        MenstruationRecord item;

        try {
            item = gson.fromJson(jsonString, MenstruationRecord.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new MenstruationRecord();
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
