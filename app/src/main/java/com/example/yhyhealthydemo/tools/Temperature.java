package com.example.yhyhealthydemo.tools;

/*******************************
 * 藍芽體溫DateBean
 * 日期 measureDate  : 20201127
 * 溫度 measureValue : 30.55
 * *****************************/

public class Temperature {
    private String measureDate;  //日期
    private Double measureValue; //體溫

    public String getMeasureDate() {
        return measureDate;
    }

    public void setMeasureDate(String measureDate) {
        this.measureDate = measureDate;
    }

    public Double getMeasureValue() {
        return measureValue;
    }

    public void setMeasureValue(Double measureValue) {
        this.measureValue = measureValue;
    }
}
