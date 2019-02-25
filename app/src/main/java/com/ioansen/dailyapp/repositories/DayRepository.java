package com.ioansen.dailyapp.repositories;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ioansen.dailyapp.database.DatabaseManager;
import com.ioansen.dailyapp.database.TodayDatabaseHelper;
import com.ioansen.dailyapp.Day;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class DayRepository {

    private Day day;

    {
        loadDay();
        loadDayStats();
    }

    private void loadDay(){
        LocalDate todayDate = Instant.now().atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate lastDayDate;
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        try(Cursor cursor = db.query("day",
                new String[] {"_id", "date"},
                null, null, null, null, null))
        {
            if (cursor.moveToLast()) {
                lastDayDate = Instant.ofEpochMilli(cursor.getLong(1)).atZone(ZoneOffset.UTC).toLocalDate();
                if ( todayDate.isAfter(lastDayDate)){
                    //new day
                    long id = TodayDatabaseHelper.addDatabaseDay(todayDate);
                    day = new Day(id, todayDate);
                }
                else {
                    //already opened dailyapp
                    day = new Day(cursor.getLong(0), todayDate);
                }
            }
        }
        DatabaseManager.getInstance().closeDatabase();
    }

    private void loadDayStats(){
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        int numberOfDayGoals = 0;
        int numberOfDayDoneGoals = 0;
        try(Cursor cursor = db.query("day_goal",
                new String[] {"is_done"},
                "day_id = ?", new String[]{String.valueOf(day.getId())}, null, null, null))
        {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                numberOfDayGoals++;
                if ( cursor.getInt(0) == 1) { numberOfDayDoneGoals++;}
            }
        }
        DatabaseManager.getInstance().closeDatabase();
        day.setGoalsAttempted(numberOfDayGoals);
        day.setGoalsDone(numberOfDayDoneGoals);
    }

    public Day getDay(){
        return day;
    }

}
