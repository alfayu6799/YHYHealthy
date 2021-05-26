package com.example.yhyhealthy.datebase;

import android.text.TextUtils;
import com.google.gson.Gson;
import java.util.List;

public class VideoData {

    private List<ServiceItemListBean> serviceItemList;

    public List<ServiceItemListBean> getServiceItemList() {
        return serviceItemList;
    }

    public static class ServiceItemListBean {
        /**
         * id : 01
         * name : 護理
         * iconImg : article_1.png
         * attrlist : [{"attrId":"01","attrName":"生理保養","serviceItemId":"01","iconImg":"article_2.png"},{"attrId":"02","attrName":"婦科疾病","serviceItemId":"01","iconImg":"article_3.png"},{"attrId":"03","attrName":"備孕與避孕","serviceItemId":"01","iconImg":"article_4.png"},{"attrId":"04","attrName":"孕期與產後","serviceItemId":"01","iconImg":"article_5.png"},{"attrId":"05","attrName":"親密男女","serviceItemId":"01","iconImg":"article_6.png"},{"attrId":"06","attrName":"預防醫學","serviceItemId":"01","iconImg":"article_7.png"}]
         */

        private String id;
        private String name;
        private String iconImg;
        private List<AttrlistBean> attrlist;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getIconImg() {
            return iconImg;
        }

        public List<AttrlistBean> getAttrlist() {
            return attrlist;
        }

        public static class AttrlistBean {
            /**
             * attrId : 01
             * attrName : 生理保養
             * serviceItemId : 01
             * iconImg : article_2.png
             */

            private String attrId;
            private String attrName;
            private String serviceItemId;
            private String iconImg;

            public String getAttrId() {
                return attrId;
            }

            public String getAttrName() {
                return attrName;
            }

            public String getServiceItemId() {
                return serviceItemId;
            }

            public String getIconImg() {
                return iconImg;
            }
        }
    }

    /**
     * JSON 字串轉  物件
     *
     * @param jsonString json 格式的資料
     * @return TemperatureReceives 物件
     */
    public static VideoData newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new VideoData();
        }

        Gson gson = new Gson();
        VideoData item;

        try {
            item = gson.fromJson(jsonString, VideoData.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new VideoData();
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
