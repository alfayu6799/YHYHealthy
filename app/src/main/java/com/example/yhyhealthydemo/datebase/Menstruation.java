package com.example.yhyhealthydemo.datebase;

/*********************************
 * 經期資料結構DataBean
 * 日期,體溫,經期狀態
 ******************************/
public class Menstruation {

    /**
     * testDate : 2020/08/01
     * temperature : 37.82542905043851
     * cycleStatus : 1
     */

    private String testDate;
    private double temperature;
    private int cycleStatus;

    public String getTestDate() {
        return testDate;
    }

    public void setTestDate(String testDate) {
        this.testDate = testDate;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getCycleStatus() {
        return cycleStatus;
    }

    public void setCycleStatus(int cycleStatus) {
        this.cycleStatus = cycleStatus;
    }
}
