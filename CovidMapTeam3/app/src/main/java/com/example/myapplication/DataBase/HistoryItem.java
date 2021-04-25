package com.example.myapplication.DataBase;

import java.sql.Timestamp;

public class HistoryItem {
    private String cityName;
    private Double lat;
    private Double lon;
    private Timestamp timestamp;
    public HistoryItem(String name, double lat, double lon, Timestamp timestamp){
        this.cityName = name;
        this.lat = lat;
        this.lon = lon;
        this.timestamp = timestamp;
    }

    public String getCityName() {
        return cityName;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
