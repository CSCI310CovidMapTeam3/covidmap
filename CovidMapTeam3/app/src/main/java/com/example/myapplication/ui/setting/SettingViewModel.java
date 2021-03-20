package com.example.myapplication.ui.setting;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<String> mCity;

    public SettingViewModel() {
        mText = new MutableLiveData<>();
        mCity = new MutableLiveData<>();
        mText.setValue("Setting");
        mCity.setValue("Santa Monica");
    }

    public void setCity(String c) {
        if (mCity == null){
            mCity = new MutableLiveData<>();
        }
        mCity.setValue(c);
        Log.d("In Set City", mCity.getValue());
    }

    public LiveData<String> getText() {
        return mText;
    }
    public LiveData<String> getCity() {
        if (mCity == null){
            mCity = new MutableLiveData<>();
        }

        return mCity;
    }
}