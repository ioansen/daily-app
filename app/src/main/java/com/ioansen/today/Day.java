package com.ioansen.today;

import java.time.LocalDate;

public class Day {

    private final long id;
    private final LocalDate date;
    private int score;
    private int goalsDone;
    private int goalsLeft;
    private int goalsAttempted;

    public Day(long id, LocalDate date) {
        this.id = id;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getGoalsDone() {
        return goalsDone;
    }

    public void setGoalsDone(int goalsDone) {
        this.goalsDone = goalsDone;
    }

    public int getGoalsAttempted() {
        return goalsAttempted;
    }

    public void setGoalsAttempted(int goalsAttempted) {
        this.goalsAttempted = goalsAttempted;
    }

    public int getGoalsLeft() {
        return goalsLeft;
    }

    public void setGoalsLeft(int goalsLeft) {
        this.goalsLeft = goalsLeft;
    }

    public void incGoalsDone(){
        goalsDone++;
    }

    public void descGoalsDone(){
        goalsDone--;
    }
    public void incGoalsAttempted(){
        goalsAttempted++;
    }

    public void descGoalsAttempted(){
        goalsAttempted--;
    }
}
