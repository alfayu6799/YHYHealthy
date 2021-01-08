package com.example.yhyhealthydemo.datebase;

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
     * type :
     * userId :
     * testDate : 2021-01-06
     * menstruation :
     * measure : {"param":"","paramName":"","temperature":0,"weight":0}
     * status : {"bleeding":"N","beastPain":"N","intercourse":"N"}
     * secretions : {"color":"none","secretionType":"none","smell":"none","symptom":"none"}
     * ovuRate : {"salivaRate":1,"btRate":1}
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
         * param :
         * paramName :
         * temperature : 0.0
         * weight : 0.0
         */
        private String param;
        private String paramName;
        private double temperature;
        private double weight;
    }

    public static class StatusBean {
        /**
         * bleeding : N
         * beastPain : N
         * intercourse : N
         */

        private String bleeding;
        private String beastPain;
        private String intercourse;

        public String getBleeding() {
            return bleeding;
        }

        public void setBleeding(String bleeding) {
            this.bleeding = bleeding;
        }

        public String getBeastPain() {
            return beastPain;
        }

        public void setBeastPain(String beastPain) {
            this.beastPain = beastPain;
        }

        public String getIntercourse() {
            return intercourse;
        }

        public void setIntercourse(String intercourse) {
            this.intercourse = intercourse;
        }
    }

    public static class SecretionsBean {
        /**
         * color : none
         * secretionType : none
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
         * btRate : 1
         */

        private int salivaRate;
        private int btRate;
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
