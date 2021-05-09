package com.example.myapplication.DailyNotification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioAttributes;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.work.ListenableWorker;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.DataBase.HistoryDBHelper;
import com.example.myapplication.DataBase.WebSpider;
import com.example.myapplication.R;
import com.example.myapplication.ui.home.HomeFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class NotificationHelper {

    private Context mContext;
    private static final String NOTIFICATION_CHANNEL_ID = "10001";
    private static final String NOTIFICATION_CHANNEL_ID_CUSTOMIZED_SOUND = "10002";

    private static final String TAG = "NotificationHelper";
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLocation;
    private LocationCallback mLocationCallback;
    private String mCity = "Los Angeles County";
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private String notificationContent = "";
    private boolean useCustomizeSound = false;
    private Uri sound;

    public static final Map<Integer,String> CITY_CODE_TO_NAME;
    static{
        Hashtable<Integer,String> tmp =
                new Hashtable<Integer,String>();
        tmp.put(1,"Santa Monica");
        tmp.put(2,"Culver City");
        tmp.put(3,"Beverly Hills");
        tmp.put(4,"West Hollywood");
        tmp.put(5,"Los Angeles City");
        CITY_CODE_TO_NAME = Collections.unmodifiableMap(tmp);
    }

    NotificationHelper(Context context) {
        mContext = context;
    }

    public void createNotification() {
        // This function would get location and send notification when location is ready
        // It is implemented this way because I must write the send notification function in mFusedLocationClient's onComplete callback
        getLocationAndSendNotification();
    }

    public void getLocationAndSendNotification(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
            }
        };

        try {
            mFusedLocationClient
                    .getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                                Log.d(TAG, "Location : " + mLocation);

                                int checkInLAResult = checkInLACounty(mLocation);

                                int[] newTotalCases = WebSpider.getTotalCases();



                                if (checkInLACounty(mLocation) >= 1){
                                    if (checkInLAResult <= 5){
                                        mCity = CITY_CODE_TO_NAME.get(checkInLAResult);
                                        if(mCity != null){
                                            notificationContent = mCity + ".Total cases is "+ newTotalCases[checkInLAResult-1];
                                            Log.d(TAG, "Notification Content"+notificationContent);
                                        } else{
                                            Log.e(TAG, "getLocationAndSendNotification() CITY_CODE_TO_NAME return null");
                                        }
                                    } else{
                                        notificationContent = "You are now in Los Angeles County Right Now. Total Cases Reported is 1,235,422";
                                        Log.d(TAG, "getLocationAndSendNotification() Notification Content"+notificationContent);
                                    }

                                } else{
                                    notificationContent = "You are now in Los Angeles County Right Now";
                                }

                                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                            } else {
                                Log.w(TAG, "Failed to get location.");
                                notificationContent = "Failed to get you location right now";
                            }

                            // It calls sendNotificationHelper
                            sendNotificationHelper();
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
            notificationContent = "Lost location permission";
        }
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
        return 0; // Location is Null
    }

    private void sendNotificationHelper(){
        // TODO: ADD Case Data in Notification Content
        Log.d(TAG, "Notification Content"+notificationContent);

        // Get sound preference from SharedPreferences
        getSoundPreference();
        Intent intent = new Intent(mContext, HomeFragment.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext,
                0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);

        mBuilder.setContentTitle("Your Daily Covid Notification")
                .setContentText(notificationContent)
                .setAutoCancel(false)
                .setContentIntent(resultPendingIntent);
        if (useCustomizeSound){
            mBuilder.setSound(sound);
        } else{
            mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        }


        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel;
            if (useCustomizeSound){
                notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_CUSTOMIZED_SOUND, "NOTIFICATION_CHANNEL_CUSTOMIZED_SOUND_NAME", importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

                // Set Channel Sound
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                notificationChannel.setSound(sound, audioAttributes);

                assert mNotificationManager != null;
                mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID_CUSTOMIZED_SOUND);

            } else{
                notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                assert mNotificationManager != null;
                mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            }
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(0 /* Request Code */, mBuilder.build());
    }

    private void getSoundPreference(){
        sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + BuildConfig.APPLICATION_ID + "/" + R.raw.fighton);

        SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(mContext);

        String currentSoundSetting = saved_values.getString("count", "default");
        Log.d("SoundSetting", "onCreateView: "+ currentSoundSetting);

        if (currentSoundSetting.equals("Fight On")) {
            useCustomizeSound = true;
        }
    }
}