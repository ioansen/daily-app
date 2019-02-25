package com.ioansen.dailyapp.repositories;

import com.ioansen.dailyapp.database.TodayDatabaseHelper;

public class RecursiveGoalsRepository extends GoalsRepository {

    @Override
    protected void loadGoals() {
        // Do an asynchronous operation to fetch users.
        new TodayDatabaseHelper.ReadGoalsTask("is_recursive = ?", new String[]{String.valueOf(1)}, null).setDelegate(this).execute();
    }

    @Override
    protected void updatePositions(Long[] idsOrder) {

    }
}

