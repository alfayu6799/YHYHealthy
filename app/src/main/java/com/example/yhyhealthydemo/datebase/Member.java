package com.example.yhyhealthydemo.datebase;


import com.example.yhyhealthydemo.datebase.Degree;

import java.util.ArrayList;
import java.util.List;

/*******************************
 * 藍芽體溫Recycler使用者DataBean
 * 大頭貼 姓名
*******************************/

public class Member {

    private int image;
    private String name;
    private double degree; //體溫
    private String Status; //連線狀態
    private String battery;  //電量
    private String mac;      //ble's mac

    private List<Degree> degreeList = new ArrayList<>();

    public Member(int image, String name) {
        this.image = image;
        this.name = name;
    }

    public void setDegree(double degree , String date){
        this.degree = degree;
        Degree degree1 = new Degree(degree, date);
        degreeList.add(degree1);
    }

    public double getDegree() {
        return degree;
    }

    public List<Degree> getDegreeList() {
        return degreeList;
    }

    public void setDegreeList(List<Degree> degreeList) {
        this.degreeList = degreeList;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
