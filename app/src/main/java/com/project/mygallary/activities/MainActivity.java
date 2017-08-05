package com.project.mygallary.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.project.mygallary.R;
import com.project.mygallary.fragments.AlbumsFragments;
import com.project.mygallary.fragments.AllImagesFragment;
import com.project.mygallary.fragments.DateImagesFragment;
import com.project.mygallary.fragments.FavouriteImages;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity{
    @BindView(R.id.content_main_tablayout)
    TabLayout tabLayout;
    @BindView(R.id.content_main_viewpager)
    ViewPager viewPager;

    private final String TAG = MainActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);




        MainFragmentsPagerAdapter fragmentPagerAdapter = new MainFragmentsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fragmentPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setText("Folders");
        tabLayout.getTabAt(1).setText("Albums");
        tabLayout.getTabAt(2).setText("All");
        tabLayout.getTabAt(3).setText("text4");


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//    ButterKnife.findById(this, R.id.content_main_viewpager).setOnTouchListener(new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//            return true;
//        }
//    });
   //     ButterKnife.findById(this, R.id.content_main_viewpager).setOnTouchListener(null);


    }



    private class MainFragmentsPagerAdapter extends FragmentPagerAdapter{
        private final int FRAGMENT_NUMBER = 4;

        MainFragmentsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new DateImagesFragment();
                case 1:
                    return new AlbumsFragments();
                case 2:
                    return new AllImagesFragment();
                default:
                    return new FavouriteImages();
            }
        }

        @Override
        public int getCount() {
            return FRAGMENT_NUMBER;
        }


    }
}
