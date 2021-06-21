package com.example.yhyhealthy.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthy.LoginActivity;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.SystemAccountActivity;
import com.example.yhyhealthy.SystemProvisionActivity;
import com.example.yhyhealthy.SystemSettingActivity;
import com.example.yhyhealthy.SystemUserActivity;
import com.example.yhyhealthy.SystemVideoActivity;

import es.dmoral.toasty.Toasty;

import static android.content.Context.MODE_PRIVATE;

/**  *************
 * 設定首頁
 *  系統設定
 *  個人設定
 *  帳戶設定
 *  教學影片
 *  使用條款
 *  登出
 * **************/

public class SettingFragment extends Fragment implements View.OnClickListener {

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;

        view = inflater.inflate(R.layout.fragment_setting, container, false);

        ImageView setting = view.findViewById(R.id.imgSystemSetting);
        ImageView UserSetting = view.findViewById(R.id.imgSystemUserSetting);
        ImageView userAccount = view.findViewById(R.id.imgSystemAccount);
        ImageView video = view.findViewById(R.id.imgSystemVedio);
        ImageView provision = view.findViewById(R.id.imgSystemProvision);
        TextView  version = view.findViewById(R.id.tvVersion);
        TextView  logout = view.findViewById(R.id.tvLogout);
        version.setText(getString(R.string.version));

        setting.setOnClickListener(this);
        UserSetting.setOnClickListener(this);
        userAccount.setOnClickListener(this);
        video.setOnClickListener(this);
        provision.setOnClickListener(this);
        logout.setOnClickListener(this);

        return view;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgSystemSetting:       //系統設定
                startActivity(new Intent(getActivity(), SystemSettingActivity.class));
                break;
            case R.id.imgSystemUserSetting:  //個人設定
                startActivity(new Intent(getActivity(), SystemUserActivity.class));
                break;
            case R.id.imgSystemAccount:      //帳戶設定
                startActivity(new Intent(getActivity(), SystemAccountActivity.class));
                break;
            case R.id.imgSystemVedio:       //教學影片
                startActivity(new Intent(getActivity(), SystemVideoActivity.class));
                break;
            case R.id.imgSystemProvision:  //使用條款
                startActivity(new Intent(getActivity(), SystemProvisionActivity.class));
                break;
            case R.id.tvLogout:     //登出
                SharedPreferences pref = getActivity().getSharedPreferences("yhyHealthy", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear(); //清除帳號&密碼  2021/06/21增加
                editor.apply(); //執行  2021/06/21增加

                //回到登入頁面
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
                Toasty.success(getActivity(), getString(R.string.logout_success), Toast.LENGTH_SHORT, true).show();
                break;
        }
    }
}
