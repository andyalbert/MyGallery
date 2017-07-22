package com.project.mygallary.fragments;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.mygallary.R;

/**
 * @author andrew
 * @version 1.0
 * @since 18/7/2017
 */

public class ImageInfoFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String id = getArguments().getString("id");

        return inflater.inflate(R.layout.fragment_image_info, container, false);
    }

 class ImageInfoAsyncTaskLoader extends AsyncTaskLoader<Cursor>{

     public ImageInfoAsyncTaskLoader(Context context) {
         super(context);
     }

     @Override
     public Cursor loadInBackground() {
         return null;
     }

     @Override
     public void registerOnLoadCanceledListener(OnLoadCanceledListener<Cursor> listener) {
         super.registerOnLoadCanceledListener(listener);
     }
 }
}
