package com.ioansen.dailyapp.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.time.LocalDate;

public class LocalDateViewModel extends ViewModel {

    private MutableLiveData<LocalDate> localDate = new MutableLiveData<>();

    public void setLocalDate(LocalDate localDate) {
        this.localDate.setValue(localDate);
    }

    public LiveData<LocalDate> getLoalDate() {
        return localDate;
    }


}
