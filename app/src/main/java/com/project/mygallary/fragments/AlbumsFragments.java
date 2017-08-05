package com.project.mygallary.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.project.mygallary.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author andrew
 * @since 7/7/2017
 * @version 1.2
 */

public class AlbumsFragments extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    @BindView(R.id.fragment_albums_recyclerview)
    RecyclerView recyclerView;

    private View view;
    private Unbinder unbinder;
    private LinkedHashMap<String, ArrayList<String>> albums;
    private final String TAG = AlbumsFragments.class.getCanonicalName();
    private AlbumsRecyclerViewAdapter adapter = new AlbumsRecyclerViewAdapter();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_albums, container, false);
        unbinder = ButterKnife.bind(this, view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

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
        Log.d(TAG, "onCreateLoader: ");
        return new CursorLoader(getActivity(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        albums = new LinkedHashMap<>();
        long start = System.nanoTime();
        data.moveToFirst();
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
        Log.d(TAG, "onLoadFinished: " + (timeInNano) + "  " + albums.size());
        adapter.changeData(albums);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    class AlbumsRecyclerViewAdapter extends RecyclerView.Adapter<AlbumsRecyclerViewAdapter.ViewHolder>{
        private LinkedHashMap<String, ArrayList<String>> albums = new LinkedHashMap<>();
        private String[] mapKeys;
        private final int TYPE_SINGLE_IMAGE = 0;
        private final int TYPE_TWO_IMAGES = 1;
        private final int TYPE_FOUR_IMAGES = 2;

        @Override
        public int getItemViewType(int position) {
            int size = albums.get(mapKeys[position]).size();
            if(size >= 5)
                return TYPE_FOUR_IMAGES;
            if(size >= 3)
                return TYPE_TWO_IMAGES;
            return TYPE_SINGLE_IMAGE;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder: ");
            switch (viewType){
                case TYPE_FOUR_IMAGES:
                    return new FourImagesAlbumViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.list_item_album_four_images, parent, false));
                case TYPE_TWO_IMAGES:
                    return new TwoImagesAlbumViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.list_item_album_two_images, parent, false));
                default:
                    return new SingleImageAlbumViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.list_item_album_single_image, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Log.d(TAG, "test: " + albums.size() + " " + position);
            if(albums.size() == 0)
                return;
            ArrayList<String> imagesUri = albums.get(mapKeys[position]);
            holder.albumName.setText(mapKeys[position]);
            holder.imagesCount.setText(String.valueOf(imagesUri.size()));


            final int size = albums.get(mapKeys[position]).size();
            if(size >= 5) {
                loadImage(((FourImagesAlbumViewHolder) holder).firstImage, imagesUri.get(0));
                loadImage(((FourImagesAlbumViewHolder) holder).secondImage, imagesUri.get(1));
                loadImage(((FourImagesAlbumViewHolder) holder).thirdImage, imagesUri.get(2));
                loadImage(((FourImagesAlbumViewHolder) holder).fourthImage, imagesUri.get(3));
            } else if(size >= 3) {
                loadImage(((TwoImagesAlbumViewHolder) holder).firstImage, imagesUri.get(0));
                loadImage(((TwoImagesAlbumViewHolder) holder).secondImage, imagesUri.get(1));
            } else
                loadImage(((SingleImageAlbumViewHolder) holder).imageView, imagesUri.get(0));

        }

        @Override
        public int getItemCount() {
            return albums.size();
        }

        abstract class ViewHolder extends RecyclerView.ViewHolder{
            @BindView(R.id.list_item_album_name)
            TextView albumName;
            @BindView(R.id.list_item_album_images_number)
            TextView imagesCount;
            @BindView(R.id.list_item_album_spinner)
            Spinner spinner;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class SingleImageAlbumViewHolder extends ViewHolder{
            @BindView(R.id.list_item_single_image_image_1)
            ImageView imageView;

            SingleImageAlbumViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class TwoImagesAlbumViewHolder extends ViewHolder{
            @BindView(R.id.list_item_two_images_image_1)
            ImageView firstImage;
            @BindView(R.id.list_item_two_images_image_2)
            ImageView secondImage;

            TwoImagesAlbumViewHolder(View itemView){
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class FourImagesAlbumViewHolder extends ViewHolder{
            @BindView(R.id.list_item_four_images_image_1)
            ImageView firstImage;
            @BindView(R.id.list_item_four_images_image_2)
            ImageView secondImage;
            @BindView(R.id.list_item_four_images_image_3)
            ImageView thirdImage;
            @BindView(R.id.list_item_four_images_image_4)
            ImageView fourthImage;

            FourImagesAlbumViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        private void loadImage(ImageView imageView, String uri){
            Glide.with(getActivity())
                    .load(uri)
                    .into(imageView);
        }

        private void changeData(LinkedHashMap<String, ArrayList<String>> albums){
            this.albums = albums;
            mapKeys = albums.keySet().toArray(new String[albums.size()]);
            Log.d(TAG, "changeData: ");
            notifyDataSetChanged();
        }
    }
}
