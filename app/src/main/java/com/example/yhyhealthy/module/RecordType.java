package com.example.yhyhealthy.module;

public enum RecordType {
    NONE("none","無","无",0),
    VISCOUS("viscous","濃稠狀","浓稠状",1),
    CHEESY("cheesy","乳酪狀","乳酪状",2),
    WATERY("watery","水狀","",3);

    RecordType(String name, String twName, String cnName, int index){
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
    public static RecordType getType(String enName){
        for (RecordType type:values()){
            if (type.getName().equals(enName)){
                return type;
            }
        }
        return null;
    }

    public static RecordType getEnName(int index){
        for (RecordType recordType :values()){
            if(recordType.getIndex() == index){
                return recordType;
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
