package com.example.myapplication.ui.news;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

public class NewsViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<Map<String,String>> mWeatherMap;

    public NewsViewModel() {
        mText = new MutableLiveData<>();
        mWeatherMap = new MutableLiveData<>();
        mText.setValue("This is news feed fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public MutableLiveData<Map<String, String>> getWeatherMap() {
        return mWeatherMap;
    }
}