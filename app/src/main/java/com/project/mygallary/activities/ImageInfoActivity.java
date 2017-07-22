package com.project.mygallary.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.project.mygallary.R;
import com.project.mygallary.fragments.ImageInfoFragment;

import butterknife.ButterKnife;

/**
 * @author andrew
 * @version 1.0
 * @since 17/7/2017
 */

public class ImageInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_info);
        ImageInfoFragment imageInfoFragment = new ImageInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString("id", getIntent().getStringExtra("id"));
        imageInfoFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.activity_image_info_framelayout, imageInfoFragment).commit();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
