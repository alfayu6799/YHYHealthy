package com.example.yhyhealthydemo.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
                localStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 質量壓縮方法
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 質量壓縮方法，這裡100表示不壓縮，把壓縮後的資料存放到baos中
        int options = 90;
        while (baos.toByteArray().length / 1024 > 100) { // 迴圈判斷如果壓縮後圖片是否大於100kb,大於繼續壓縮
            baos.reset(); // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 這裡壓縮options%，把壓縮後的資料存放到baos中
            options -= 10;// 每次都減少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把壓縮後的資料baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream資料生成圖片
        return bitmap;
    }



    /**
     * 圖片按比例大小壓縮方法
     * @param srcPath （根據路徑獲取圖片並壓縮）
     * @return
     */
    public static Bitmap getimage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 開始讀入圖片，此時把options.inJustDecodeBounds 設回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此時返回bm為空
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 現在主流手機比較多是800*480解析度，所以高和寬我們設定為
        float hh = 800f;// 這裡設定高度為800f
        float ww = 480f;// 這裡設定寬度為480f
        // 縮放比。由於是固定比例縮放，只用高或者寬其中一個資料進行計算即可
        int be = 1;// be=1表示不縮放
        if (w > h && w > ww) {// 如果寬度大的話根據寬度固定大小縮放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的話根據寬度固定大小縮放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 設定縮放比例
        // 重新讀入圖片，注意此時已經把options.inJustDecodeBounds 設回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);// 壓縮好比例大小後再進行質量壓縮
    }

    /**
     * 圖片按比例大小壓縮方法
     * @param image （根據Bitmap圖片壓縮）
     * @return
     */
    public static Bitmap compressScale(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        // 判斷如果圖片大於1M,進行壓縮避免在生成圖片（BitmapFactory.decodeStream）時溢位
        if (baos.toByteArray().length / 1024 > 1024) {
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 80, baos);// 這裡壓縮50%，把壓縮後的資料存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 開始讀入圖片，此時把options.inJustDecodeBounds 設回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;

        // 現在主流手機比較多是800*480解析度，所以高和寬我們設定為
        // float hh = 800f;// 這裡設定高度為800f
        // float ww = 480f;// 這裡設定寬度為480f
        float hh = 512f;
        float ww = 512f;
        // 縮放比。由於是固定比例縮放，只用高或者寬其中一個資料進行計算即可
        int be = 1;// be=1表示不縮放
        if (w > h && w > ww) {// 如果寬度大的話根據寬度固定大小縮放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) { // 如果高度高的話根據高度固定大小縮放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be; // 設定縮放比例
        // newOpts.inPreferredConfig = Config.RGB_565;//降低圖片從ARGB888到RGB565
        // 重新讀入圖片，注意此時已經把options.inJustDecodeBounds 設回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);// 壓縮好比例大小後再進行質量壓縮
        //return bitmap;
    }

    public static Bitmap bast64toBitmap(String base64){
        byte[] imageByteArray = Base64.decode(base64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageByteArray,0, imageByteArray.length);
    }
}
