package com.example.socialnetwork;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {

    private ImageView imageView;
    private String ImageUrl, ImageName;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageView = findViewById(R.id.Image_viewer);
        ImageUrl = getIntent().getStringExtra("url");
        ImageName = getIntent().getStringExtra("name");

        Picasso.with(ImageViewerActivity.this).load(ImageUrl).into(imageView);

        mToolbar = findViewById(R.id.my_image_viewer);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(ImageName);
    }
}
