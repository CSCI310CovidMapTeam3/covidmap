package com.example.myapplication.DataBase;

public class TestCenter{
    private String name;
    private double lon;
    private double lat;
    private String address;

    public TestCenter(String name, double lat, double lon, String address){
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.address = address;
    }

    public String getName(){
        return this.name;
    }

    public double getLat() {
        return this.lat;
    }

    public String getAddress() {
        return this.address;
    }

    public double getLon() {
        return this.lon;
    }

}
