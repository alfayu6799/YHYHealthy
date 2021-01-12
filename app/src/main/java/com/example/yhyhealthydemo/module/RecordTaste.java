package com.example.yhyhealthydemo.module;

public enum RecordTaste {
    NONE("none","無","无",0),
    FISHY("fishy-smell","魚腥味","鱼腥味",1),
    MALODOROUS("malodorous","惡臭味","恶臭味",2);

    RecordTaste(String name, String twName, String cnName, int index){
        this.name = name;
        this.twName = twName;
        this.cnName = cnName;
        this.index = index;
    }

    private final String name;
    private final String twName;
    private final String cnName;
    private final int index;

    // 普通靜態方法方法 可通過列舉類直接呼叫
    public static RecordTaste getTaste(String enName){
        for (RecordTaste taste:values()){
            if (taste.getName().equals(enName)){
                return taste;
            }
        }
        return null;
    }

    public static RecordTaste getEnName(int index){
        for (RecordTaste recordTaste:values()){
            if(recordTaste.getIndex() == index){
                return recordTaste;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getTwName() {
        return twName;
    }

    public String getCnName() {
        return cnName;
    }

    public int getIndex() {
        return index;
    }
}
