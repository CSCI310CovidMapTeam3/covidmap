package com.example.myapplication.ui.home;

import android.location.Location;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

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

    // White Box Test Case No. 6
    @Test
    public void testGetMarkerColor() {
        float realMarkerColorLosAngeles = (float) BitmapDescriptorFactory.HUE_RED;
        float realMarkerColorSantaMonica = (float) BitmapDescriptorFactory.HUE_YELLOW;
        assertEquals(realMarkerColorLosAngeles, losAngeles.getMarkerColor(), 1);
        assertEquals(realMarkerColorSantaMonica, santaMonica.getMarkerColor(), 1);
        losAngeles.setPopulation(0);
        assertEquals(BitmapDescriptorFactory.HUE_BLUE, losAngeles.getMarkerColor(), 1);
        losAngeles.setPopulation(3792621);
    }

    // White Box Test Case No. 7
    @Test
    public void testGetCurrentCase() {
        int realCurrentCaseLosAngeles = 98504; // We calculated by calculator on Phone
        assertEquals(realCurrentCaseLosAngeles, losAngeles.getCurrentCase());
        int realCurrentCaseSantaMonica = 903; // We calculated by calculator on Phone
        assertEquals(realCurrentCaseSantaMonica, santaMonica.getCurrentCase());
        City dummy = new City("Dummy");
        assertEquals(0, dummy.getCurrentCase());
    }

    // White Box Test Case No. 8
    @Test
    public void testGetCaseRate() {
        double realCaseRateLosAngeles = 12986;
        double realCaseRateSantaMonica = 4930;
        assertEquals(realCaseRateLosAngeles, losAngeles.getCaseRate(), 1);
        assertEquals(realCaseRateSantaMonica, santaMonica.getCaseRate(), 1);
        losAngeles.setPopulation(0);
        boolean illegalStateExceptionThrown = false;
        boolean otherExceptionThrown = false;
        try{
            losAngeles.getCaseRate();
        } catch (IllegalStateException e){
            illegalStateExceptionThrown = true;
        } catch (Exception e){
            otherExceptionThrown = true;
        }
        assertTrue(illegalStateExceptionThrown);
        assertFalse(otherExceptionThrown);
        losAngeles.setPopulation(3792621);
    }

    // White Box Test Case No. 9
    @Test
    public void testGetNewCaseRate() {
        double realNewCaseRateLosAngeles = 264;
        double realNewCaseRateSantaMonica = 62;
        assertEquals(realNewCaseRateLosAngeles, losAngeles.getNewCaseRate(), 1);
        assertEquals(realNewCaseRateSantaMonica, santaMonica.getNewCaseRate(), 1);
        losAngeles.setPopulation(0);
        boolean illegalStateExceptionThrown = false;
        boolean otherExceptionThrown = false;
        try{
            losAngeles.getNewCaseRate();
        } catch (IllegalStateException e){
            illegalStateExceptionThrown = true;
        } catch (Exception e){
            otherExceptionThrown = true;
        }
        assertTrue(illegalStateExceptionThrown);
        assertFalse(otherExceptionThrown);
        losAngeles.setPopulation(3792621);
    }

    // White Box Test Case No. 10
    @Test
    public void testGetDistanceBetweenCityCenterKM() {
        // Calculate by https://www.nhc.noaa.gov/gccalc.shtml in km
        double realDistanceBetweenLosAngelesSantaMonicaKM = 23;
        assertEquals(realDistanceBetweenLosAngelesSantaMonicaKM, losAngeles.getDistanceBetweenCityCenterKM(santaMonica), 1);
        assertEquals(0, losAngeles.getDistanceBetweenCityCenterKM(losAngeles), 1);

        City dummy = new City("Dummy");
        assertEquals(0, dummy.getCurrentCase());

        boolean illegalStateExceptionThrown = false;
        boolean otherExceptionThrown = false;
        try{
            losAngeles.getDistanceBetweenCityCenterKM(dummy);
        } catch (IllegalStateException e){
            illegalStateExceptionThrown = true;
        } catch (Exception e){
            otherExceptionThrown = true;
        }
        assertTrue(illegalStateExceptionThrown);
        assertFalse(otherExceptionThrown);
    }
}