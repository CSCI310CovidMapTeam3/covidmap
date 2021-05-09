package com.example.myapplication.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myapplication.DataBase.HistoryDBHelper;
import com.example.myapplication.DataBase.TestCenter;
import com.example.myapplication.DataBase.TestCenterDBHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class Recorder extends Worker {

    private static final String TAG = "Recorder";
    private FusedLocationProviderClient mFusedLocationClient;
    private Context mContext;
    private Location mLocation;
    private LocationCallback mLocationCallback;
    private Geocoder mGeocoder;
    private String mCity = "Los Angeles County";
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    public static final Map<Integer,String> MYHASH;
    static{
        Hashtable<Integer,String> tmp =
                new Hashtable<Integer,String>();
        tmp.put(1,"Santa Monica");
        tmp.put(2,"Culver City");
        tmp.put(3,"Beverly Hills");
        tmp.put(4,"West Hollywood");
        tmp.put(5,"Los Angeles City");
        MYHASH = Collections.unmodifiableMap(tmp);
    }

    public Recorder(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: ");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
            }
        };

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try {
            mFusedLocationClient
                    .getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                                Log.d(TAG, "Location : " + mLocation);


                                mGeocoder = new Geocoder(mContext);

                                try {
                                    List<Address> addresses = mGeocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
                                    if (addresses.get(0) != null){
                                        if(addresses.get(0).getLocality() != null){
                                            mCity = addresses.get(0).getLocality();
                                            Log.d(TAG, "Geocoder: City Changed to . " + mCity);
                                        }
                                        else{
                                            Log.d(TAG, "Geocoder: Locality Null. " + mCity);
                                        }
                                    } else{
                                        Log.d(TAG, "Geocoder: Address Null. " + mCity);
                                    }
                                } catch (IOException e) {
                                    Log.e(TAG, "Geocoder: Exception during Geocoder. " + e);
                                    e.printStackTrace();
                                }
                                Log.d(TAG, "Geocoder: Current City to . " + mCity);


                                // Record
                                HistoryDBHelper inst = HistoryDBHelper.getInstance(mContext);
                                Date date = new Date();
                                System.out.println(new Timestamp(date.getTime()));
                                int checkInLAResult = checkInLACounty(mLocation);
                                if (checkInLAResult >= 1){
                                    if (checkInLAResult <= 5){
                                        inst.addHistoryItem(MYHASH.get(checkInLAResult), mLocation.getLatitude(), mLocation.getLongitude(), new Timestamp(date.getTime()));
                                        Log.d(TAG, "Dummy Geocoder: In LA "+ MYHASH.get(checkInLAResult));
                                    } else{
                                        inst.addHistoryItem(mCity, mLocation.getLatitude(), mLocation.getLongitude(), new Timestamp(date.getTime()));
                                    }

                                } else{
                                    Log.d(TAG, "Geocoder: Not in LA, not Recorded " + mCity);
                                }

                                System.out.println(MYHASH.get(1));

                                // new Timestamp(today.getTime());
                                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
            return Result.failure();
        }

        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, null);
        } catch (SecurityException unlikely) {
            //Utils.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
            return Result.failure();
        } catch (Exception e) {
            Log.e(TAG, "Exception. " + e);
            return Result.failure();
        }


        return Result.success();
    }

    private int checkInLACounty(Location location){
        if (location != null) {
            if (location.getLatitude() > 33.5 &&  location.getLatitude() < 34.8){
                if (location.getLongitude() > -118.9 && location.getLongitude() < -117.6){
                    Log.d(TAG, "checkInLACounty: In LA County");
                    // Start Dummy and Hardcoded Geocoder for our real service area
                    if (location.getLatitude() > 33.99 && location.getLatitude() < 34.10 && location.getLongitude() > -118.52 && location.getLongitude() < -118.20){
                        if (location.getLongitude() < -118.43) {
                            return 1; // Santa Monica
                        } else if (location.getLongitude() < -118.39){
                            if (location.getLatitude() > 34.06){
                                return 3; // Beverly Hills
                            } else {
                                return 2; // Culver City
                            }
                        } else if (location.getLongitude() < -118.33){
                            if (location.getLatitude() > 34.06){
                                return 4; // West Hollywood
                            } else {
                                return 5; // Los Angeles City Downtown
                            }
                        } else {
                            return 5; // Los Angeles City Downtown
                        }
                    } else {
                        return 6; // In LA county but not in our service area
                    }
                }
            }
            Log.d(TAG, "checkInLACounty: Not In LA");
            return -1;
        }
        Log.d(TAG, "checkInLACounty: Null Location");
        return 0; // Location is Null Default to True
    }
}
