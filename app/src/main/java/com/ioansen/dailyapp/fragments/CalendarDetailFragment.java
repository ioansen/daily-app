package com.ioansen.dailyapp.fragments;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ioansen.dailyapp.viewmodels.LocalDateViewModel;
import com.ioansen.dailyapp.R;

import java.time.LocalDate;


/**
 * A simple {@link Fragment} subclass.
 */
public class CalendarDetailFragment extends Fragment {

    private LocalDateViewModel localeDateModel;
    private FrameLayout rootLayout;

    public CalendarDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootLayout = (FrameLayout) inflater.inflate(R.layout.fragment_calendar_detail, container, false);
        return rootLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final TextView detailTextView = (TextView) rootLayout.findViewById(R.id.detail_textview);
        localeDateModel = ViewModelProviders.of(getActivity()).get(LocalDateViewModel.class);
        localeDateModel.getLoalDate().observe(this, new Observer<LocalDate>() {
            @Override
            public void onChanged(@Nullable LocalDate localDate) {
                detailTextView.setText(localDate.toString());
            }
        });
    }
}
