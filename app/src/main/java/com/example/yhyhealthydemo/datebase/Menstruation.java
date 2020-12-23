package com.example.yhyhealthydemo.datebase;

/*********************************
 * 經期資料結構DataBean
 * 日期,體溫,經期狀態
 ******************************/
public class Menstruation {
    String periodDate;
    String periodDegree;
    String periodStatus;

    public Menstruation(String periodDate, String periodDegree) {
        this.periodDate = periodDate;
        this.periodDegree = periodDegree;
    }

    public String getPeriodDate() {
        return periodDate;
    }

    public void setPeriodDate(String periodDate) {
        this.periodDate = periodDate;
    }

    public String getPeriodDegree() {
        return periodDegree;
    }

    public void setPeriodDegree(String periodDegree) {
        this.periodDegree = periodDegree;
    }

    public String getPeriodStatus() {
        return periodStatus;
    }

    public void setPeriodStatus(String periodStatus) {
        this.periodStatus = periodStatus;
    }
}
