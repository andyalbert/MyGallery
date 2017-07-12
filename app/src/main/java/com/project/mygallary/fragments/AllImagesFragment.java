package com.project.mygallary.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.project.mygallary.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author andrew
 * @since 7/7/2017
 * @version 1.0
 */

public class AllImagesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    @BindView(R.id.fragment_all_images_gridview)
    GridView gridView;
    private View view;
    private final int REQUEST_CODE = 1;
    private final boolean NONE_SELECTED = false;
    private final boolean SELECTED = true;
    //private boolean isSelected;
    private AllImagesCursorAdapter imagesCursorAdapter;
    private int currentRotation;
    private float imageWidth;
    private int columnCount;
    private ArrayList<Boolean> isSelected;
    private int selectedNumber;
    private final String TAG = "AI";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_all_images, container, false);
        ButterKnife.bind(this, view);
        findScreenDisplayMetrics();
        gridView.setNumColumns(columnCount);
        selectedNumber = 0;
    //    currentRotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();

        imagesCursorAdapter = new AllImagesCursorAdapter(getActivity(), null, 0); // zero in order not to handle changes, it will be done by the loader itself
        gridView.setAdapter(imagesCursorAdapter);
        gridView.setOnItemLongClickListener(this);


        //// TODO: 08/07/17 put this in the main activity
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            else
                getLoaderManager().initLoader(0, null, this);
        } else
            getLoaderManager().initLoader(0, null, this);
        return view;
    }

    private void findScreenDisplayMetrics() {
        columnCount = getResources().getInteger(R.integer.num_images);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        imageWidth = ((displayMetrics.widthPixels - (columnCount - 1) * gridView.getVerticalSpacing()) / columnCount);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media._ID
        };
        return new CursorLoader(getActivity(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        imagesCursorAdapter.swapCursor(data);
    //    isSelected = new HashMap<>(data.getCount());
        Log.d("AllImages", "onLoadFinished: " + data.getCount());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //View currentView = (View) parent.getItemAtPosition(position);
        imagesCursorAdapter.setSelected(position, view);
        gridView.setOnItemLongClickListener(null);
        gridView.setOnItemClickListener(this);
        selectedNumber++;
        Log.d(TAG, "onItemLongClick: ");
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick: ");
        if(!isSelected.get(position)) {
            selectedNumber++;
            imagesCursorAdapter.setSelected(position, view);
        }
        else {
            imagesCursorAdapter.setUnSelected(position, view);
            selectedNumber--;
            if(selectedNumber == 0){
                gridView.setOnItemClickListener(null);
                gridView.setOnItemLongClickListener(this);
            }
        }
    }

    private class AllImagesCursorAdapter extends CursorAdapter{
        private LayoutInflater inflater;

        AllImagesCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(R.layout.list_item_all_images, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView imageView = ButterKnife.findById(view, R.id.list_item_all_images_image);
            imageView.getLayoutParams().height = Math.round(imageWidth);
            imageView.getLayoutParams().width = Math.round(imageWidth);

            Glide.with(context)
                    .load(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)))
                    .into(imageView);

            if(isSelected.get(cursor.getPosition())){
                imageView.setAlpha(0.6f);
                imageView.setScaleX(0.8f);
                imageView.setScaleY(0.8f);
            } else
                updateViewUnSelected(view);

        }

        @Override
        public Cursor swapCursor(Cursor newCursor) {
            //// TODO: 12/07/17 causion : this is only handling adding new images, NOT DELETING
            if(isSelected != null){
                int diffSize = newCursor.getCount() - isSelected.size();
                ArrayList<Boolean> tmp = new ArrayList<>(diffSize);
                tmp.addAll(Collections.nCopies(diffSize, Boolean.FALSE));
                tmp.addAll(isSelected);
                isSelected = tmp;
            } else{
                isSelected = new ArrayList<>(newCursor.getCount());
                isSelected.addAll(Collections.nCopies(newCursor.getCount(), Boolean.FALSE));
            }
            return super.swapCursor(newCursor);
        }

        void setSelected(int position, View v){
            AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(v.getContext(), R.animator.image_shrinking);
            set.setTarget(ButterKnife.findById(v, R.id.list_item_all_images_image));
            set.start();
            isSelected.set(position, Boolean.TRUE);
            notifyDataSetChanged();
            Log.d(TAG, "setSelected: ");
        }

        void setUnSelected(int position, View view) {
            updateViewUnSelected(view);
            isSelected.set(position, Boolean.FALSE);
            notifyDataSetChanged();
            Log.d(TAG, "setUnSelected: ");
        }

        void updateViewUnSelected(View v){
            ImageView imageView = ButterKnife.findById(v, R.id.list_item_all_images_image);
            imageView.setAlpha(1f);
            imageView.setScaleX(1f);
            imageView.setScaleY(1f);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            getLoaderManager().initLoader(0, null, this);
    }
}
