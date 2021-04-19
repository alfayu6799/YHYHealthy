package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yhyhealthydemo.fragments.DocumentaryFragment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/********************
* 排卵拍照上傳page
* 客製化照片系統
********************/

public class CameraActivity extends AppCompatActivity implements Camera.PictureCallback {

    private final static String TAG = CameraActivity.class.getSimpleName();

    private SurfaceView surfaceView;
    private Camera camera = null;
    private ImageView capture;
    Camera.Parameters mParameters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        surfaceView = findViewById(R.id.surfaceView);  //預覽畫面
        surfaceView.getHolder().addCallback(cpHolderCallback);
        capture = findViewById(R.id.takePhotoClick);   //拍照click
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
            }
        });
    }

    private void captureImage() {
        if ( camera!= null ){
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if(success){ //對焦成功後拍照
                        camera.cancelAutoFocus();
                        camera.takePicture(shutterCallback, null, CameraActivity.this);
                    }
                }
            });
        }
    }

    private SurfaceHolder.Callback cpHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            startPreview();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            stopPreview();
        }
    };

    private void stopPreview() {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private void startPreview() {
        camera = Camera.open();
        mParameters = camera.getParameters(); //得到相機的參數
        mParameters.setPictureFormat(PixelFormat.JPEG);
        mParameters.setPictureSize(1280, 960);
        camera.setParameters(mParameters);

        try {
            camera.setPreviewDisplay(surfaceView.getHolder());
            camera.setDisplayOrientation(90); //鏡頭需旋轉90度
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //創建jpeg圖片回調數據對象
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        String path = "";
        if ((path = saveImage(data)) != null) {
            Intent it = new Intent(CameraActivity.this, PeriodRecordActivity.class);
            it.putExtra("path", path);
            setResult(2, it);
            finish();
        }
    }

    //保存臨時文件的方法
    private String saveFile(byte[] bytes){
        try {
            File file = File.createTempFile("img","");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    //保存臨時文件的方法
    private String saveImage(byte[] bytes){
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Matrix matrix = new Matrix();
        matrix.setRotate(90); //圖片轉90度
        Bitmap mBitmap = Bitmap.createBitmap(bitmap,0 ,0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        try {
            File file = File.createTempFile("img",".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            fos.write(bytes);
            fos.flush();
            fos.close();
            bitmap.recycle();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    //拍照時會出現"喀擦"聲
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback(){
        @Override
        public void onShutter() {
        }
    };

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
