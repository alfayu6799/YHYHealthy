package com.example.yhyhealthy.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.yhyhealthy.CatalogActivity;
import com.example.yhyhealthy.ForumActivity;
import com.example.yhyhealthy.OnLineCallActivity;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.VideoActivity;

import pl.droidsonroids.gif.GifImageView;

/***
 * 衛教頁面
 * 文章,影片,討論區,線上諮詢
 */

public class EducationFragment extends Fragment implements View.OnClickListener {

   private View view;

   private Button btnArticle, btnVideo, btnForum, btnOnline;

    private GifImageView gifImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;
        view = inflater.inflate(R.layout.fragment_education, container, false);

        btnArticle = view.findViewById(R.id.article);
        btnVideo = view.findViewById(R.id.video);
        btnForum = view.findViewById(R.id.discuss);
        btnForum.setVisibility(View.GONE);  //2021/05/25 hide
        btnOnline = view.findViewById(R.id.onlineCall);
        btnOnline.setVisibility(View.GONE); //2021/05/25 hide

        //動畫background
        gifImageView = view.findViewById(R.id.game_gif);
        gifImageView.setBackgroundResource(R.mipmap.yhy_new_background);

        btnArticle.setOnClickListener(this);
        btnVideo.setOnClickListener(this);
        btnForum.setOnClickListener(this);
        btnOnline.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        Class<?> target = null;

        switch (view.getId()){
            case R.id.article:
                target = CatalogActivity.class; //文章
                break;
            case R.id.video:
                target = VideoActivity.class; //影片
                break;
            case R.id.discuss:
                target = ForumActivity.class; //討論區
                break;
            case R.id.onlineCall:
                target = OnLineCallActivity.class; //線上諮詢
                break;
        }
        if (target != null) startActivity(new Intent(getContext(), target));
    }
}
