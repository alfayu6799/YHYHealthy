package com.example.yhyhealthydemo.module;

public enum RecordSymptom {
    NONE("none","無","无",0),
    BURNING("burning","灼熱感","灼热感",1),
    ITCHY("itchy","癢","痒",2),
    PAINFUL("painful","痛","痛",3);

    RecordSymptom(String name, String twName, String cnName, int index){
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
    public static RecordSymptom getSymptom(String enName){
        for (RecordSymptom symptom:values()){
            if (symptom.getName().equals(enName)){
                return symptom;
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
