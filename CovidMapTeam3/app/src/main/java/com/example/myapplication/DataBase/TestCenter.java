package com.example.myapplication.DataBase;

public class TestCenter{
    private String name;
    private double lon;
    private double lat;

    public TestCenter(String name, double lat, double lon){
        this.name = name;
        this.lat = lat;
        this.lon = lon;
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
