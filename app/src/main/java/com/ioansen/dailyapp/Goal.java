package com.ioansen.dailyapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>Goal</code> object defines what a human might call
 * a  goal and it is characterized by a
 * name, description, difficulty, importance and a recursive
 * boolean stating if the goal is a one time goal or a repeatable goal.
 * "Buy a new mouse" might be a non-recursive goal while
 * "Go to gym" might be recursive.
 * <p>
 * This class is immutable and can only be created through its builder.
 *  A <code>Goal</code> cannot be created without a name.
 *
 * @since 1.0
 */
public class Goal implements Parcelable{

    private final long id;
    private final String name;
    private final String description;

    /**An Integer value in the range of 0 - 10*/
    private final int difficulty;
    /**An Integer value in the range of 0 - 10*/
    private final int importance;

    private final boolean isRecursive;

    /**This states the done or not done state of the goal
     * in a specific {@link Day [Day]}*/
    private boolean isDone;

    /**The {@link Label [labels]} associated with this goal.*/
    private List<Label> labels;

    /**A builder class used to create {@link Goal [Goals]}*/
    public static class Builder {
        // Required parameters
        private final String name;
        // Optional parameters - initialized to default values
        private String description;
        private int difficulty;
        private int importance;
        private long id;
        private boolean isRecursive;
        private boolean isDone;
        private List<Label> labels;

        public Builder(String name) {
            this.name = name;
        }

        public Builder description(String description)
        { this.description = description; return this; }

        public Builder difficulty(int difficulty)
        {  this.difficulty = difficulty;return this; }

        public Builder importance(int importance)
        { this.importance = importance; return this; }

        public Builder id(long id)
        { this.id = id; return this; }

        public Builder isRecursive(boolean isRecursive)
        { this.isRecursive = isRecursive; return this; }

        public Builder isDone(boolean isDone)
        { this.isDone = isDone; return this; }

        public Builder labels(List<Label> labels)
        { this.labels = labels; return this; }


        public Goal build() {
            return new Goal(this);
        }
    }

    private Goal(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.difficulty = builder.difficulty;
        this.importance = builder.importance;
        this.description = builder.description;
        this.isRecursive = builder.isRecursive;
        this.isDone = builder.isDone;
        this.labels = builder.labels;
    }

    public Goal(Parcel source) {
        this.id = source.readLong();
        this.name = source.readString();
        this.description = source.readString();
        this.difficulty = source.readInt();
        this.importance = source.readInt();
        this.isRecursive = source.readByte() != 0;
        this.isDone = source.readByte() != 0;
        this.labels = new ArrayList<>();
        source.readTypedList(labels, Label.CREATOR);
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getImportance() {
        return importance;
    }


    public boolean isRecursive() {
        return isRecursive;
    }

    public boolean isDone() {
        return isDone;
    }

    public List<Label> getLabels() {
        return labels;
    }

    /**Appends an id to this goal by creating a new goal
     * with this goal's values and the inputed id.
     * This method is to replace setId() and it is mainly
     * needed because the id of a goal is usually set after
     * the goal has been appended to the db.
     * <p>
     * @return a new <code>Goal</code> representing
     * an identical goal with the id set*/
    public Goal addId(long id){
        return new Builder(this.name)
                .id(id)
                .description(description)
                .difficulty(difficulty)
                .importance(importance)
                .isRecursive(isRecursive)
                .isDone(isDone)
                .labels(labels)
                .build();
    }

    /**TODO
     * find a way to change this, maybe with a new mutable class
     * that has an id and a done
     * @param done the boolean value to set
     * @return a new <code>Goal</code> representing
     * an identical goal with the id set*/
    public Goal setDone(boolean done){
        return new Builder(name)
                .id(id)
                .description(description)
                .difficulty(difficulty)
                .importance(importance)
                .isRecursive(isRecursive)
                .isDone(done)
                .labels(labels)
                .build();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(difficulty);
        dest.writeInt(importance);
        dest.writeByte((byte) (isRecursive ? 1 : 0));
        dest.writeByte((byte) (isDone ? 1 : 0));
        dest.writeTypedList(labels);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Goal> CREATOR = new Creator<Goal>() {
        @Override
        public Goal createFromParcel(Parcel source) {
            return new Goal(source);
        }

        @Override
        public Goal[] newArray(int size) {
            return new Goal[0];
        }
    };
}
