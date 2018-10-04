package com.ioansen.today.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ioansen.today.Label;
import com.ioansen.today.R;
import com.ioansen.today.adapters.LabelsAdapter;
import com.ioansen.today.repositories.LabelsRepository;

/**
 * A simple {@link Fragment} subclass.
 */
public class LabelListFragment extends Fragment {

    /*private RecyclerView labelsRecycler;
    private LabelsAdapter labelsAdapter;*/
    public LabelListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LinearLayout root = (LinearLayout) inflater.inflate(R.layout.fragment_label_list, container, false);
        RecyclerView labelsRecycler = (RecyclerView) root.findViewById(R.id.labels_recycler);
        LabelsRepository labelsRepository = new LabelsRepository().init();
        final LabelsAdapter labelsAdapter = new LabelsAdapter(labelsRepository.getLabels(), R.layout.card_label);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        labelsRecycler.setLayoutManager(linearLayoutManager);
        labelsRecycler.setAdapter(labelsAdapter);
        labelsRepository.setReadListener(new LabelsRepository.ReadListener() {
            @Override
            public void onAddLabel(int position, Label label) {
                labelsAdapter.notifyItemInserted(position);
            }

            @Override
            public void onRemoveLabel(int position) {
                labelsAdapter.notifyItemRemoved(position);
            }
        });
        labelsRepository.loadLabelsAsync();
        return root;
    }

}
