package com.ioansen.today.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.ioansen.today.Day;
import com.ioansen.today.repositories.DayRepository;

import java.time.LocalDate;

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
