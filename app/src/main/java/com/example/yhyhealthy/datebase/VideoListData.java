package com.example.yhyhealthy.datebase;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.List;

public class VideoListData {

    private List<VideoListBean> videoList;

    public List<VideoListBean> getVideoList() {
        return videoList;
    }

    public static class VideoListBean {
        /**
         * video_id : 23
         * video_title : 痛經時該怎麼辦?
         * video_file : -lP8dNM-S5k
         * video_img : http://192.168.1.120:8080/health_education/cover/23-zh-TW.jpg
         * video_time : 3:06
         */

        private String video_id;
        private String video_title;
        private String video_file;
        private String video_img;
        private String video_time;

        public String getVideo_id() {
            return video_id;
        }

        public String getVideo_title() {
            return video_title;
        }

        public String getVideo_file() {
            return video_file;
        }

        public String getVideo_img() {
            return video_img;
        }

        public String getVideo_time() {
            return video_time;
        }
    }

    /**
     * JSON 字串轉 物件
     *
     * @param jsonString json 格式的資料
     * @return 物件
     */
    public static VideoListData newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new VideoListData();
        }

        Gson gson = new Gson();
        VideoListData item;

        try {
            item = gson.fromJson(jsonString, VideoListData.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new VideoListData();
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
