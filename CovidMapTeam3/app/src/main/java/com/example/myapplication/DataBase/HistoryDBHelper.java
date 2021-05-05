package com.example.myapplication.DataBase;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HistoryDBHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "HistoryDBHelper";

    //singleton
    private static HistoryDBHelper ts;

    private static final String TAG = "HistoryDBHelper";

    public static HistoryDBHelper getInstance(Context context) {
        if (ts == null) {
            ts = new HistoryDBHelper(context);
        }
        return ts;
    }

    public HistoryDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }


    /*
    Store the travel tracking data in an encrypted form to prevent privacy leak.
     */
    @Override
    public void onConfigure(SQLiteDatabase db){
        db.execSQL("PRAGMA key = 'secretkey'");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table HistoryDBHelper (" +
                "name TEXT," +
                "lat DOUBLE," +
                "long DOUBLE,"+
                "timestamp TIMESTAMP"+
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public boolean addHistoryItem(String name, double lat, double lon, Timestamp timestamp){
        try{
            SQLiteDatabase db = ts.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name",name);
            values.put("lat",lat);
            values.put("long",lon);
            values.put("timestamp",timestamp.toString());
            db.insert(DATABASE_NAME,null,values);
            return true;
        }catch(Exception e) {
            Log.v(TAG, String.valueOf(e));
            return false;
        }
    }

    public ArrayList<HistoryItem>  getAllListHistory(){
        Log.v(TAG, "Start method getListTestCenter");
        ArrayList<HistoryItem> HistoryItems = new ArrayList<HistoryItem>();
        SQLiteDatabase db = ts.getReadableDatabase();
        Cursor c = db.query(DATABASE_NAME,null,null,null,null,null,null);
        while (c.moveToNext()) {
            HistoryItem temp = new HistoryItem(c.getString(c.getColumnIndex("name")),
                    c.getDouble(c.getColumnIndex("lat")),
                    c.getDouble(c.getColumnIndex("long")),
                    Timestamp.valueOf( c.getString(c.getColumnIndex("timestamp")))
                   );
            Log.v(TAG, temp.toString());
            HistoryItems.add(temp);
        }
        return HistoryItems;
    }

    public void clear(){
        SQLiteDatabase db = ts.getWritableDatabase();
        //db.execSQL("DROP TABLE IF EXISTS '" + DATABASE_NAME + "'");
        db.execSQL("DELETE FROM " + DATABASE_NAME);
        //db.execSQL("DROP " + DATABASE_NAME);
    }

    public void initSimpleTravelHistory(){
        Log.v(TAG, "Start method initSimpleTravelHistory");
        // My Saturday Trip
        addHistoryItem("Los Angeles (Tommy's)", 34.0695, -118.2763, Timestamp.valueOf("2021-04-24 01:10:10.345"));
        addHistoryItem("Los Angeles (Village)", 34.0255, -118.2849, Timestamp.valueOf("2021-04-24 08:10:12.345"));
        addHistoryItem("Santa Monica (Starbucks)", 34.0251, -118.4592, Timestamp.valueOf("2021-04-24 08:30:12.345"));
        addHistoryItem("Santa Monica (SMC)", 34.0171, -118.47, Timestamp.valueOf("2021-04-24 09:00:27.627"));
        addHistoryItem("Los Angeles (K-Town)", 34.0620, -118.3026, Timestamp.valueOf("2021-04-24 18:00:27.627"));
        addHistoryItem("Los Angeles (Figueroa)", 34.0249, -118.2787, Timestamp.valueOf("2021-04-24 19:00:27.627"));
        addHistoryItem("Los Angeles (Lorenzo)", 34.0289, -118.2729, Timestamp.valueOf("2021-04-24 20:00:27.627"));
        addHistoryItem("Los Angeles (Parkside)", 34.0289, -118.2729, Timestamp.valueOf("2021-04-24 21:00:27.627"));

        // My Sunday Trip
        addHistoryItem("Los Angeles (Village)", 34.0255, -118.2849, Timestamp.valueOf("2021-04-25 13:10:12.345"));
        addHistoryItem("Los Angeles (Universal Studio)", 34.1370, -118.3518, Timestamp.valueOf("2021-04-25 14:10:12.345"));
        addHistoryItem("Los Angeles (Pantages Theater)", 34.1023, -118.3256, Timestamp.valueOf("2021-04-25 17:10:12.345"));
        addHistoryItem("Los Angeles (Santa Monica Beach)", 34.0132, -118.5017, Timestamp.valueOf("2021-04-25 21:10:12.345"));
        addHistoryItem("Los Angeles (K-town)", 34.0631, -118.2967, Timestamp.valueOf("2021-04-25 23:10:12.345"));
        addHistoryItem("Los Angeles (Village)", 34.0255, -118.2849, Timestamp.valueOf("2021-04-25 23:40:12.345"));

        // My Coliseum Trip
        addHistoryItem("Los Angeles (Coliseum)", 34.0143, -118.2878, new Timestamp(System.currentTimeMillis()));
    }

    /*
     retrieve by DATE which belongs to java.util public class Date
     to init custom simply call new Date() to get current date or new Date
     for custom use
     public Date(int year,
            int month,
            int date)
     */

    public ArrayList<HistoryItem> retrieveByDate(Date day){
        Log.v(TAG, "Start method retrieveByDate");
        ArrayList<HistoryItem> HistoryItems = new ArrayList<HistoryItem>();
        SQLiteDatabase db = ts.getReadableDatabase();
        String dateFormat = new SimpleDateFormat("yyyy-MM-dd").format(day);

        Log.v(TAG, "day: "+ dateFormat);
        Cursor c = db.query(DATABASE_NAME,null,"DATE(timestamp) = "+ "'" + dateFormat + "'", null,null,null,null);
        while (c.moveToNext()) {
            HistoryItem temp = new HistoryItem(c.getString(c.getColumnIndex("name")),
                    c.getDouble(c.getColumnIndex("lat")),
                    c.getDouble(c.getColumnIndex("long")),
                    Timestamp.valueOf( c.getString(c.getColumnIndex("timestamp")))
            );
            Log.v(TAG, temp.toString());
            HistoryItems.add(temp);
        }
        return HistoryItems;
    }

    public boolean deleteByDate(Date day){
        try{
            SQLiteDatabase db = ts.getWritableDatabase();
            String dateFormat = new SimpleDateFormat("yyyy-MM-dd").format(day);
            db.execSQL("DELETE FROM " + DATABASE_NAME +" WHERE DATE(timestamp) ="+ "'" + dateFormat + "'");

            return true;
        }catch (Exception error)
        {
            Log.v(TAG, error.toString());
            return false;
        }
    }

    public boolean deleteBeforeDate(Date day){
        try{
            SQLiteDatabase db = ts.getWritableDatabase();
            String dateFormat = new SimpleDateFormat("yyyy-MM-dd").format(day);
            db.execSQL("DELETE FROM " + DATABASE_NAME +" WHERE DATE(timestamp) <="+ "'" + dateFormat + "'");

            return true;
        }catch (Exception error)
        {
            Log.v(TAG, error.toString());
            return false;
        }
    }

}