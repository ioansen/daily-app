package com.ioansen.today.Database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager {

    private int mOpenCounter;

    private static DatabaseManager instance;
    private static SQLiteOpenHelper dbHelper;
    private SQLiteDatabase db;

    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (instance == null) {
            instance = new DatabaseManager();
            dbHelper = helper;
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }

        return instance;
    }

    public synchronized SQLiteDatabase getDatabase() {
        mOpenCounter++;
        if(mOpenCounter == 1) {
            // Opening new database
            db = dbHelper.getWritableDatabase();
        }
        return db;
    }

    public synchronized void closeDatabase() {
        mOpenCounter--;
        if(mOpenCounter == 0) {
            // Closing database
            db.close();

        }
    }

}
