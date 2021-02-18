package com.example.yhyhealthydemo.datebase;

import com.google.gson.Gson;

/**
 * 更新排卵紀錄DataBean
 * */

public class ChangeRecord {

    /**
     * measure : {"param":"0.8","paramName":"Ovulation","temperature":33.23,"weight":70.2}
     * menstruation :
     * ovuRate : {"btRate":2,"salivaRate":3}
     * secretions : {"color":"yellow","secretionType":"none","smell":"none","symptom":"burning"}
     * status : {"bleeding":false,"breastPain":true,"intercourse":false}
     * testDate : 2021-02-18
     */

    private MeasureBean measure;
    private String menstruation;
    private OvuRateBean ovuRate;
    private SecretionsBean secretions;
    private StatusBean status;
    private String testDate;

    public MeasureBean getMeasure() {
        return measure;
    }

    public void setMeasure(MeasureBean measure) {
        this.measure = measure;
    }

    public String getMenstruation() {
        return menstruation;
    }

    public void setMenstruation(String menstruation) {
        this.menstruation = menstruation;
    }

    public OvuRateBean getOvuRate() {
        return ovuRate;
    }

    public void setOvuRate(OvuRateBean ovuRate) {
        this.ovuRate = ovuRate;
    }

    public SecretionsBean getSecretions() {
        return secretions;
    }

    public void setSecretions(SecretionsBean secretions) {
        this.secretions = secretions;
    }

    public StatusBean getStatus() {
        return status;
    }

    public void setStatus(StatusBean status) {
        this.status = status;
    }

    public String getTestDate() {
        return testDate;
    }

    public void setTestDate(String testDate) {
        this.testDate = testDate;
    }

    public static class MeasureBean {
        /**
         * param : 0.8
         * paramName : Ovulation
         * temperature : 33.23
         * weight : 70.2
         */

        private String param;
        private String paramName;
        private double temperature;
        private double weight;

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

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }
    }

    public static class OvuRateBean {
        /**
         * btRate : 2
         * salivaRate : 3
         */

        private int btRate;
        private int salivaRate;

        public int getBtRate() {
            return btRate;
        }

        public void setBtRate(int btRate) {
            this.btRate = btRate;
        }

        public int getSalivaRate() {
            return salivaRate;
        }

        public void setSalivaRate(int salivaRate) {
            this.salivaRate = salivaRate;
        }
    }


    public static class SecretionsBean {
        /**
         * color : yellow
         * secretionType : none
         * smell : none
         * symptom : burning
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

    public static class StatusBean {
        /**
         * bleeding : false
         * breastPain : true
         * intercourse : false
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
