package com.example.yhyhealthy.datebase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**  **************
 * 症狀專用DataBean
 * 因為value值有boolean跟Array所以要自己設計
 * create: 2021/04/13
 * *************       *****/

public class SymptomData {

    private int errorCode;

    private List<SwitchItemBean> switchItemList;

    private List<CheckBoxGroup> checkBoxGroupList;

    public static class SwitchItemBean {
        private String key;
        private boolean value;

        public String getKey() {
            return key;
        }

        public boolean isValue() {
            return value;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setValue(boolean value) {
            this.value = value;
        }

        public SwitchItemBean(String key, boolean value){
            this.key = key;
            this.value = value;
        }

    }

    public static class CheckBoxGroup {
        @Expose(serialize = true)
        @SerializedName("key")
        private String key;

        @Expose(serialize = false, deserialize = false)
        private List<String> value;

        @Expose(serialize = true)
        @SerializedName("value")
        private Set<String> checked = new HashSet<>();

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public List<String> getChecked() {
            return Arrays.asList(checked.toArray(new String[checked.size()]));
        }

        public void setChecked(String checked) {
            if (this.checked.contains(checked)){
                this.checked.remove(checked);
            }else {
                this.checked.add(checked);
            }
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
