package com.example.myapplication.ui.home;

import android.location.Location;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.lang.Math;

public class City {
    public String cityName;
    public int cityId;

    private Location center;
    private int population;
    private int caseNumber;
    private int deathNumber;
    private int recoveredCase;
    private int fourteenDayCaseNumber;



    public City(String cityName, int cityId, Location center, int population, int caseNumber, int deathNumber, int recoveredCase, int fourteenDayCaseNumber) {
        this.cityName = cityName;
        this.cityId = cityId;
        this.center = center;
        this.population = population;
        this.caseNumber = caseNumber;
        this.deathNumber = deathNumber;
        this.recoveredCase = recoveredCase;
        this.fourteenDayCaseNumber = fourteenDayCaseNumber;
    }

    public City(String name){
        cityName = name;
    }

    public String getCityName() {
        return cityName;
    }

    public Location getCenter() {
        return center;
    }

    public void setCenter(Location center) {
        this.center = center;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(int caseNumber) {
        this.caseNumber = caseNumber;
    }

    public int getDeathNumber() {
        return deathNumber;
    }

    public void setDeathNumber(int deathNumber) {
        this.deathNumber = deathNumber;
    }

    public int getRecoveredCase() {
        return recoveredCase;
    }

    public void setRecoveredCase(int recoveredCase) {
        this.recoveredCase = recoveredCase;
    }

    public int getFourteenDayCaseNumber() {
        return fourteenDayCaseNumber;
    }

    public void setFourteenDayCaseNumber(int fourteenDayCaseNumber) {
        this.fourteenDayCaseNumber = fourteenDayCaseNumber;
    }

    public int getCurrentCase() {
        return (this.caseNumber - this.recoveredCase);
    }

    public double getCaseRate() throws IllegalStateException {
        if (population == 0){
            throw new IllegalStateException("Population was not initialized");
        }
        return (caseNumber * (100000.0 / population));
    }

    public double getNewCaseRate() throws IllegalStateException {
        if (population == 0){
            throw new IllegalStateException("Population was not initialized");
        }
        return (fourteenDayCaseNumber * (100000.0 / population));
    }

    public double getDistanceBetweenCityCenterKM(City other) throws IllegalStateException {
        if (this.center == null){
            throw new IllegalStateException("Current City Center was not initialized");
        }
        if (other.center == null){
            throw new IllegalStateException("Other City Center was not initialized");
        }
        double latitude = this.center.getLatitude();
        double longitude = this.center.getLongitude();
        double other_latitude = other.center.getLatitude();
        double other_longitude = other.center.getLongitude();
        double delta = other_longitude - longitude;
        return Haversine(latitude, longitude, other_latitude, other_longitude);
    }

    static double Haversine(double lat1, double lon1, double lat2, double lon2)
    {
        // distance between latitudes and longitudes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(lat1) *
                        Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
    }

    float getMarkerColor()
    {
        // When case data is presented, the Color of Marker goes from Green (HUE_GREEN = 120) to Red (HUE_RED = 0)
        // When case data is missing, it will return blue (HUE_BLUE = 240)
        try{
            double currentCaseRate = this.getCaseRate();
            if (currentCaseRate >= 10000.0){
                return BitmapDescriptorFactory.HUE_RED;
            }
            else if (currentCaseRate <= 0.0){
                return BitmapDescriptorFactory.HUE_GREEN;
            }
            else{
                return (float) (((10000.0 - currentCaseRate) / 10000.0) * 120.0);
            }
        } catch(IllegalStateException e){
            return BitmapDescriptorFactory.HUE_BLUE;
        }
    }
}
