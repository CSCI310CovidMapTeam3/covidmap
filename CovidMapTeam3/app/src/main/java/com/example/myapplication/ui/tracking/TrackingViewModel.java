package com.example.myapplication.ui.tracking;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TrackingViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public TrackingViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Tracking feature is under development");
    }

    public LiveData<String> getText() {
        return mText;
    }
}