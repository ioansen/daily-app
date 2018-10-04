package com.ioansen.today.repositories;

import com.ioansen.today.Database.TodayDatabaseHelper;
import com.ioansen.today.repositories.GoalsRepository;

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

