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

    @Override
    public String toString() {
        return "TestCenter{" +
                "name='" + name + '\'' +
                ", lon=" + lon +
                ", lat=" + lat +
                '}';
    }
}
