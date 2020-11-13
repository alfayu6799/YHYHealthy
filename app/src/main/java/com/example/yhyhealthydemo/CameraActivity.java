package com.example.yhyhealthydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity implements Camera.PictureCallback {

    private final static String TAG = CameraActivity.class.getSimpleName();

    private SurfaceView surfaceView;
    private Camera camera = null;
    private ImageView capture;


    private File currentImageFile = null;

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
                    if(success){ //對焦成功拍照
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
        if ((path = saveFile(data)) != null){
            Log.d(TAG, "onPictureTaken: " + path);
        }
//        if ((path = saveFile(data)) != null) {
//            Bundle bundle = new Bundle();
//            bundle.putString("path", path);
//            DocumentaryFragment documentaryFragment = new DocumentaryFragment();
//            documentaryFragment.setArguments(bundle);
//            Log.d(TAG, "onPictureTaken: " + path);
//            saveImage(data);  //照片存檔
            onBackPressed();
//            Intent it = new Intent(CameraActivity.this, PreviewActivity.class);
//            it.putExtra("path", path);
//            startActivity(it);
//        }
//        saveImage(data);  //照片存檔
//        onBackPressed();
    }

    //保存临时文件的方法
    private String saveFile(byte[] bytes){
        try {
//            File file = File.createTempFile("img","");
            String strFileName;
            FileOutputStream fos;
            File file = new File("/sdcard/demo/");
            if (!file.exists())
            {
                file.mkdir();
            }
            String tmp = "/sdcard/demo/picture.jpg";
            fos = new FileOutputStream(tmp);
            fos.write(bytes);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    //照片存檔
    private void saveImage(byte[] bytes) {
        //存檔前對照片做處理
//        Bitmap bm = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
//        Matrix matrix = new Matrix();
//        matrix.setRotate(90);
//        int height = bm.getHeight();
//        int width = bm.getWidth();
//        Bitmap bitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true); //圖片重建
        FileOutputStream fout;
        File dir = new File("/sdcard/demo/");
        if (!dir.exists()){
            dir.mkdir();
        }

        String tmp = "/sdcard/demo/picture.jpg";
        try {
            fout = new FileOutputStream(tmp);
            fout.write(bytes);
            fout.flush();
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
