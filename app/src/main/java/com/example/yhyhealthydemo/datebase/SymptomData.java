package com.example.yhyhealthydemo.datebase;

import java.util.List;

/**  **************
 * 症狀專用DataBean
 * 因為value值有boolean跟Array所以要自己設計
 * create: 2021/04/13
 * *************       *****/

public class SymptomData {

    private static final String TAG = "TestData";

    private int errorCode;

    private List<SwitchItemBean> switchItemList; //上半部

    private List<CheckBoxGroup> checkBoxGroupList;  //下半部

    public static class SwitchItemBean {
        private String key;
        private boolean value;

        public String getKey() {
            return key;
        }

        public boolean isValue() {
            return value;
        }

        public SwitchItemBean(String key, boolean value){
            this.key = key;
            this.value = value;
        }
    }

    public static class CheckBoxGroup {
        private String key;
        private List<String > value;

        public String getKey() {
            return key;
        }

        public List<String> getValue() {
            return value;
        }

        public CheckBoxGroup(String key, List<String> value) {
            this.key = key;
            this.value = value;
        }
    }

    public int getErrorCode() {
        return errorCode;
    }

    public List<SwitchItemBean> getSwitchItemList() {
        return switchItemList;
    }

    public List<CheckBoxGroup> getCheckBoxGroupList() {
        return checkBoxGroupList;
    }

}
