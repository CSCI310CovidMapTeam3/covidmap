package com.example.myapplication.ui.home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private MutableLiveData<String> name;

    public void setNameData(String nameData) {
        if (name == null) {
            name = new MutableLiveData<>();
        }

        name.setValue(nameData);

/*
        If you are calling setNameData from a background thread use:
        name.postValue(nameData);
*/
    }

    public void setNameDataBackground(String nameData) {
        if (name == null) {
            name = new MutableLiveData<>();
        }

        name.postValue(nameData);

/*
        If you are calling setNameData from a background thread use:
        name.postValue(nameData);
*/
    }

    public MutableLiveData<String> getNameData() {
        if (name == null) {
            name = new MutableLiveData<>();
        }

        return name;
    }

    public String getNameDataBackground() {

        if (name == null) {
            name = new MutableLiveData<>();
        }

        name.postValue("Los Angeles City");

        return name.getValue();
    }
}