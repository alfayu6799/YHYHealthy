package com.example.yhyhealthydemo;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthydemo.tools.ProgressDialogUtil;

import es.dmoral.toasty.Toasty;

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
        ImageView observation = view.findViewById(R.id.imgSystemObser);
        TextView  version = view.findViewById(R.id.tvVersion);
        TextView  logout = view.findViewById(R.id.tvLogout);

        setting.setOnClickListener(this);
        UserSetting.setOnClickListener(this);
        userAccount.setOnClickListener(this);
        observation.setOnClickListener(this);
        video.setOnClickListener(this);
        provision.setOnClickListener(this);
        logout.setOnClickListener(this);

        return view;
    }

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
            case R.id.imgSystemObser:       //觀測者編輯
                startActivity(new Intent(getActivity(), SysObservationActivity.class));
                break;
            case R.id.imgSystemVedio:       //教學影片
                startActivity(new Intent(getActivity(), SystemVideoActivity.class));
                break;
            case R.id.imgSystemProvision:  //使用條款
                startActivity(new Intent(getActivity(), SystemProvisionActivity.class));
                break;
            case R.id.tvLogout:     //登出
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
                Toasty.success(getActivity(), getString(R.string.logout_success), Toast.LENGTH_SHORT, true).show();
                break;
        }
    }
}
