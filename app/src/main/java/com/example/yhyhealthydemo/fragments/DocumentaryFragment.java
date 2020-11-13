package com.example.yhyhealthydemo.fragments;

/*****************************
* 排卵紀錄Page
* 照相Fxn
* ***************************/

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthydemo.CameraActivity;
import com.example.yhyhealthydemo.PreviewActivity;
import com.example.yhyhealthydemo.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION_CODES.M;

public class DocumentaryFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = DocumentaryFragment.class.getSimpleName();

    private View view;

    private TextView recordDay;

    private ImageView photo;
    private Button takePhoto;

    public static final int REQUEST_CODE = 100;
    private String[] neededPermissions = new String[]{CAMERA, WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};
    boolean result;

    private ImageView bleSearch;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;

        Bundle bundle = this.getArguments();
        if (bundle != null){
            String path = getArguments().getString("path");
            Log.d(TAG, "onCreateView: " + path);
//            if(path != null){
//                photo.setImageURI(Uri.fromFile(new File(path)));
//            }
        }

        view = inflater.inflate(R.layout.fragment_documentary, container, false);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String today = sdf.format(new Date());
        recordDay = view.findViewById(R.id.textRecordDate);
        recordDay.setText(today);

        photo = view.findViewById(R.id.imgPhoto);
        takePhoto = view.findViewById(R.id.btnPhoto);
        bleSearch = view.findViewById(R.id.imgBLESearch);
        takePhoto.setOnClickListener(this);
        bleSearch.setOnClickListener(this);

        result = checkPermission(); //要求Camera&SDCard權限

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnPhoto:
                result = checkPermission(); //要求權限
                if (result) {
                    Intent camera = new Intent(getActivity(), CameraActivity.class); //有bug
                    startActivity(camera);
                }
                break;
            case R.id.imgBLESearch:
                result = checkPermission(); //要求權限(BLE要求local位置權限)
                if(result){
                    initBle();
                }

                break;
        }
    }

    private void initBle() {
        //init ble adapter
        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled()){
            //直接開啟藍芽
            bluetoothAdapter.enable();
        }
        //dialog
        
    }

    //permission
    private boolean checkPermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= M ){
            ArrayList<String> permissionsNotGranted = new ArrayList<>();
            for (String permission : neededPermissions) {
                if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNotGranted.add(permission);
                }
            }
            if (permissionsNotGranted.size() > 0) {
                boolean shouldShowAlert = false;
                for (String permission : permissionsNotGranted) {
                    shouldShowAlert = ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission);
                }
                if (shouldShowAlert) {
                    showPermissionAlert(permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]));
                } else {
                    requestPermissions(permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]));
                }
                return false;
            }
        }
        return true;
    }

    private void requestPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_CODE);
    }

    private void showPermissionAlert(final String[] permissions) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(R.string.permission_required);
        alertBuilder.setMessage(R.string.permission_message);
        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(permissions);
            }
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    /**
     * 取得權限判斷
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CODE:
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(getActivity(), R.string.permission_warning, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                break;
        }
    }

}
