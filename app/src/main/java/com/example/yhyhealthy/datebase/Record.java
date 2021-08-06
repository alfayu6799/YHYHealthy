package com.example.yhyhealthy.datebase;

import java.util.List;

/********************
 *  歷史紀錄資料類別
 *  create 2021/08/04
 * ******************/

public class Record {

    /**
     * success : {"aido_medicineRecord":[{"medicine":"普拿疼","drug_dose":"1","create_date":"2021-07-02 13:40:35"},{"medicine":"","drug_dose":"","create_date":"2021-07-26 15:10:32"}],"aido_measuredValues":[{"celsius":25.42,"fahrenheit":78,"measured_time":"2021-07-01 14:51:21"},{"celsius":26.27,"fahrenheit":79,"measured_time":"2021-07-01 14:51:26"},{"celsius":26.29,"fahrenheit":79,"measured_time":"2021-07-01 14:51:31"},{"celsius":26.13,"fahrenheit":79,"measured_time":"2021-07-01 14:51:36"},{"celsius":36.51,"fahrenheit":98,"measured_time":"2021-07-01 14:51:41"},{"celsius":36.57,"fahrenheit":98,"measured_time":"2021-07-01 14:51:46"},{"celsius":36.65,"fahrenheit":98,"measured_time":"2021-07-01 14:51:51"},{"celsius":30.47,"fahrenheit":87,"measured_time":"2021-07-01 14:51:56"},{"celsius":27.2,"fahrenheit":81,"measured_time":"2021-07-01 14:52:01"},{"celsius":26.1,"fahrenheit":79,"measured_time":"2021-07-01 14:52:06"},{"celsius":35.86,"fahrenheit":97,"measured_time":"2021-07-01 14:52:11"},{"celsius":35.79,"fahrenheit":96,"measured_time":"2021-07-01 14:52:16"},{"celsius":36.63,"fahrenheit":98,"measured_time":"2021-07-01 14:52:21"},{"celsius":36.74,"fahrenheit":98,"measured_time":"2021-07-01 14:52:26"},{"celsius":36.79,"fahrenheit":98,"measured_time":"2021-07-01 14:52:31"},{"celsius":36.82,"fahrenheit":98,"measured_time":"2021-07-01 14:52:36"},{"celsius":36.83,"fahrenheit":98,"measured_time":"2021-07-01 14:52:41"},{"celsius":36.74,"fahrenheit":98,"measured_time":"2021-07-01 14:52:46"},{"celsius":36.89,"fahrenheit":98,"measured_time":"2021-07-01 14:52:51"},{"celsius":36.96,"fahrenheit":99,"measured_time":"2021-07-01 14:52:56"},{"celsius":37.14,"fahrenheit":99,"measured_time":"2021-07-01 14:53:01"},{"celsius":37.27,"fahrenheit":99,"measured_time":"2021-07-01 14:53:06"},{"celsius":37.37,"fahrenheit":99,"measured_time":"2021-07-01 14:53:11"},{"celsius":37.44,"fahrenheit":99,"measured_time":"2021-07-01 14:53:16"},{"celsius":37.47,"fahrenheit":99,"measured_time":"2021-07-01 14:53:21"},{"celsius":37.5,"fahrenheit":100,"measured_time":"2021-07-01 14:53:26"},{"celsius":37.5,"fahrenheit":100,"measured_time":"2021-07-01 14:53:31"},{"celsius":29.46,"fahrenheit":85,"measured_time":"2021-07-01 14:53:36"},{"celsius":27.2,"fahrenheit":81,"measured_time":"2021-07-01 14:53:41"},{"celsius":26.45,"fahrenheit":80,"measured_time":"2021-07-01 14:53:46"},{"celsius":26.3,"fahrenheit":79,"measured_time":"2021-07-01 14:53:51"},{"celsius":26.37,"fahrenheit":79,"measured_time":"2021-07-01 14:53:56"},{"celsius":26.31,"fahrenheit":79,"measured_time":"2021-07-01 14:54:01"},{"celsius":26.31,"fahrenheit":79,"measured_time":"2021-07-01 14:54:06"},{"celsius":26.38,"fahrenheit":79,"measured_time":"2021-07-01 14:54:11"},{"celsius":26.38,"fahrenheit":79,"measured_time":"2021-07-01 14:54:16"},{"celsius":26.31,"fahrenheit":79,"measured_time":"2021-07-01 14:54:21"},{"celsius":26.32,"fahrenheit":79,"measured_time":"2021-07-01 14:54:26"},{"celsius":36.8,"fahrenheit":98,"measured_time":"2021-07-01 14:54:48"},{"celsius":26.02,"fahrenheit":79,"measured_time":"2021-07-01 14:57:15"},{"celsius":25.62,"fahrenheit":78,"measured_time":"2021-07-01 14:59:42"},{"celsius":26.32,"fahrenheit":79,"measured_time":"2021-07-01 15:30:26"}],"aido_symptom":[{"vaccine_name":"","covid19_vaccine":"","other_brand":"","pain_at_the_vaccination_site":false,"swelling_redness":false,"fever":true,"headache":false,"fatigue":false,"sore_muscles_joints":false,"sore_throat":false,"cough":false,"runny_nose":false,"diarrhea":false,"chest_tightness":true,"shortness_breath":false,"taste":false,"vomiting":false,"chills":false,"other":"","sputum_color":"none","sputum_type":"foamy","nose_color":"yellowGreen","nose_type":"none","create_date":"2021-07-01 14:37:44"}],"ainiita":[]}
     * errorCode : 0
     */

    private SuccessBean success;
    private int errorCode;

    public static class SuccessBean {
        private List<AidoMedicineRecordBean> aido_medicineRecord;
        private List<AidoMeasuredValuesBean> aido_measuredValues;
        private List<AidoSymptomBean> aido_symptom;
        private List<?> ainiita;

        public static class AidoMedicineRecordBean {
            /**
             * medicine : 普拿疼
             * drug_dose : 1
             * create_date : 2021-07-02 13:40:35
             */

            private String medicine;
            private String drug_dose;
            private String create_date;
        }

        public static class AidoMeasuredValuesBean {
            /**
             * celsius : 25.42
             * fahrenheit : 78
             * measured_time : 2021-07-01 14:51:21
             */

            private double celsius;
            private int fahrenheit;
            private String measured_time;
        }

        public static class AidoSymptomBean {
            /**
             * vaccine_name :
             * covid19_vaccine :
             * other_brand :
             * pain_at_the_vaccination_site : false
             * swelling_redness : false
             * fever : true
             * headache : false
             * fatigue : false
             * sore_muscles_joints : false
             * sore_throat : false
             * cough : false
             * runny_nose : false
             * diarrhea : false
             * chest_tightness : true
             * shortness_breath : false
             * taste : false
             * vomiting : false
             * chills : false
             * other :
             * sputum_color : none
             * sputum_type : foamy
             * nose_color : yellowGreen
             * nose_type : none
             * create_date : 2021-07-01 14:37:44
             */

            private String vaccine_name;
            private String covid19_vaccine;
            private String other_brand;
            private boolean pain_at_the_vaccination_site;
            private boolean swelling_redness;
            private boolean fever;
            private boolean headache;
            private boolean fatigue;
            private boolean sore_muscles_joints;
            private boolean sore_throat;
            private boolean cough;
            private boolean runny_nose;
            private boolean diarrhea;
            private boolean chest_tightness;
            private boolean shortness_breath;
            private boolean taste;
            private boolean vomiting;
            private boolean chills;
            private String other;
            private String sputum_color;
            private String sputum_type;
            private String nose_color;
            private String nose_type;
            private String create_date;
        }
    }
}
