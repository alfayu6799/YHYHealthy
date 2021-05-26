package com.example.yhyhealthy.datebase;

/***** ***************
 * 系統 - 觀測者 DataBean
 *  大頭照
 *  姓名
 *  性別
 *  生日
 ****** *********/

public class Observation {
    int ObserverImage;
    String ObserverName;
    String ObserverGender;
    String ObserverBirthday;

    public Observation(int observerImage, String observerName, String observerGender, String observerBirthday) {
        ObserverImage = observerImage;
        ObserverName = observerName;
        ObserverGender = observerGender;
        ObserverBirthday = observerBirthday;
    }

    public int getObserverImage() {
        return ObserverImage;
    }

    public void setObserverImage(int observerImage) {
        ObserverImage = observerImage;
    }

    public String getObserverName() {
        return ObserverName;
    }

    public void setObserverName(String observerName) {
        ObserverName = observerName;
    }

    public String getObserverGender() {
        return ObserverGender;
    }

    public void setObserverGender(String observerGender) {
        ObserverGender = observerGender;
    }

    public String getObserverBirthday() {
        return ObserverBirthday;
    }

    public void setObserverBirthday(String observerBirthday) {
        ObserverBirthday = observerBirthday;
    }
}
