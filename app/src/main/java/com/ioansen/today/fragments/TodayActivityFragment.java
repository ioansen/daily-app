package com.ioansen.today.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ioansen.today.Database.TodayDatabaseHelper;
import com.ioansen.today.Day;
import com.ioansen.today.Goal;
import com.ioansen.today.repositories.DayRepository;
import com.ioansen.today.repositories.GoalsRepository;
import com.ioansen.today.R;
import com.ioansen.today.repositories.TodayGoalsRepository;
import com.ioansen.today.adapters.SwipeAndDragHelper;
import com.ioansen.today.adapters.GoalsAdapter;
import com.ioansen.today.Database.DatabaseManager;
import com.ioansen.today.viewmodels.DayViewModel;
import com.ioansen.today.viewmodels.GoalsViewModel;

import java.time.Instant;
import java.util.List;


/**
 * A fragment containing a list of the {@link Goal [goals]}
 * of the current {@link Day [Day]}.
 *
 * @since 1.0
 */
public class TodayActivityFragment extends Fragment {

    private GoalsAdapter goalsAdapter;
    private GoalsViewModel goalsModel;
    private RecyclerView goalsRecycler;
    private OnFragmentInteractionListener listener;
    private GoalsRepository todayGoalsRepository;

    /**This is the day from which the fragment inserts goals,
     * update goals and deletes goals and display goals.
     * It is provided by the enclosing Activity via DayViewModel
     * and retrieved with {@link com.ioansen.today.viewmodels.DayViewModel#getToday}*/
    private Day today;

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

        /**Called when a goal is removed from today's list.
         * @param position the position of the removed goal in the moment of its removal
         * @param goal the removed goal*/
        void onRemoveGoalFromToday(int position, Goal goal);

        /**Called when one of the following changes has occurred in the list:
         * goal was added to the list
         * goal was removed from the list
         * goal's {@link Goal#isDone [isDone]} has changed.*/
        void updateProgress();

        /**Called when one of the goals layout has been clicked.
         * @param position the position of the goal in the moment of the click
         * @param goal the clicked goal*/
        void onGoalClick(int position, Goal goal);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        goalsRecycler = (RecyclerView)inflater.inflate(R.layout.fragment_today, container, false);

        return goalsRecycler;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if ( getActivity() != null) {
            goalsModel = ViewModelProviders.of(getActivity()).get(GoalsViewModel.class);
            today = ViewModelProviders.of(getActivity()).get(DayViewModel.class).getToday();
        }
        todayGoalsRepository = new TodayGoalsRepository();
        todayGoalsRepository.setListener(new GoalsRepository.ReadListener() {
            @Override
            public void onAddGoal(Goal goal) {
                //Toast.makeText(getContext(), "Goal added: " + goal.getId(), Toast.LENGTH_SHORT).show();
                goalsAdapter.notifyItemInserted(goalsModel.getSize() - 1);
            }

            @Override
            public void onRemoveGoal(int position) {
               // Toast.makeText(getContext(), "Goal removed: " + goal.getId(), Toast.LENGTH_SHORT).show();
                goalsAdapter.notifyItemRemoved(position);
                goalsAdapter.notifyItemChanged(position, goalsModel.getSize());
            }

            @Override
            public void onAddGoalPosition(int position, Goal goal) {
               // Toast.makeText(getContext(), "Goal added: " + goal.getId(), Toast.LENGTH_SHORT).show();
                goalsAdapter.notifyItemInserted(position);
                goalsAdapter.notifyItemChanged(position);
            }

        });
        setupGoalsRecyclerView();
    }

    private void setupGoalsRecyclerView() {
        goalsModel.init(todayGoalsRepository);
        goalsAdapter = new GoalsAdapter(goalsModel.getGoals(), R.layout.card_goal);
        goalsAdapter.setContext(getContext());
        final LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        goalsRecycler.setLayoutManager(llm);
        goalsRecycler.setAdapter(goalsAdapter);
        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(goalsAdapter);
        final ItemTouchHelper touchHelper = new ItemTouchHelper(swipeAndDragHelper);
        touchHelper.attachToRecyclerView(goalsRecycler);
        goalsAdapter.setListener(new GoalsAdapter.Listener() {
            @Override
            public void onSwipeGoal(Goal goal, int position) {
                listener.onRemoveGoalFromToday(position, goal);
            }

            @Override
            public void onClickGoal(Goal goal, int position) {
                listener.onGoalClick(position, goal);
            }
        });
        goalsAdapter.setOnStartDragListener(new GoalsAdapter.OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                touchHelper.startDrag(viewHolder);
            }
        });
        goalsAdapter.setOnGoalDoneListener(new GoalsAdapter.OnGoalDoneListener() {
            @Override
            public void onGoalDone(Goal goal, int position) {
                ImageView doneView = (ImageView) llm.findViewByPosition(position).findViewById(R.id.done);
                View overlay = llm.findViewByPosition(position).findViewById(R.id.overlay);
                View overlayBoolean = llm.findViewByPosition(position).findViewById(R.id.overlay_boolean);
                if (goal.isDone()){
                    goal = goal.setDone(false);
                    doneView.setImageResource(R.drawable.ic_done_black_48dp);
                    today.descGoalsDone();
                    TodayDatabaseHelper.updateDatabaseDayGoalDone(today.getId(), goal.getId(), false);
                    overlay.setVisibility(View.INVISIBLE);
                    overlayBoolean.setVisibility(View.INVISIBLE);
                }
                else {
                    goal = goal.setDone(true);
                    doneView.setImageResource(R.drawable.ic_done_green_48dp);
                    today.incGoalsDone();
                    TodayDatabaseHelper.updateDatabaseDayGoalDone(today.getId(), goal.getId(), true);
                    overlay.setVisibility(View.VISIBLE);
                    overlayBoolean.setVisibility(View.VISIBLE);

                }
                listener.updateProgress();

            }
        });
        todayGoalsRepository.load();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TodayActivityFragment.OnFragmentInteractionListener) {
            listener = (TodayActivityFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TodayActivityFragment.Listener");
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        todayGoalsRepository.updateTodayPositions();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //db.close();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
