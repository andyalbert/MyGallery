package com.project.mygallary.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;

import com.project.mygallary.R;

/**
 * @author andrew
 * @version 1.0
 * @since 6/8/2017
 *
 * a splash activity that also checks if the read external storage permission is granted or not
 * @see #onRequestPermissionsResult(int, String[], int[]) to view if permission granted or not
 */

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                    else
                        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                } else
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            }
        }, 1500);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
            finishAffinity();
        else
            startActivity(new Intent(this, MainActivity.class));
    }
}
