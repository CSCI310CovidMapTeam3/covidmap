package com.example.myapplication.DataBase;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

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
        }
        return ts;
    }

    public TestCenterDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
            values.put("long",address);
            db.insert(DATABASE_NAME,null,values);
            return true;
        }catch(Exception e) {
            Log.v(TAG, String.valueOf(e));
            return false;
        }
    }

    public String getTestCenter(){
        SQLiteDatabase db = ts.getReadableDatabase();
        Cursor c = db.query(DATABASE_NAME,null,null,null,null,null,null);
        while (c.moveToNext()) {
            TestCenter temp = new TestCenter(c.getString(c.getColumnIndex("name")),
                    c.getDouble(c.getColumnIndex("lat")),
                    c.getDouble(c.getColumnIndex("long")),
                    c.getString(c.getColumnIndex("address")));
            Log.v(TAG, temp.toString());
        }
        return "";
    }

    public void clear(){
        SQLiteDatabase db = ts.getWritableDatabase();
        db.execSQL("DELETE FROM " + DATABASE_NAME);
    }

    public void initTestCenter(){
        this.addTestCenter("Edendale Library - Echo Park", 34.07873,-118.2642767, "2044 Reservoir St, Los Angeles");
        this.addTestCenter("LA Union Station", 34.056224,-118.2386961, "800 N. Alameda St., Los Angeles");
        this.addTestCenter("Consulate General of Mexico", 34.0617154,-118.2800256, "2401 W 6th St., Los Angeles");
        this.addTestCenter("Angeles Community Health Center - Los Angeles", 34.0559926,-118.2772331, "1919 W 7th Street 1st Floor, Los Angeles");
        this.addTestCenter("LA Union Station", 34.056224,-118.2386961, "800 N. Alameda St., Los Angeles");
        this.addTestCenter("LA Union Station", 34.056224,-118.2386961, "800 N. Alameda St., Los Angeles");
        this.addTestCenter("LA Union Station", 34.056224,-118.2386961, "800 N. Alameda St., Los Angeles");
    }

}