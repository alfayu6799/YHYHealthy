package com.example.yhyhealthy.datebase;

import android.text.TextUtils;

import com.google.gson.Gson;

public class PhotoData {

    /**
     * success : {"param":"","paramName":"Unrecognizable"}
     * errorCode : 0
     */

    private SuccessBean success;
    private int errorCode;

    public SuccessBean getSuccess() {
        return success;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public static class SuccessBean {
        /**
         * param :
         * paramName : Unrecognizable
         */

        private String param;
        private String paramName;

        public String getParam() {
            return param;
        }

        public String getParamName() {
            return paramName;
        }
    }

    /**
     * JSON 字串轉 物件
     *
     * @param jsonString json 格式的資料
     * @return PhotoData 物件
     */
    public static PhotoData newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new PhotoData();
        }

        Gson gson = new Gson();
        PhotoData item;

        try {
            item = gson.fromJson(jsonString, PhotoData.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new PhotoData();
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
