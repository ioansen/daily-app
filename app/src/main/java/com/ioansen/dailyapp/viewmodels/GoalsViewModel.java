package com.ioansen.dailyapp.viewmodels;


import android.arch.lifecycle.ViewModel;

import com.ioansen.dailyapp.Goal;
import com.ioansen.dailyapp.repositories.GoalsRepository;

import java.util.List;

public class GoalsViewModel extends ViewModel {

    private List<Goal> goals;
    private GoalsRepository goalRepo;

    public void init(GoalsRepository goalsRepository) {
        goalRepo = goalsRepository;
        if (goals == null) {
            goals = goalsRepository.getGoals();
        }
    }

    public List<Goal> getGoals() {
        if ( goals != null)
            return goals;
        throw new AssertionError("should call init() before getGoals()");
    }

    public void addGoal(Goal goal){
        goalRepo.addGoal(goal);
    }

    public void addGoal(int position, Goal goal){
        goalRepo.addGoal(position, goal);
    }
    public void removeGoal(int position, Goal goal){
        goalRepo.removeGoal(position);
    }
    public int getSize(){
        return goals.size();
    }

    public String[] getNamesArray(){
       return goalRepo.getNamesArray();
    }

    public String[] getNamesArray(long[] bannedIds){
        return goalRepo.getNamesArray(bannedIds);
    }

    public long[] getIdsArray(){
        return goalRepo.getIdsArray();
    }

   /* public Goal findGoalByName(String name){
       return goalRepo.findGoalByName(name);
    }*/

    public Goal findGoalById(long id){
       return  goalRepo.findGoalById(id);
    }

    public void updateGoal(Goal goal){
        goalRepo.updateGoal(goal);
    }

}
