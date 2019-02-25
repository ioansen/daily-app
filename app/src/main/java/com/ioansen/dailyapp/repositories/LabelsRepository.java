package com.ioansen.dailyapp.repositories;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ioansen.dailyapp.database.DatabaseManager;
import com.ioansen.dailyapp.database.TodayDatabaseHelper;
import com.ioansen.dailyapp.Label;

import java.util.ArrayList;
import java.util.List;

public class LabelsRepository implements
        TodayDatabaseHelper.ReadLabelsTask.AsyncResponse,
        TodayDatabaseHelper.ReadGoalLabelsTask.AsyncResponse
{

    private List<Label> labels;
    private ReadListener listener;

    public interface ReadListener{
        void onAddLabel(int position, Label label);
        void onRemoveLabel(int position);
    }
    public LabelsRepository init(){
        if (labels == null) {
            labels = new ArrayList<>();
        }
        return this;
    }

    public void setReadListener(ReadListener readListener){
        listener = readListener;
    }

    public void loadLabelsAsync(){
        new TodayDatabaseHelper.ReadLabelsTask(null, null, null).setDelegate(this).execute();
    }

    public void loadGoalsLabelsAsync(long goalId){
        new TodayDatabaseHelper.ReadGoalLabelsTask().setDelegate(this).execute(goalId);
    }

    public void loadLabels(){
        labels = new ArrayList<>();
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        db.beginTransaction();
        try(Cursor cursor = db.query("label",
                new String[] {"_id","text", "color"},
                null, null, null, null, null))

        {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                labels.add(new Label(cursor.getLong(0), cursor.getString(1), cursor.getInt(2)));
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        DatabaseManager.getInstance().closeDatabase();
    }

    public void loadGoalLabels(long goalId){
        labels = new ArrayList<>();
        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        db.beginTransaction();
        try(Cursor cursor = db.query("goal_label",
                new String[] {"label_id"},
                "goal_id = ?", new String[]{String.valueOf(goalId)}, null, null, null))

        {
            while(cursor.moveToNext()) {

                try(Cursor lblcursor = db.query("label",
                        new String[] {"text", "color"},
                        "_id = ?", new String[]{String.valueOf(cursor.getLong(0))}, null, null, null))

                {
                    if (lblcursor.moveToFirst()){
                        Label label = new Label(cursor.getLong(0), lblcursor.getString(0), lblcursor.getInt(1));
                        labels.add(label);
                    }


                }
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        DatabaseManager.getInstance().closeDatabase();
    }

    public List<Label> getLabels() {
        return labels;
    }

    public String[] getLabelTexts(){
        String[] labelTexts = new String[labels.size()];

        for(int i = 0; i < labels.size(); i++){
            labelTexts[i] = labels.get(i).getText();
        }

        return labelTexts;
    }

    public void addLabel(Label label){
        labels.add(label);
        if ( listener != null){
            listener.onAddLabel(labels.size()-1, label);
        }
    }

    public void addLabel(int position, Label label){
        labels.add(position, label);
        if ( listener != null){
            listener.onAddLabel(position, label);
        }
    }

    public void addPreLastLabel(Label label){
        addLabel(labels.size() - 1, label);
    }

    public void removeLabel(int position){

        labels.remove(position);
        if ( listener != null){
            listener.onRemoveLabel(labels.size()-1);
        }
    }

    public void removeLabel(Label label){

        int position = findLabelPosition(label);
        if (position != -1){
            labels.remove(position);
            if ( listener != null){
                listener.onRemoveLabel(position);
            }
        }

    }

    public void removeLabelRepository(LabelsRepository other){
        List<Label> otherLabels = other.getLabels();
        List<Label> toRemoveLabels = new ArrayList<>();

        for(Label label: labels){
            for ( Label oLabel: otherLabels){
                if (label.getId() == oLabel.getId()){
                    toRemoveLabels.add(label);
                }
            }
        }
        labels.removeAll(toRemoveLabels);
    }

    private int findLabelPosition(Label label){
        for ( int i = 0; i < labels.size(); i++){
            if ( labels.get(i).getId() == label.getId()){
                return i;
            }
        }
        //throw new AssertionError("Label: " + label.getId() + " not found");
        return -1;
    }

    public Label getLabelByPosition(int position){
        return labels.get(position);
    }

    @Override
    public void onAddAfterRead(Label label) {
        addLabel(label);
    }

    @Override
    public void onReadFinished(List<Label> labels) {
        if (this.labels == null){
            this.labels = labels;
        }
    }
}
