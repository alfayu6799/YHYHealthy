package com.example.yhyhealthy.module;

public enum RecordColor {
    NONE("none","無","无",0),
    EGGWHITE("egg-white","蛋清色","蛋清色",1),
    YELLOW("yellow","黃色","黄色",2),
    MILKY("milky","乳白色","乳白色",3),
    BROWN("brown","褐色","褐色",4),
    YELLOWGREEN("yellow-green","黃綠色","黄绿色",5);

    RecordColor (String name, String twName, String cnName, int index){
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
    public static RecordColor getColor(String enName){
        for (RecordColor color:values()){
            if (color.getName().equals(enName)){
                return color;
            }
        }
        return null;
    }

    public static RecordColor getEnName(int index){
        for (RecordColor recordColor:values()){
            if(recordColor.getIndex() == index){
                return recordColor;
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
