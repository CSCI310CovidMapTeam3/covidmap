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
                "name text," +
                "lat double," +
                "long DOUBLE"+
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public boolean addTestCenter(String name, double lat, double lon){
        try{
            SQLiteDatabase db = ts.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name",name);
            values.put("lat",lat);
            values.put("long",lon);
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
                    c.getDouble(c.getColumnIndex("long")));
            Log.v(TAG, temp.toString());
        }
        return "";
    }

    public void clear(){
        SQLiteDatabase db = ts.getWritableDatabase();
        db.execSQL("DELETE FROM " + DATABASE_NAME);
    }

}