package com.example.myapplication.DataBase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class TestCenterTest {
    private static final String TAG = "TestCenterTest";
    private double testDoubleDelta ;

    @Before
    public void setUp() throws Exception {
        testDoubleDelta = 0.001;
    }

    // White Box Test Case No. 11
    @Test
    public void testTestCenterInitialization() {
        String name = "testCenter";
        String address = "testCenter Address";
        double lat1 = -1.23123;
        double lon1 = -2.12314;

        TestCenter testCenter1 = new TestCenter(name,lat1,lon1,address);
        assertEquals(testCenter1.getName(), name);
        assertEquals(testCenter1.getAddress(), address);
        assertEquals(testCenter1.getLat(),lat1, testDoubleDelta );
        assertEquals(testCenter1.getLon(),lon1, testDoubleDelta);
    }

}
