package com.example.yhyhealthydemo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yhyhealthydemo.R;

public class DocumentaryFragment extends Fragment implements View.OnClickListener {

    private View view;

    private ImageView photo;
    private Button takePhoto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;
        view = inflater.inflate(R.layout.fragment_documentary, container, false);

        photo = view.findViewById(R.id.ig_photo);
        takePhoto = view.findViewById(R.id.bt_take_photo);
        takePhoto.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_take_photo:
                Toast.makeText(getActivity(),"照相", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
