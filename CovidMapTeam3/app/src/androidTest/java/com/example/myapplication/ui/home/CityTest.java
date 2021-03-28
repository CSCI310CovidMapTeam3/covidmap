package com.example.myapplication.ui.home;

import android.location.Location;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CityTest {
    City losAngeles;
    City santaMonica;

    // http://publichealth.lacounty.gov/media/coronavirus/locations.htm
    @Before
    public void setUp() throws Exception {
        losAngeles = new City("Los Angeles");
        losAngeles.cityId = 5;
        Location losAngelesCityCenter = new Location("");
        losAngelesCityCenter.setLatitude(34.0522);
        losAngelesCityCenter.setLongitude(-118.2437);
        losAngeles.setCenter(losAngelesCityCenter);
        losAngeles.setCaseNumber(492519);
        losAngeles.setDeathNumber(9154);
        losAngeles.setFourteenDayCaseNumber(10017);
        losAngeles.setPopulation(3792621);
        losAngeles.setRecoveredCase(394015);

        santaMonica = new City("Santa Monica");
        santaMonica.cityId = 1;
        Location santaMonicaCityCenter = new Location("");
        santaMonicaCityCenter.setLatitude(34.0195);
        santaMonicaCityCenter.setLongitude(-118.4912);
        santaMonica.setCenter(santaMonicaCityCenter);
        santaMonica.setCaseNumber(4515);
        santaMonica.setDeathNumber(156);
        santaMonica.setFourteenDayCaseNumber(57);
        santaMonica.setPopulation(91577);
        santaMonica.setRecoveredCase(3612);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetCurrentCase() {
        int realCurrentCaseLosAngeles = 98504;
        assertEquals(realCurrentCaseLosAngeles, losAngeles.getCurrentCase());
        int realCurrentCaseSantaMonica = 903;
        assertEquals(realCurrentCaseSantaMonica, santaMonica.getCurrentCase());
    }

    @Test
    public void testGetCaseRate() {
        double realCaseRateLosAngeles = 12986;
        double realCaseRateSantaMonica = 4930;
        assertEquals(realCaseRateLosAngeles, losAngeles.getCaseRate(), 1);
        assertEquals(realCaseRateSantaMonica, santaMonica.getCaseRate(), 1);
    }

    @Test
    public void testGetNewCaseRate() {
        double realNewCaseRateLosAngeles = 264;
        double realNewCaseRateSantaMonica = 62;
        assertEquals(realNewCaseRateLosAngeles, losAngeles.getNewCaseRate(), 1);
        assertEquals(realNewCaseRateSantaMonica, santaMonica.getNewCaseRate(), 1);
    }

    @Test
    public void testGetDistanceBetweenCityCenter() {
        // Calculate by https://www.nhc.noaa.gov/gccalc.shtml in km
        double realDistanceBetweenLosAngelesSantaMonica = 23;
        assertEquals(realDistanceBetweenLosAngelesSantaMonica, losAngeles.getDistanceBetweenCityCenter(santaMonica), 1);
    }
}