package com.example.yhyhealthydemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/****
 * 首頁
 * 四個功能 : 排卵紀錄, 藍芽體溫 , 懷孕紀錄 , 呼吸監控
 * 二個icon : 購物,教學
 * */
public class MeasureFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MeasureFragment";

    private View view;

    private ImageView ovulationOnClick, temperatureOnClick, pregnancyOnClick, monitorOnClick;

    private ImageView guidOnclick;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;

        view = inflater.inflate(R.layout.fragment_measure, container, false);

        ovulationOnClick = view.findViewById(R.id.ivOvulation);
        temperatureOnClick = view.findViewById(R.id.ivTemperature);
        pregnancyOnClick = view.findViewById(R.id.ivPregnancy);
        monitorOnClick = view.findViewById(R.id.ivBreath);

        guidOnclick = view.findViewById(R.id.ivGuid);

        ovulationOnClick.setOnClickListener(this);
        temperatureOnClick.setOnClickListener(this);
        pregnancyOnClick.setOnClickListener(this);
        monitorOnClick.setOnClickListener(this);

        guidOnclick.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.ivOvulation:
                checkOvulationInfo(); //檢查設定
                break;

            case R.id.ivTemperature:
                Intent intent_t = new Intent(getActivity(), TemperatureActivity.class);
                startActivity(intent_t);
                break;

            case R.id.ivPregnancy:
                Intent intent_p = new Intent(getActivity(), PregnancyActivity.class);
                startActivity(intent_p);
                break;

            case R.id.ivBreath: //呼吸監控

                break;

            case R.id.ivGuid:  //教學影片
                startActivity(new Intent(getActivity(), TeachVideoActivity.class));
                break;
        }
    }

    //檢查婚姻狀況與經期設定是否有設定完成
    private void checkOvulationInfo() {
        //取得相關資訊(local file)
        //婚姻狀況
        boolean marriageStatus = this.getActivity().getSharedPreferences("yhyHealthy", Context.MODE_PRIVATE).getBoolean("MARRIAGE", false);
        //經期設定
        boolean menstrualStatus = this.getActivity().getSharedPreferences("yhyHealthy", Context.MODE_PRIVATE).getBoolean("MENSTRUAL", false);
        //個人資料設定
        boolean userInfoStatus = this.getActivity().getSharedPreferences("yhyHealthy", Context.MODE_PRIVATE).getBoolean("USERSET", false);

        //判斷進入排卵功能必需的元素是否齊全
        if (!marriageStatus) {  //婚姻狀態不齊全
            startActivity(new Intent(getActivity(), UserMarriageActivity.class));
        } else if (!menstrualStatus) { //經期設定不齊全
            startActivity(new Intent(getActivity(), UserPeriodActivity.class));
        }else if (!userInfoStatus){  //個人資料設定不齊全
            startActivity(new Intent(getActivity(), UserBasicActivity.class));
        }else {
            startActivity(new Intent(getActivity(), OvulationActivity.class));
        }

    }

}
