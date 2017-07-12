package com.project.mygallary.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.mygallary.R;

import butterknife.ButterKnife;

/**
 * @author andrew
 * @since 7/7/2017
 * @version 1.0
 */

public class DateImagesFragment extends Fragment {
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_date_images, container, false);
        ButterKnife.bind(this, view);
        return view;
    }
}
