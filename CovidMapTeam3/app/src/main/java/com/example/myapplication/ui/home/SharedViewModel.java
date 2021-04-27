package com.example.myapplication.ui.home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Date;

public class SharedViewModel extends ViewModel {

    private MutableLiveData<String> name;
    private MutableLiveData<String> county;
    private MutableLiveData<Date> selectedDate;

    public void setNameData(String nameData) {
        if (name == null) {
            name = new MutableLiveData<>();
        }

        name.setValue(nameData);

    }

    public void setCountyData(String countyData) {
        if (county == null) {
            county = new MutableLiveData<>();
        }

        county.setValue(countyData);

    }

    public void setSelectedDate(Date date) {
        if (selectedDate == null) {
            selectedDate = new MutableLiveData<>();
        }

        selectedDate.setValue(date);

    }

    public void setNameDataBackground(String nameData) {
        if (name == null) {
            name = new MutableLiveData<>();
        }

        name.postValue(nameData);

    }

    public void setCountyDataBackground(String countyData) {
        if (county == null) {
            county = new MutableLiveData<>();
        }

        county.postValue(countyData);
    }

    public MutableLiveData<String> getNameData() {
        if (name == null) {
            name = new MutableLiveData<>();
        }

        return name;
    }

    public MutableLiveData<String> getCountyData() {
        if (county == null) {
            county = new MutableLiveData<>();
        }

        return county;
    }

    public MutableLiveData<Date> getSelectedDate() {
        if (selectedDate == null) {
            selectedDate = new MutableLiveData<>();
        }

        return selectedDate;
    }

    public String getNameDataBackground() {

        if (name == null) {
            name = new MutableLiveData<>();
        }

        name.postValue("Los Angeles City");

        return name.getValue();
    }

    public String getCountyDataBackground() {

        if (county == null) {
            county = new MutableLiveData<>();
        }

        county.postValue("Los Angeles City");

        return county.getValue();
    }
}