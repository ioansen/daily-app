package com.ioansen.dailyapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.LongSparseArray;

import com.ioansen.dailyapp.Goal;
import com.ioansen.dailyapp.Label;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TodayDatabaseHelper extends SQLiteOpenHelper {


    public static class ReadGoalsTask extends AsyncTask<Void, Goal, List<Goal>> {

        public interface AsyncResponse {
            void onReadFinished(List<Goal> goals);
            void onAddAfterRead(Goal goal);
        }

        private AsyncResponse delegate = null;

        String selection;
        String[] selectionArgs;
        String orderBy;

        public ReadGoalsTask(String selection, String[] selectionArgs, String orderBy) {
            this.selection = selection;
            this.selectionArgs = selectionArgs;
            this.orderBy = orderBy;
        }

        public ReadGoalsTask setDelegate(AsyncResponse delegate) {
            this.delegate = delegate;
            return this;
        }

        @Override
        protected List<Goal> doInBackground(Void... iamvoid){

            List<Goal> goals = new ArrayList<>();
            SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
            db.beginTransaction();
            try(Cursor cursor = db.query("goal",
                    new String[] {"_id","name", "description","difficulty", "importance", "is_recursive"},
                    selection, selectionArgs, null, null, orderBy))

            {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {


                    Goal goal = new Goal.Builder(cursor.getString(1))
                            .id(cursor.getLong(0))
                            .description(cursor.getString(2))
                            .difficulty(cursor.getInt(3))
                            .importance(cursor.getInt(4))
                            .isRecursive(cursor.getInt(5) > 0)
                            .build();

                    goals.add(goal);
                    publishProgress(goal);
                }


            }
            db.setTransactionSuccessful();
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
            return goals;
        }

        @Override
        protected void onProgressUpdate(Goal... goals) {
            //Code that you want to run to publish the progress of your task
            if ( delegate != null ) {
                delegate.onAddAfterRead(goals[0]);
            }
        }

        @Override
        protected void onPostExecute(List<Goal> goals) {
            //Code that you want to run when the task is complete
            if ( delegate != null ) {
                delegate.onReadFinished(goals);
            }
        }
    }

    public static class ReadLabelsTask extends AsyncTask<Void, Label, List<Label>> {

        public interface AsyncResponse {
            void onReadFinished(List<Label> labels);
            void onAddAfterRead(Label label);
        }

        private AsyncResponse delegate = null;

        String selection;
        String[] selectionArgs;
        String orderBy;

        public ReadLabelsTask(String selection, String[] selectionArgs, String orderBy) {
            this.selection = selection;
            this.selectionArgs = selectionArgs;
            this.orderBy = orderBy;
        }

        public ReadLabelsTask setDelegate(AsyncResponse delegate) {
            this.delegate = delegate;
            return this;
        }

        @Override
        protected List<Label> doInBackground(Void... iamvoid){

            List<Label> labels = new ArrayList<>();
            SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
            db.beginTransaction();
            try(Cursor cursor = db.query("label",
                    new String[] {"_id","text", "color"},
                    selection, selectionArgs, null, null, orderBy))

            {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    Label label = new Label(cursor.getLong(0), cursor.getString(1), cursor.getInt(2));
                    labels.add(label);
                    publishProgress(label);
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
            return labels;
        }

        @Override
        protected void onProgressUpdate(Label... labels) {
            //Code that you want to run to publish the progress of your task
            if ( delegate != null ) {
                delegate.onAddAfterRead(labels[0]);
            }
        }

        @Override
        protected void onPostExecute(List<Label> labels) {
            //Code that you want to run when the task is complete
            if ( delegate != null ) {
                delegate.onReadFinished(labels);
            }
        }
    }

    public static class ReadGoalLabelsTask extends AsyncTask<Long, Label, List<Label>> {

        public interface AsyncResponse {
            void onReadFinished(List<Label> labels);
            void onAddAfterRead(Label label);
        }

        private AsyncResponse delegate = null;

        public ReadGoalLabelsTask setDelegate(AsyncResponse delegate) {
            this.delegate = delegate;
            return this;
        }

        @Override
        protected List<Label> doInBackground(Long... goalsIds){

            long goalId = goalsIds[0];
            List<Label> labels = new ArrayList<>();
            SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
            db.beginTransaction();
            try(Cursor cursor = db.query("goal_label",
                    new String[] {"label_id"},
                    "goal_id = ?", new String[]{String.valueOf(goalId)}, null, null, null))

            {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                    try(Cursor lblcursor = db.query("label",
                            new String[] {"text", "color"},
                            "_id = ?", new String[]{String.valueOf(cursor.getLong(0))}, null, null, null))

                    {
                        lblcursor.moveToFirst();
                        Label label = new Label(cursor.getLong(0), lblcursor.getString(0), lblcursor.getInt(1));
                        labels.add(label);
                        publishProgress(label);

                    }
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
            return labels;
        }

        @Override
        protected void onProgressUpdate(Label... labels) {
            //Code that you want to run to publish the progress of your task
            if ( delegate != null ) {
                delegate.onAddAfterRead(labels[0]);
            }
        }

        @Override
        protected void onPostExecute(List<Label> labels) {
            //Code that you want to run when the task is complete
            if ( delegate != null ) {
                delegate.onReadFinished(labels);
            }
        }
    }

    public static class GetDayGoalsTask extends AsyncTask<Void, Long, LongSparseArray<Boolean>> {

        public interface AsyncResponse {
            void onGetFinished(LongSparseArray<Boolean> goalDoneMap);
        }

        private AsyncResponse delegate = null;

        public GetDayGoalsTask setDelegate(AsyncResponse delegate) {
            this.delegate = delegate;
            return this;
        }

        @Override
        protected LongSparseArray<Boolean> doInBackground(Void... iamvoid) {
            LongSparseArray<Boolean> goalDoneMap = new LongSparseArray<>();
            SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
            db.beginTransaction();
            long lastDayId = -1;
            //get last day
            try(Cursor dayCursor = db.query("day",
                    new String[] {"_id"}, null, null,null, null, null)) {
                if (dayCursor.moveToLast()) {
                    lastDayId = dayCursor.getLong(0);
                }
            }
            try(Cursor cursor = db.query("day_goal",
                    new String[] {"goal_id", "is_done"},
                    "day_id = ?", new String[]{String.valueOf(lastDayId)}, null, null, null))

            {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    goalDoneMap.put(cursor.getLong(0), cursor.getInt(1) > 0);
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
            return goalDoneMap;
        }

        @Override
        protected void onPostExecute(LongSparseArray<Boolean> goalDoneMap) {
            if(delegate != null) {
                delegate.onGetFinished(goalDoneMap);
            }
        }
    }


    public static class UpdateGoalsTodayPosTask extends AsyncTask<Long, Goal, Void> {

        public interface AsyncResponse {
            void onUpdateTodayPosFinished();
            void onUpdateTodayPos(Goal goal);
        }

        String selection;
        String[] selectionArgs;
        String orderBy;

        public UpdateGoalsTodayPosTask(String selection, String[] selectionArgs) {
            this.selection = selection;
            this.selectionArgs = selectionArgs;
        }

        private AsyncResponse delegate = null;

        UpdateGoalsTodayPosTask setDelegate(AsyncResponse delegate) {
            this.delegate = delegate;
            return this;
        }

        @Override
        protected Void doInBackground(Long... idsorder){

            SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
            db.beginTransaction();
            for (int i = 0 ; i< idsorder.length; i++) {
                ContentValues values = new ContentValues();
                values.put("day_pos", i);
                db.update("goal", values, "_id = ?", new String[]{String.valueOf(idsorder[i])});

            }
            db.setTransactionSuccessful();
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
            return null;
        }

        @Override
        protected void onProgressUpdate(Goal... goals) {
            //Code that you want to run to publish the progress of your task
            if ( delegate != null ) {
                delegate.onUpdateTodayPos(goals[0]);
            }
        }

        @Override
        protected void onPostExecute(Void v) {
            //Code that you want to run when the task is complete
            if ( delegate != null ) {
                delegate.onUpdateTodayPosFinished();
            }
        }
    }

    public static class RemoveLabelTask extends AsyncTask<Label, Void, Void> {

        @Override
        protected Void doInBackground(Label... labels){

            Label label = labels[0];
            SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
            db.beginTransaction();
            try(Cursor cursor = db.query("goal_label",
                    new String[] {"goal_id"},
                    "label_id = ?", new String[]{String.valueOf(label.getId())}, null, null, null))

            {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    /*ContentValues goalValues = new ContentValues();
                    goalValues.put("label_id", Label.NONE.getId());
                    db.update("goal", goalValues, "_id = ?", new String[]{String.valueOf(cursor.getLong(0))});*/
                    deleteGoalLabel(db, cursor.getLong(0), label.getId());
                }
            }
            deleteLabel(db, label.getId());
            db.setTransactionSuccessful();
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
            return null;
        }
    }

    public static class InsertGoalLabelsTask extends AsyncTask<Label, Void, Void> {

        private long goalId;

        public InsertGoalLabelsTask setGoal(long goalId){
            this.goalId = goalId;
            return this;
        }

        @Override
        protected Void doInBackground(Label... labels){

            SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
            db.beginTransaction();
            for(Label label: labels){
                insertGoalLabel(db, goalId, label.getId());
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
            return null;
        }
    }

    public static class UpdateGoalLabelsTask extends AsyncTask<Label, Void, Void> {

        private long goalId;

        public UpdateGoalLabelsTask setGoal(long goalId){
            this.goalId = goalId;
            return this;
        }

        @Override
        protected Void doInBackground(Label... labels){

            List<Label> labelsToAdd = new ArrayList<>(Arrays.asList(labels));
            boolean found;
            SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
            db.beginTransaction();
            try(Cursor cursor = db.query("goal_label",
                    new String[] {"label_id"},
                    "goal_id = ?", new String[]{String.valueOf(goalId)}, null, null, null))

            {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    found = false;
                    for (Label label : labels){
                        if ( cursor.getLong(0) == label.getId()){
                            labelsToAdd.remove(label);
                            found = true;
                            break;
                        }
                    }
                    if (!found){
                        deleteGoalLabel(db, goalId, cursor.getLong(0));
                    }
                }
                Label[] addLabels = new Label[labelsToAdd.size()];
                new InsertGoalLabelsTask().setGoal(goalId).execute(labelsToAdd.toArray(addLabels));

            }
            db.setTransactionSuccessful();
            db.endTransaction();
            DatabaseManager.getInstance().closeDatabase();
            return null;
        }
    }

    private static final String DB_NAME = "todaysdatabase"; // the name of our database
    private static final int DB_VERSION = 1; // the version of the database

    public TodayDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { updateMyDatabase(db, 0, DB_VERSION); }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateMyDatabase(db, oldVersion, newVersion);
    }

    private void updateMyDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {

            db.execSQL("CREATE TABLE label ("
                    + "_id integer primary key autoincrement, "
                    + "text text, "
                    + "color integer);");

            db.execSQL("CREATE TABLE goal ("
                    + "_id integer primary key autoincrement, "
                    + "name text, "
                    + "description text, "
                    + "difficulty integer, "
                    + "importance integer, "
                    + "date_created integer, "
                    + "day_pos integer, "
                    + "is_recursive numeric);");

            db.execSQL("CREATE TABLE day ("
                    + "_id integer primary key autoincrement, "
                    + "date integer, "
                    + "done integer, "
                    + "attempted integer, "
                    + "score integer);");

            db.execSQL("CREATE TABLE day_goal ("
                    + "_id integer primary key autoincrement, "
                    + "goal_id integer, "
                    + "day_id integer, "
                    + "goal_name text, "
                    + "score integer, "
                    + "is_done numeric, "
                    + "FOREIGN KEY(goal_id) REFERENCES goal(_id), "
                    + "FOREIGN KEY(day_id) REFERENCES day(_id));");

            db.execSQL("CREATE TABLE goal_label ("
                    + "_id integer primary key autoincrement, "
                    + "goal_id integer, "
                    + "label_id integer, "
                    + "FOREIGN KEY(goal_id) REFERENCES goal(_id), "
                    + "FOREIGN KEY(label_id) REFERENCES label(_id));");

            insertDay(db, System.currentTimeMillis());
            insertLabel(db, new Label("Photography", 0xb33000aa));
            insertLabel(db, new Label("Work", 0xb333aa33));
            insertLabel(db, new Label("Self-Dev", 0xeeffcc00));
            insertLabel(db, new Label("School", 0xb300aadd));
            insertLabel(db, new Label("Sport", 0xb3cc0000));
        }

    }

    public static long addDatabaseLabel(Label label){
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        long id = insertLabel(db, label);
        DatabaseManager.getInstance().closeDatabase();
        return  id;
    }

    public static long addDatabaseGoal(Goal goal){
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        long id = insertGoal(db, goal);
        DatabaseManager.getInstance().closeDatabase();
        return  id;
    }

    public static long addDatabaseDay(LocalDate date){
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        long id = insertDay(db, date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
        DatabaseManager.getInstance().closeDatabase();
        return id;
    }

    public static void addDatabaseDayGoal(long dayId, Goal goal){
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        insertDayGoal(db, dayId, goal);
        DatabaseManager.getInstance().closeDatabase();
    }

    public static void addDatabaseGoalLabel(long goalId, long labelId){
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        insertGoalLabel(db, goalId, labelId);
        DatabaseManager.getInstance().closeDatabase();
    }

    public static void addDatabaseTodayGoal(Goal goal){
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        try(Cursor dayCursor = db.query("day",
                new String[] {"_id"}, null, null,null, null, null)) {
            if (dayCursor.moveToLast()) {
                insertDayGoal(db, dayCursor.getLong(0), goal);
            }
        }
        DatabaseManager.getInstance().closeDatabase();
    }

    public static void removeDatabaseLabel(long id){
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        deleteLabel(db, id);
        DatabaseManager.getInstance().closeDatabase();
    }

    public static void removeDatabaseGoal(long id){
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        deleteGoal(db, id);
        DatabaseManager.getInstance().closeDatabase();
    }

    public static void removeDatabaseDay(long id){
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        deleteDay(db, id);
        DatabaseManager.getInstance().closeDatabase();
    }

    public static void removeDatabaseDayGoal(long dayId, long goalId){
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        deleteDayGoal(db, dayId, goalId);
        DatabaseManager.getInstance().closeDatabase();
    }

    public static void removeDatabaseGoalLabel(long goalId, long labelId){
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        deleteGoalLabel(db, goalId, labelId);
        DatabaseManager.getInstance().closeDatabase();
    }

    public static void updateDatabaseLabel(Label label){
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        updateLabel(db, label);
        DatabaseManager.getInstance().closeDatabase();
    }
    public static void updateDatabaseGoal(Goal goal){
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        updateGoal(db, goal);
        DatabaseManager.getInstance().closeDatabase();
    }

    public static void updateDatabaseDayGoalDone(long dayId, long goalId, boolean done){
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        updateDayGoalDone(db, dayId, goalId, done);
        DatabaseManager.getInstance().closeDatabase();
    }

    private static long insertLabel(SQLiteDatabase db, Label label) {
        ContentValues labelValues = new ContentValues();
        labelValues.put("text", label.getText());
        labelValues.put("color", label.getColor());
        return db.insert("label", null, labelValues);
    }

    private static long insertGoal(SQLiteDatabase db, Goal goal) {
        ContentValues goalValues = new ContentValues();
        goalValues.put("name", goal.getName());
        goalValues.put("description", goal.getDescription());
        goalValues.put("difficulty", goal.getDifficulty());
        goalValues.put("importance", goal.getImportance());
        goalValues.put("date_created", System.currentTimeMillis());
        goalValues.put("is_recursive", goal.isRecursive());
        return db.insert("goal", null, goalValues);
    }

    private static long insertDay(SQLiteDatabase db, long millis) {
        ContentValues dayValues = new ContentValues();
        dayValues.put("date", millis);
        return db.insert("day", null, dayValues);
    }

    private static void insertDayGoal(SQLiteDatabase db, long dayId, Goal goal) {
        ContentValues dayGoalValues = new ContentValues();
        dayGoalValues.put("goal_id", goal.getId());
        dayGoalValues.put("day_id", dayId);
        dayGoalValues.put("goal_name", goal.getName());
        db.insert("day_goal", null, dayGoalValues);
    }

    private static void insertGoalLabel(SQLiteDatabase db, long goalId, long labelId) {
        ContentValues goalLabelValues = new ContentValues();
        goalLabelValues.put("goal_id", goalId);
        goalLabelValues.put("label_id", labelId);
        db.insert("goal_label", null, goalLabelValues);
    }

    private static void deleteLabel(SQLiteDatabase db, long id) {
        db.delete("label","_id = ?", new String[] {String.valueOf(id)});
    }

    private static void deleteGoal(SQLiteDatabase db, long id) {
        db.delete("goal","_id = ?", new String[] {String.valueOf(id)});
    }

    private static void deleteDay(SQLiteDatabase db, long id) {
        db.delete("day","_id = ?", new String[] {String.valueOf(id)});
    }

    private static void deleteDayGoal(SQLiteDatabase db, long day_id, long goal_id) {
        db.delete("day_goal","goal_id = ? and day_id = ?", new String[] {String.valueOf(goal_id), String.valueOf(day_id)});
    }

    private static void deleteGoalLabel(SQLiteDatabase db, long goalId, long labelId) {
        db.delete("goal_label","goal_id = ? and label_id = ?", new String[] {String.valueOf(goalId), String.valueOf(labelId)});
    }

    private static void updateGoal(SQLiteDatabase db, Goal goal) {
        ContentValues goalValues = new ContentValues();
        goalValues.put("name", goal.getName());
        goalValues.put("description", goal.getDescription());
        goalValues.put("difficulty", goal.getDifficulty());
        goalValues.put("importance", goal.getImportance());
        goalValues.put("date_created", System.currentTimeMillis());
        goalValues.put("is_recursive", goal.isRecursive());
        db.update("goal", goalValues, "_id = ?", new String[]{String.valueOf(goal.getId())});
    }

    private static void updateLabel(SQLiteDatabase db, Label label) {
        ContentValues labelValues = new ContentValues();
        labelValues.put("text", label.getText());
        labelValues.put("color", label.getColor());
        db.update("label", labelValues, "_id = ?", new String[]{String.valueOf(label.getId())});
    }

    private static void updateDayGoalDone(SQLiteDatabase db, long dayId, long goalId, boolean done) {
        ContentValues dayGoalValues = new ContentValues();
        dayGoalValues.put("is_done", done);
        db.update("day_goal",dayGoalValues,"goal_id = ? and day_id = ?", new String[] {String.valueOf(goalId), String.valueOf(dayId)});
    }


}
