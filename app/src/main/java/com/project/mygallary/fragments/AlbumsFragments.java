package com.project.mygallary.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.mygallary.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author andrew
 * @since 7/7/2017
 * @version 1.0
 */

public class AlbumsFragments extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private View view;
    private Unbinder unbinder;
    private HashMap<String, ArrayList<String>> albums;
    private final String TAG = AlbumsFragments.class.getCanonicalName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_albums, container, false);
        unbinder = ButterKnife.bind(this, view);

        getLoaderManager().initLoader(0, null, this);
        return view;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };
        return new CursorLoader(getActivity(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        albums = new HashMap<>();
        long start = System.nanoTime();
        while(data.moveToNext()){
            String bucketDisplayName = data.getString(data.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
            if(albums.containsKey(bucketDisplayName))
                albums.get(bucketDisplayName).add(data.getString(data.getColumnIndex(MediaStore.Images.Media.DATA)));
            else
                albums.put(bucketDisplayName, new ArrayList<String>(){
                    {
                        add(data.getString(data.getColumnIndex(MediaStore.Images.Media.DATA)));
                    }
                });
        }
        long timeInNano = System.nanoTime() - start;
        Log.d(TAG, "onLoadFinished: " + (timeInNano));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    class AlbumsRecyclerView extends RecyclerView.Adapter<AlbumsRecyclerView.ViewHolder>{
        private Context context;
        private HashMap<String, ArrayList<String>> albums;

        AlbumsRecyclerView(Context context, HashMap<String, ArrayList<String>> albums){
            this.context = context;
            this.albums = albums;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return albums.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{

            public ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
