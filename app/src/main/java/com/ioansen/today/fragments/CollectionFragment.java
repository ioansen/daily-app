package com.ioansen.today.fragments;

import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ioansen.today.Database.TodayDatabaseHelper;
import com.ioansen.today.Goal;
import com.ioansen.today.repositories.GoalsRepository;
import com.ioansen.today.repositories.RecursiveGoalsRepository;
import com.ioansen.today.adapters.GoalsAdapter;
import com.ioansen.today.viewmodels.GoalsViewModel;
import com.ioansen.today.R;
import com.ioansen.today.adapters.SwipeAndDragHelper;

import java.util.List;

public class CollectionFragment extends Fragment {

    private RecyclerView colRecycler;
    private GoalsAdapter colAdapter;
    private GoalsViewModel goalsModel;
    private GoalsRepository recursiveGoalRepo;
    private long removeId;
    private boolean remove;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        colRecycler = (RecyclerView)inflater.inflate(R.layout.fragment_collection, container, false);
        return colRecycler;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if ( getActivity() != null) {
            goalsModel = ViewModelProviders.of(getActivity()).get(GoalsViewModel.class);
        }
        recursiveGoalRepo = new RecursiveGoalsRepository();
        recursiveGoalRepo.setListener(new GoalsRepository.ReadListener() {
            @Override
            public void onAddGoal(Goal goal) {
                colAdapter.notifyDataSetChanged();
            }

            @Override
            public void onRemoveGoal(int position) {
                colAdapter.notifyItemRemoved(position);
            }

            @Override
            public void onAddGoalPosition(int position, Goal goal) {
                colAdapter.notifyItemInserted(position);
            }

        });
        setupColRecyclerView();
    }

    private void setupColRecyclerView() {
        goalsModel.init(recursiveGoalRepo);
        colAdapter = new GoalsAdapter(goalsModel.getGoals(), R.layout.card_collection_goal);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        colRecycler.setLayoutManager(linearLayoutManager);
        colRecycler.setAdapter(colAdapter);
        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(colAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeAndDragHelper);
        touchHelper.attachToRecyclerView(colRecycler);
        colAdapter.setListener(new GoalsAdapter.Listener() {
            @Override
            public void onSwipeGoal(final Goal goal, final int position) {
                goalsModel.removeGoal(position, goal);
                if(remove) TodayDatabaseHelper.removeDatabaseGoal(goal.getId());
                removeId = goal.getId();
                remove = true;
                Snackbar snackbar = Snackbar.make(colRecycler, " \"" + goal.getName() + "\" deleted from db" ,  Snackbar.LENGTH_LONG);
                snackbar.setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        goalsModel.addGoal(position, goal);
                        remove = false;
                    }
                });
                snackbar.show();
            }

            @Override
            public void onClickGoal(Goal goal, int position) {

            }
        });
        recursiveGoalRepo.load();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(remove) TodayDatabaseHelper.removeDatabaseGoal(removeId);
    }

}
