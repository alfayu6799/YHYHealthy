package com.example.yhyhealthydemo.data;


/***********************
 *  圖表資料集
 *  day = 日期
 *  temperature = 體溫
* *********************/

public class LineChartData {
    String date;
    double temperature;

    public LineChartData(String date, double temperature) {
        this.date = date;
        this.temperature = temperature;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}
