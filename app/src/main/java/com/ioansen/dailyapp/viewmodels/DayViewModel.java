package com.ioansen.dailyapp.viewmodels;

import android.arch.lifecycle.ViewModel;

import com.ioansen.dailyapp.Day;
import com.ioansen.dailyapp.repositories.DayRepository;

public class DayViewModel extends ViewModel {

    private Day today;
    private DayRepository dayRepo;

    public void setDayRepository(DayRepository dayRepository) {
        dayRepo = dayRepository;
    }

    public void setToday() {
        if (today == null) {
            today = dayRepo.getDay();
        }
    }

    public Day getToday() {
        return today;
    }
}
