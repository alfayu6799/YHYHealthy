package com.example.yhyhealthydemo.datebase;

import com.google.gson.Gson;

public class ArticleData {

    /**
     * attrId : 01
     * attrName : 生理保養
     * serviceItemId : 01
     * iconImg : article_2.png
     */

    private String attrId;
    private String attrName;      //圖片名稱
    private String serviceItemId;
    private String iconImg;       //圖片png檔

    public String getAttrId() {
        return attrId;
    }

    public void setAttrId(String attrId) {
        this.attrId = attrId;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getServiceItemId() {
        return serviceItemId;
    }

    public void setServiceItemId(String serviceItemId) {
        this.serviceItemId = serviceItemId;
    }

    public String getIconImg() {
        return iconImg;
    }

    public void setIconImg(String iconImg) {
        this.iconImg = iconImg;
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
