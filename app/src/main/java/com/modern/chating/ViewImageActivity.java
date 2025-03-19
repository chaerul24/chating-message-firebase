package com.modern.chating;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import wtc.material.image.ImageWtcView;

public class ViewImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_image);
        String imageUrl = getIntent().getStringExtra("image_url");
        ImageView imageWtcView = findViewById(R.id.imageWtcView);
        Glide.with(this)
                .load(imageUrl)
                .into(imageWtcView);

    }
}
