package com.example.yhyhealthydemo.module;

import android.text.TextUtils;

import com.google.gson.Gson;

/*********************************
 * 排卵紀錄資訊-資料結構DataBean
 * 日期,經期
 * 唾液辨識結果
 * 經期期間 - 非經期出血 , 脹痛 , 行房
 * 分泌物
 * 唾液辨識之體溫與排卵機率
 ******************************/

public class MenstruationRecord {

    /**
     * testDate : 2021-01-08
     * menstruation :
     * measure : {"param":"","paramName":"","temperature":0,"weight":0}
     * status : {"bleeding":true,"breastPain":false,"intercourse":false}
     * secretions : {"color":"milky","secretionType":"viscous","smell":"fishy-smell","symptom":"burning"}
     * ovuRate : {"salivaRate":2,"btRate":1}
     */

    private String testDate;
    private String menstruation;
    private MeasureBean measure;
    private StatusBean status;               //狀態
    private SecretionsBean secretions;
    private OvuRateBean ovuRate;            //機率

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
         * param :
         * paramName :
         * temperature : 0.0
         * weight : 0.0
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

    public static class StatusBean {
        /**
         * bleeding : true
         * breastPain : false
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

    public static class SecretionsBean {
        /**
         * color : milky
         * secretionType : viscous
         * smell : fishy-smell
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

    public static class OvuRateBean {
        /**
         * salivaRate : 2
         * btRate : 1
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
