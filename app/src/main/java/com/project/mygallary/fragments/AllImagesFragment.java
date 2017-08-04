package com.project.mygallary.fragments;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.project.mygallary.R;
import com.project.mygallary.activities.ImageViewerActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author andrew
 * @since 7/7/2017
 * @version 1.8
 */

public class AllImagesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    @BindView(R.id.fragment_all_images_gridview)
    GridView gridView;
    @BindView(R.id.fragment_all_images_progressbar)
    ProgressBar progressBar;
    private Unbinder unbinder;
    private Cursor cursor = null;
    private View view;
    private AllImagesCursorAdapter imagesCursorAdapter;
    /**
     * the image width that will be calculated for this device
     * @see #findScreenDisplayMetrics()
     */
    private float imageWidth;
    /**
     * the number of columns to be set for the grid view
     * also calculated
     * @see #findScreenDisplayMetrics()
     */
    private int columnCount;
    /**
     * array list that identify the selected images, as boolean array list
     */
    private ArrayList<Boolean> isSelected;
    private int selectedNumber = 0;
    private final String TAG = "AI";
    /**
     * used to identify (when orientation change), that the image list was loaded before
     * {@link AllImagesCursorAdapter#swapCursor(Cursor)}
     */
    private Boolean isSelectedArraySaved = false;
    /**
     * to detect if the image position in the grid view was just selected or selected before
     * to be used to distinguish the usage of the animator or just setting scaleX and scaleY
     * @see AllImagesCursorAdapter#bindView(View, Context, Cursor)
     * @see AllImagesFragment#onItemClick(AdapterView, View, int, long)
     * @see AllImagesFragment#onItemLongClick(AdapterView, View, int, long)
     */
    private int justSelected = -1;
    /**
     * to detect if the image position in the grid view was just un-selected or un-selected before
     * to be used to distinguish the usage of the animator or just setting scaleX and scaleY
     * @see AllImagesFragment#onItemLongClick(AdapterView, View, int, long)
     * @see AllImagesCursorAdapter#bindView(View, Context, Cursor)
     */
    private int justUnselected = -1;
        //// TODO: 13/07/17 bug: first item animation :(
    //// TODO: 14/07/17 bug: animating the action bar
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_all_images, container, false);
        unbinder = ButterKnife.bind(this, view);

        findScreenDisplayMetrics();

        if(savedInstanceState != null){
            selectedNumber = savedInstanceState.getInt("selectionNum");
            isSelected = (ArrayList<Boolean>) savedInstanceState.getSerializable("selectionArray");
            isSelectedArraySaved = true;
        }
        imagesCursorAdapter = new AllImagesCursorAdapter(getActivity(), null, 0); // zero in order not to handle changes, it will be done by the loader itself

        if(selectedNumber == 0)
            gridView.setOnItemLongClickListener(this);
        gridView.setNumColumns(columnCount);
        gridView.setAdapter(imagesCursorAdapter);

        getLoaderManager().initLoader(0, null, this);
        gridView.setOnItemClickListener(this);
        return view;
    }

    /**
     * used to calculate the {@link #imageWidth} and the {@link #columnCount}  according to screen size and orientation
     */
    private void findScreenDisplayMetrics() {
        columnCount = getResources().getInteger(R.integer.num_images);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        imageWidth = ((displayMetrics.widthPixels - (columnCount - 1) * gridView.getVerticalSpacing()) / columnCount);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: ");
        String[] projection = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };
        return new CursorLoader(getActivity(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        imagesCursorAdapter.swapCursor(data);
        cursor = data;
        Log.d("AllImages", "onLoadFinished: " + data.getCount());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset: ");
    }

    /**
     * this will handle the long click event, the {@link com.project.mygallary.activities.MainActivity#tabLayout} will be disabled,
     * also the swipe capability for the {@link com.project.mygallary.activities.MainActivity#viewPager} will be disabled
     * all the images view will change to show the new ability to select images as needed
     *
     * this touch capability will be disabled after the first time used, adn the {@link #onItemClick(AdapterView, View, int, long)} will be enabled instead
     *
     * {@inheritDoc}
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        isSelected.set(position, Boolean.TRUE);
        justSelected = position;
        selectedNumber++;
        imagesCursorAdapter.notifyDataSetChanged();

        gridView.setOnItemLongClickListener(null);
        Log.d(TAG, "onItemLongClick: ");

        initializeToolbarOptions();

        return true;
    }

    private void initializeToolbarOptions() {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeAsUpIndicator(ContextCompat.getDrawable(getActivity(), R.drawable.ic_close));
            actionBar.setDisplayHomeAsUpEnabled(true);
            setHasOptionsMenu(true);
        }
    }

    /**
     * it's used to handle both multi-selecting items, or viewing a specific image
     *
     * for the multi-selection capability:
     * it's used to handle the clicking of any image after the {@link #onItemLongClick(AdapterView, View, int, long)} is pressed, it either select or de-select items changing the values of
     * @see #selectedNumber and
     * @see #isSelected
     *
     * if no items are selected anymore, it will be disabled and the {@link #onItemLongClick(AdapterView, View, int, long)} will be enabled
     *
     * {@inheritDoc}
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(gridView.getOnItemLongClickListener() != null){
            Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
            cursor.moveToPosition(position);
            intent.putExtra("position", position);
            startActivity(intent);
            Log.d(TAG, "onItemClick: test");
            return;
        }
        if(!isSelected.get(position)) {
            selectedNumber++;
            justSelected = position;
            isSelected.set(position, Boolean.TRUE);
            imagesCursorAdapter.notifyDataSetChanged();
        }
        else {
            selectedNumber--;
            isSelected.set(position, Boolean.FALSE);
            justUnselected = position;
            imagesCursorAdapter.notifyDataSetChanged();
            if(selectedNumber == 0){
                gridView.setOnItemLongClickListener(this);
                setHasOptionsMenu(false);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
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

            View selectionBackground = ButterKnife.findById(view, R.id.list_item_all_images_selection_background);
            ImageView selectionIdentifier = ButterKnife.findById(view, R.id.list_item_all_images_selected);
            if(justSelected == cursor.getPosition()){
                showHiddenViews(selectionBackground, selectionIdentifier);
                AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(view.getContext(), R.animator.image_shrinking);
                set.setTarget(imageView);
                set.start();
                justSelected = -1;
            } else if(isSelected.get(cursor.getPosition())){
            //    imageView.setAlpha(0.6f);
                imageView.setScaleX(0.8f);
                imageView.setScaleY(0.8f);
                showHiddenViews(selectionBackground, selectionIdentifier);
            }
            else if (justUnselected == cursor.getPosition()){
                justUnselected = -1;
                AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.image_enlarging);
                set.setTarget(imageView);
                set.start();
                selectionIdentifier.setVisibility(View.INVISIBLE);
                selectionBackground.setVisibility(View.INVISIBLE);
            }else if(selectedNumber > 0){
                selectionIdentifier.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_selection_candidate));
                selectionIdentifier.setVisibility(View.VISIBLE);
                selectionBackground.setVisibility(View.INVISIBLE);
          //      imageView.setAlpha(1f);
                    imageView.setScaleX(1f);
                    imageView.setScaleY(1f);
            }
            else{
                selectionBackground.setVisibility(View.INVISIBLE);
                selectionIdentifier.setVisibility(View.INVISIBLE);
                imageView.setScaleX(1f);
                imageView.setScaleY(1f);
            }

            Log.d(TAG, "bindView: " + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)));
        }

        private void showHiddenViews(View selectionBackground, ImageView selectionIdentifier) {
            selectionBackground.setVisibility(View.VISIBLE);
            selectionIdentifier.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_selection_done));
            selectionIdentifier.setVisibility(View.VISIBLE);
        }

        /**
         * this function can either assign a new curosr to the adapter, or just modify the variable {@link #isSelected} if something changed
         * the variable {@link #isSelectedArraySaved} is used to deletect if the orientation was changed in order to just set the old one instead of wholly new
         *
         * {@inheritDoc}
         */
        @Override
        public Cursor swapCursor(Cursor newCursor) {
            Log.d(TAG, "swapCursor: ");
            //// TODO: 12/07/17 adding and removing images from external resources should be handled in future releases
          /*  if(isSelected != null && !isSelectedArraySaved){
                int diffSize = newCursor.getCount() - isSelected.size();
                ArrayList<Boolean> tmp = new ArrayList<>(diffSize);
                tmp.addAll(Collections.nCopies(diffSize, Boolean.FALSE));
                tmp.addAll(isSelected);
                isSelected = tmp;
            } else */if(!isSelectedArraySaved){
                isSelected = new ArrayList<>(newCursor.getCount());
                isSelected.addAll(Collections.nCopies(newCursor.getCount(), Boolean.FALSE));
            }
            isSelectedArraySaved = false;
            progressBar.setVisibility(View.GONE);
            if (newCursor.getCount() == 0) {
                ButterKnife.findById(view, R.id.fragment_all_images_no_image).setVisibility(View.VISIBLE);
                gridView.setVisibility(View.GONE);
            } else{
                gridView.setVisibility(View.VISIBLE);
                ButterKnife.findById(view, R.id.fragment_all_images_no_image).setVisibility(View.GONE);
            }
            return super.swapCursor(newCursor);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            deleteSelectedImages();
        else
            Toast.makeText(getActivity(), "Permission denied !", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("selectionArray", isSelected);
        outState.putSerializable("selectionNum", selectedNumber);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: {
                gridView.setOnItemLongClickListener(this);
                for(int i = 0;i < isSelected.size();i++)
                    isSelected.set(i, Boolean.FALSE);
                selectedNumber = 0;
                justSelected = -1;
                justUnselected = -1;
                imagesCursorAdapter.notifyDataSetChanged();
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                setHasOptionsMenu(false);
                //// TODO: 22/07/17 handle on back pressed with items selected
                return true;
            }
            case R.id.item_delete: {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    if(getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                        getActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    } else
                        deleteSelectedImages();
                else
                    deleteSelectedImages();
                return true;
            }
            case R.id.item_create_album:
                //// TODO: 14/07/17 finish
                break;
            case R.id.item_share:{
                ArrayList<Uri> uris = new ArrayList<>();
                for(int i = 0;i < isSelected.size();i++){
                    if(isSelected.get(i)){
                        //// TODO: 15/07/17 replace with better implementation
                        cursor.moveToPosition(i);
                        uris.add(Uri.fromFile(new File(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)))));
                    }
                }
                Intent intent;
                if(isSelected.size() == 1)
                    intent = new Intent(Intent.ACTION_SEND);
                else
                    intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, uris);
                Intent chooser = Intent.createChooser(intent, "share through");
                startActivity(chooser);
            }
            case R.id.item_favorite:
                //// TODO: 14/07/17 finish
        }
        return false;
    }

    private void deleteSelectedImages() {
        new AlertDialog.Builder(getActivity())
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
                        StringBuilder builder = new StringBuilder("_id in ( ");
                        for(int i = 0; i < isSelected.size();i++)
                            if(isSelected.get(i)){
                                //// TODO: 15/07/17 replace with better implementation
                                cursor.moveToPosition(i);
                                builder.append(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)) + ", ");
                            }
                        builder.replace(builder.length() - 2, builder.length() - 1, ")");
                        Log.d(TAG, "onClick: " + builder.toString());
                        isSelected = null;
                        selectedNumber = 0;
                        justSelected = -1;
                        justUnselected = -1;
                        getActivity().getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, builder.toString(), null);
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        setHasOptionsMenu(false);
                        gridView.setOnItemLongClickListener(AllImagesFragment.this);
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_image_selection, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
