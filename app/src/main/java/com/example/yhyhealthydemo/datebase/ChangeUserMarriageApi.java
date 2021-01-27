package com.example.yhyhealthydemo.datebase;

import android.text.TextUtils;

import com.google.gson.Gson;

/**  *** **********
 * 婚姻設定更新Api
 * * * * ****  ****/

public class ChangeUserMarriageApi {

    /**
     * married : true
     * contraception : false
     * hasChild : false
     */

    private boolean married;
    private boolean contraception;
    private boolean hasChild;

    public boolean isMarried() {
        return married;
    }

    public void setMarried(boolean married) {
        this.married = married;
    }

    public boolean isContraception() {
        return contraception;
    }

    public void setContraception(boolean contraception) {
        this.contraception = contraception;
    }

    public boolean isHasChild() {
        return hasChild;
    }

    public void setHasChild(boolean hasChild) {
        this.hasChild = hasChild;
    }

    /**
     * JSON 字串轉 物件
     *
     * @param jsonString json 格式的資料
     * @return TemperatureReceives 物件
     */
    public static ChangeUserMarriageApi newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new ChangeUserMarriageApi();
        }

        Gson gson = new Gson();
        ChangeUserMarriageApi item;

        try {
            item = gson.fromJson(jsonString, ChangeUserMarriageApi.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new ChangeUserMarriageApi();
        }

        return item;
    }

    /**
     * SignInAPI 物件轉 JSON字串
     *
     * @return json 格式的資料
     */
    public String toJSONString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
