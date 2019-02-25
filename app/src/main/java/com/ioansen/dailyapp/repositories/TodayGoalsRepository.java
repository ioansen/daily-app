package com.ioansen.dailyapp.repositories;

import android.util.LongSparseArray;

import com.ioansen.dailyapp.database.TodayDatabaseHelper;
import com.ioansen.dailyapp.Goal;

public class TodayGoalsRepository extends GoalsRepository
    implements TodayDatabaseHelper.GetDayGoalsTask.AsyncResponse{

    private String[] goalsIds;
    private String selection;
    private LongSparseArray<Boolean> doneMap;

    @Override
    public void onGetFinished(LongSparseArray<Boolean> goalDoneMap) {
        int size = goalDoneMap.size();
        goalsIds = new String[size];
        if ( size == 0 ){
            return;
        }
        doneMap = goalDoneMap;
        StringBuilder selection = new StringBuilder("_id IN(?");
        goalsIds[0] = String.valueOf(goalDoneMap.keyAt(0));
        for(int i = 1; i < size; i++) {
            goalsIds[i] = String.valueOf(goalDoneMap.keyAt(i));
            selection.append(",?");
        }
        selection.append(")");
        this.selection = selection.toString();
        new TodayDatabaseHelper.ReadGoalsTask(this.selection, goalsIds, "day_pos").setDelegate(this).execute();

    }


    @Override
    protected void loadGoals() {
        new TodayDatabaseHelper.GetDayGoalsTask().setDelegate(this).execute();
    }

    @Override
    public void updatePositions(Long[] idsOrder) {
        new TodayDatabaseHelper.UpdateGoalsTodayPosTask(selection, goalsIds).execute(idsOrder);
    }

    @Override
    public void onAddAfterRead(Goal goal) {
        goal.setDone(doneMap.get(goal.getId()));
        addGoal(goal);
    }
}
