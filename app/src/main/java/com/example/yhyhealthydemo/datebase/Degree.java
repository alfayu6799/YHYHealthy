package com.example.yhyhealthydemo.datebase;

public class Degree {
    Double degree;
    String Date;

    public Degree(Double degree, String date) {
        this.degree = degree;
        Date = date;
    }

    public Double getDegree() {
        return degree;
    }

    public void setDegree(Double degree) {
        this.degree = degree;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }
}
