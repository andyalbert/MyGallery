package com.project.mygallary.activities;

import android.Manifest;
import android.app.LoaderManager;
import android.app.WallpaperManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.project.mygallary.R;
import com.project.mygallary.fragments.ImageFragment;
import com.project.mygallary.fragments.ImageInfoFragment;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author andrew
 * @version 1.1
 * @since 14/7/2017
 */

public class ImageViewerActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    @BindView(R.id.activity_image_viewer_viewpager)
    ViewPager viewPager;
    private ImagesSwapperAdapter imagesSwapperAdapter;
    private int cursorPosition;
    private Cursor cursor;
    private int currentImageDateTaken = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        ButterKnife.bind(this);
        cursorPosition = getIntent().getIntExtra("position", 0);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_back));

        this.getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DESCRIPTION
        };
        return new CursorLoader(this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() == 0)
            finish();
        this.cursor = cursor;
        imagesSwapperAdapter = new ImagesSwapperAdapter(getSupportFragmentManager());
        viewPager.setAdapter(imagesSwapperAdapter);
        if (cursorPosition != -1) {
            viewPager.setCurrentItem(cursorPosition);
            cursorPosition = -1;
        } else {
            int itemsCount = cursor.getCount();
            for (int i = 0; i < itemsCount; i++) {
                cursor.moveToPosition(i);
                int tempDateTaken = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
                //// TODO: 17/07/17 bug :(
                if (tempDateTaken == currentImageDateTaken || tempDateTaken < currentImageDateTaken) {
                    viewPager.setCurrentItem(i);
                    break;
                } else if(i + 1 == itemsCount)
                    viewPager.setCurrentItem(i);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }


    private class ImagesSwapperAdapter extends FragmentStatePagerAdapter {
        ImagesSwapperAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            cursor.moveToPosition(position);
            ImageFragment imageFragment = new ImageFragment();
            Bundle bundle = new Bundle();
            bundle.putString("imageUri", cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
            String desc = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION));
            int date = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
            Log.d("shit", "getItem: " + position + " " + desc + " " + date);
            imageFragment.setArguments(bundle);
            return imageFragment;
        }

        @Override
        public int getCount() {
            return cursor.getCount();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_image, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.item_share:
                cursor.moveToPosition(viewPager.getCurrentItem());
                Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                Intent chooser = Intent.createChooser(intent, "share through");
                startActivity(chooser);
                return true;
            case R.id.item_info:
                cursor.moveToPosition(viewPager.getCurrentItem());
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                intent = new Intent(this, ImageInfoActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
                return true;
            case R.id.item_delete:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                        deleteCurrent();
                    else
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                } else
                    deleteCurrent();
                return true;
            case R.id.item_set_as_wallpaper:
                //// TODO: 22/07/17 finish
            default: return false;
        }
    }

    private void deleteCurrent() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_warning)
                .setCancelable(true)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setTitle("Caution")
                .setMessage("photos will be permanently deleted, continue ?")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        cursor.moveToPosition(viewPager.getCurrentItem());
                        currentImageDateTaken = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
                        String idToDelete = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                        getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "_id = " + idToDelete, null);
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            deleteCurrent();
    }

    @Override
    protected void onPause() {
        if(cursor.getCount() == 0){ //used to handle finishing the activity when all images are deleted
            super.onPause();
            return;
        }
        cursor.moveToPosition(viewPager.getCurrentItem());
        currentImageDateTaken = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
        super.onPause();
    }
}
