package com.ioansen.today.repositories;

import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;

import com.ioansen.today.Database.TodayDatabaseHelper;
import com.ioansen.today.Goal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class GoalsRepository
        implements TodayDatabaseHelper.ReadGoalsTask.AsyncResponse {

    private List<Goal> goals = new ArrayList<>();
    private ReadListener listener;
    private OnLoadFinishedListener onLoadFinishedListener;

    protected abstract void loadGoals();
    protected abstract void updatePositions(Long[] idsOrder);


    public interface ReadListener {
        void onAddGoal(Goal goal);
        void onRemoveGoal(int position);
        void onAddGoalPosition(int position, Goal goal);

    }

    public interface OnLoadFinishedListener {
        void onLoadFinished(List<Goal> goals);
    }

    public void load(){
        loadGoals();
    }

    public List<Goal> getGoals() {
        return goals;
    }

    public List<Goal> getGoals(long[] bannedIds) {
        List<Goal> newList = new ArrayList<>();
        boolean found;
        for(Goal goal: goals){
            found = false;
            for ( long id : bannedIds){
                if (goal.getId() == id){
                    found = true;
                    break;
                }
            }
            if(!found){
                newList.add(goal);
            }
        }
        return newList;
    }

    public void addGoal(Goal goal) {
            goals.add(goal);
            if (listener != null){
                listener.onAddGoal(goal);
            }
    }

    public void addGoal(int position, Goal goal) {
            goals.add(position, goal);
            if ( listener != null){
                listener.onAddGoalPosition(position, goal);
            }
    }

    public void removeGoal(int position) {
            goals.remove(position);
            if ( listener != null){
                listener.onRemoveGoal(position);
            }
    }

    public void updateTodayPositions() {
        Long[] idsOrder = new Long[goals.size()];
        for(int i = 0; i < goals.size(); i++ ){
            idsOrder[i] = goals.get(i).getId();
        }
        updatePositions(idsOrder);
    }

    public String[] getNamesArray(){
        String[] names = new String[goals.size()];
        for(int i = 0; i < names.length; i++){
            names[i] = goals.get(i).getName();
        }
        return names;
    }

    public String[] getNamesArray(long[] bannedIds){
        List<String> names = new ArrayList<>();
        boolean found;
        int i = 0;
        for(Goal goal: goals){
            found = false;
            for ( long id : bannedIds){
                if (goal.getId() == id){
                    found = true;
                    break;
                }
            }
            if(!found){
                names.add(goal.getName());
            }
        }
        String[] namesArray = new String[names.size()];
        return names.toArray(namesArray);
    }

    public SparseArray<Long> getPositionsMap(){
        SparseArray<Long> positionsMap = new SparseArray<>(goals.size());
        int i = 0;
        for(Goal goal : goals){
            positionsMap.put(i++, goal.getId());
        }

        return positionsMap;
    }

    public SparseArray<Long> getPositionsMap(long[] bannedIds){
        SparseArray<Long> positionsMap = new SparseArray<>(goals.size());
        int i = 0;
        boolean found;
        for(Goal goal: goals){
            found = false;
            for (long id : bannedIds){
                if (goal.getId() == id){
                    found = true;
                    break;
                }
            }
            if(!found){
                positionsMap.put(i++, goal.getId());
            }
        }
        return positionsMap;
    }

    public long[] getIdsArray(){
        long[] ids = new long[goals.size()];
        for(int i = 0; i < ids.length; i++){
            ids[i] = goals.get(i).getId();
        }
        return ids;
    }

    public Goal findGoalById(long id){
        for(Goal goal: goals){
            if (goal.getId() == id){
                return goal;
            }
        }
        return null;
    }

    private int getGoalPosition(long id){
        for (int i = 0; i < goals.size(); i++){
            if (goals.get(i).getId() == id) return i;
        }
        return -1;
    }

    public void updateGoal(Goal goal){
        int position = getGoalPosition(goal.getId());
        removeGoal(position);
        addGoal(position, goal);
    }

    @Override
    public void onReadFinished(List<Goal> fetchedGoals) {
        if ( onLoadFinishedListener != null){
            onLoadFinishedListener.onLoadFinished(fetchedGoals);
        }
    }

    @Override
    public void onAddAfterRead(Goal goal) {
        addGoal(goal);
    }


    public void setListener(ReadListener listener) {
        this.listener = listener;
    }
    public void setOnLoadFinishedListener( OnLoadFinishedListener listener) {
        this.onLoadFinishedListener = listener;
    }


}
