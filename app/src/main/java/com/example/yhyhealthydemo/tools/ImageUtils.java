package com.example.yhyhealthydemo.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/****
 * 圖片相關的工具類
 * */

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * 將圖片轉換成Base64編碼的字串
     *
     * @param path 圖片本地路徑
     * @return base64編碼的字串
     */
    public static String imageToBase64(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        InputStream is = null;
        byte[] data;
        String result = null;
        try {
            is = new FileInputStream(path);
            //建立一個字元流大小的陣列。
            data = new byte[is.available()];
            //寫入陣列
            is.read(data);
            //用預設的編碼格式進行編碼
            result = Base64.encodeToString(data, Base64.NO_WRAP); //略去所有的換行符(好大的坑!!)
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 在ImageView裡展示指定路徑的圖片
     *
     * @param path      本地路徑
     * @param imageView ImageView
     */
    public static void ShowPic2View(Context context, String path, ImageView imageView) {
        File localFile;
        FileInputStream localStream;
        Bitmap bitmap;
        localFile = new File(path);
        if (!localFile.exists()) {
            Log.d(TAG, "ShowPic2View: photoPath is empty");
        } else {
            try {
                localStream = new FileInputStream(localFile);
                bitmap = BitmapFactory.decodeStream(localStream);
                imageView.setImageBitmap(bitmap);
                //                if (localStream != null) {
                localStream.close();
                //                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
