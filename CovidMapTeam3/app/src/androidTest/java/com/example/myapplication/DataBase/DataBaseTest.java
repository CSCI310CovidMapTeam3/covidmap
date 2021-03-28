package com.example.myapplication.DataBase;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class DataBaseTest {
    private static final String TAG = "TestCenterTest";
    private static final String DATABASE_NAME = "testCenter";
    private static double testDoubleDelta = 0.001 ;

    Context instrumentationContext;

    @Before
    public void setUp() throws Exception {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.myapplication", instrumentationContext.getPackageName());
        TestCenterDBHelper instanceOne = TestCenterDBHelper.getInstance(instrumentationContext);
        instanceOne.clear();
    }

    @Test
    public void testDBInitialization() {
        TestCenterDBHelper instanceOne = TestCenterDBHelper.getInstance(instrumentationContext);
        assertNotNull(instanceOne);
        Log.v(TAG, instanceOne.getClass().getSimpleName());
        assertEquals("TestCenterDBHelper", instanceOne.getClass().getSimpleName());
    }

    @Test
    public void testDBAdd(){
        TestCenterDBHelper instanceOne = TestCenterDBHelper.getInstance(instrumentationContext);
        TestCenter temp1 = new TestCenter("test1", -2.4123412,63.4234, "temp address ave");
        instanceOne.addTestCenter(temp1.getName(), temp1.getLat(), temp1.getLon(), temp1.getAddress());
        SQLiteDatabase db = instanceOne.getReadableDatabase();
        String sql = "SELECT * FROM " +DATABASE_NAME.trim();
        Cursor cursor = db.rawQuery(sql,null);
        assertTrue(cursor.moveToNext());
        if(cursor.moveToNext()){
            assertEquals(cursor.getString(cursor.getColumnIndex("name")), temp1.getName());
            assertEquals(cursor.getDouble(cursor.getColumnIndex("lat")),temp1.getLat(), testDoubleDelta);
            assertEquals(cursor.getDouble(cursor.getColumnIndex("long")),temp1.getLon(), testDoubleDelta);
            assertEquals(cursor.getString(cursor.getColumnIndex("address")), temp1.getAddress());

        }
    }

    @Test
    public void testDBClear(){
        TestCenterDBHelper instanceOne = TestCenterDBHelper.getInstance(instrumentationContext);
        instanceOne.clear();
        SQLiteDatabase db = instanceOne.getReadableDatabase();
        String sql = "SELECT * FROM " +DATABASE_NAME.trim();
        Cursor cursor = db.rawQuery(sql,null);
        assertFalse(cursor.moveToNext());
    }

    @Test
    public void testDBGet(){
        TestCenter temp1 = new TestCenter("test1", -2.4123412,63.4234, "temp address ave");
        TestCenter temp2 = new TestCenter("test2", -3.42,12.3, "temp address ave2");
        TestCenterDBHelper instanceOne = TestCenterDBHelper.getInstance(instrumentationContext);
        instanceOne.clear();
        instanceOne.addTestCenter(temp1.getName(), temp1.getLat(), temp1.getLon(), temp1.getAddress());
        instanceOne.addTestCenter(temp2.getName(), temp2.getLat(), temp2.getLon(), temp2.getAddress());
        ArrayList<TestCenter> result = instanceOne.getListTestCenter();
        ArrayList<TestCenter> tempList = new ArrayList<TestCenter>();
        tempList.add(temp1);
        tempList.add(temp2);

        assertEquals(result.size(),tempList.size());
        assertEquals(result.get(0).getName(),tempList.get(0).getName());
        assertEquals(result.get(0).getLon(),tempList.get(0).getLon(), testDoubleDelta);
        assertEquals(result.get(0).getLat(),tempList.get(0).getLat(), testDoubleDelta);
        assertEquals(result.get(0).getAddress(),tempList.get(0).getAddress());
    }


}
