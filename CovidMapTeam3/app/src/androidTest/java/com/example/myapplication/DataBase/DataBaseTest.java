package com.example.myapplication.DataBase;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import android.content.Context;
import android.util.Log;

import andriod.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

public class DataBaseTest {
    private static final String TAG = "TestCenterTest";
    Context instrumentationContext;

    @Before
    public void setUp() throws Exception {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.myapplication", instrumentationContext.getPackageName());
    }

    @Test
    public void testDBInitialization() {
        TestCenterDBHelper instanceOne = TestCenterDBHelper.getInstance(instrumentationContext);
        assertNotNull(instanceOne);
        Log.v(TAG, instanceOne.getClass().getSimpleName());
        assertEquals("TestCenterDBHelper", instanceOne.getClass().getSimpleName());
    }

    @Test
    public void testAdd(){

    }

    @Test
    public void testClear(){

    }

    @Test
    public void testGet(){

    }


}
