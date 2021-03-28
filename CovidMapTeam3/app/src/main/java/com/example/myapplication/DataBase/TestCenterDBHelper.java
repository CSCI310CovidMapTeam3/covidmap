package com.example.myapplication.DataBase;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

public class TestCenterDBHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "testCenter";

    //singleton
    private static TestCenterDBHelper ts;

    private static final String TAG = "TestCenterDBHelper";

    public static TestCenterDBHelper getInstance(Context context) {
        if (ts == null) {
            ts = new TestCenterDBHelper(context);
            ts.initTestCenter();
        }
        return ts;
    }

    public TestCenterDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        context.deleteDatabase(DATABASE_NAME);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table testCenter (" +
                "name TEXT," +
                "lat DOUBLE," +
                "long DOUBLE,"+
                "address TEXT"+
                ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public boolean addTestCenter(String name, double lat, double lon, String address){
        try{
            SQLiteDatabase db = ts.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name",name);
            values.put("lat",lat);
            values.put("long",lon);
            values.put("address",address);
            db.insert(DATABASE_NAME,null,values);
            return true;
        }catch(Exception e) {
            Log.v(TAG, String.valueOf(e));
            return false;
        }
    }

    public ArrayList<TestCenter>  getListTestCenter(){
        Log.v(TAG, "Start method getListTestCenter");
        ArrayList<TestCenter> testCenters = new ArrayList<TestCenter>();
        SQLiteDatabase db = ts.getReadableDatabase();
        Cursor c = db.query(DATABASE_NAME,null,null,null,null,null,null);
        while (c.moveToNext()) {
            TestCenter temp = new TestCenter(c.getString(c.getColumnIndex("name")),
                    c.getDouble(c.getColumnIndex("lat")),
                    c.getDouble(c.getColumnIndex("long")),
                    c.getString(c.getColumnIndex("address")));
            Log.v(TAG, temp.toString());
            testCenters.add(temp);
        }
        return testCenters;
    }

    public void clear(){
        SQLiteDatabase db = ts.getWritableDatabase();
        //db.execSQL("DROP TABLE IF EXISTS '" + DATABASE_NAME + "'");
        db.execSQL("DELETE FROM " + DATABASE_NAME);
        //db.execSQL("DROP " + DATABASE_NAME);
    }

    public void initTestCenter(){
        Log.v(TAG, "Start method initTestCenter");
        this.addTestCenter("Edendale Library - Echo Park", 34.07873,-118.2642767, "2044 Reservoir St, Los Angeles");
        this.addTestCenter("LA Union Station", 34.056224,-118.2386961, "800 N. Alameda St., Los Angeles");
        this.addTestCenter("Consulate General of Mexico", 34.0617154,-118.2800256, "2401 W 6th St., Los Angeles");
        this.addTestCenter("Angeles Community Health Center - Los Angeles", 34.0559926,-118.2772331, "1919 W 7th Street 1st Floor, Los Angeles");
        this.addTestCenter("Center for Community Health - JWCH Institute, Inc. Medical Clinic", 34.0432489,-118.2459655, "522 South San Pedro St\n" +
                "Los Angeles");
        this.addTestCenter("Lincoln Park", 34.0654262,-118.2080912, "3501 Valley Blvd.\n" +
                "Los Angeles");
        this.addTestCenter("Exposition Park\n", 34.0115444,-118.287104, "3986 South Hoover Street\n" +
                "Los Angeles\n");
        this.addTestCenter("ChapCare Pasadena\n", 34.1733782,-118.1346178, "1595 N. Lake Avenue\n" +
                "Pasadena\n");
        this.addTestCenter("DPH – Monrovia Health Center\n",34.1412157 ,-118.0082174, "330 W Maple Avenue\n" +
                "Monrovia\n");
        //this.addTestCenter("", ,-, "");
    }

}