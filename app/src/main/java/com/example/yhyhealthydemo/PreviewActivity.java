package com.example.yhyhealthydemo;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yhyhealthydemo.fragments.DocumentaryFragment;

import java.io.File;

public class PreviewActivity extends AppPage {
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        ImageView img = findViewById(R.id.preview);
        String path = getIntent().getStringExtra("path");
        if(path != null){
            img.setImageURI(Uri.fromFile(new File(path)));
        }

        DocumentaryFragment documentaryFragment = new DocumentaryFragment();

        Button save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    onBackPressed();
//                Bundle bundle = new Bundle();
//                bundle.putString("path", path);
//                DocumentaryFragment documentaryFragment = new DocumentaryFragment();
//                documentaryFragment.setArguments(bundle);
            }
        });
    }

}
