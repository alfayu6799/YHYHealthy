package com.example.yhyhealthydemo.datebase;

import java.util.List;

public class SymptomData {

    private int errorCode;
    private List<SuccessBean> success;

    public int getErrorCode() {
        return errorCode;
    }

    public List<SuccessBean> getSuccess() {
        return success;
    }

    public static class SuccessBean {

        //recyclerView上半部使用
        private List<SwitchItemBean> switchItemList;

        //recyclerView下半部使用
        private List<CheckBoxGroupBean> checkBoxGroupList;

        public List<SwitchItemBean> getSwitchItemList() {
            return switchItemList;
        }

        public void setSwitchItemList(List<SwitchItemBean> switchItemList) {
            this.switchItemList = switchItemList;
        }

        public List<CheckBoxGroupBean> getCheckBoxGroupList() {
            return checkBoxGroupList;
        }

        public void setCheckBoxGroupList(List<CheckBoxGroupBean> checkBoxGroupList) {
            this.checkBoxGroupList = checkBoxGroupList;
        }

        private static class SwitchItemBean {
            private String key;
            private boolean value;

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public boolean isValue() {
                return value;
            }

            public void setValue(boolean value) {
                this.value = value;
            }
        }

        private static class CheckBoxGroupBean {
            private String key;
            private List<String> value;

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public List<String> getValue() {
                return value;
            }

            public void setValue(List<String> value) {
                this.value = value;
            }
        }
    }

}
