package com.example.yhyhealthydemo.datebase;

/********************
 *
 *  歷史紀錄資料類別
 *
 * ******************/

public class Record {
    String name;
    String range;

    public Record() {
    }

    public Record(String name, String range) {
        this.name = name;
        this.range = range;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }
}
