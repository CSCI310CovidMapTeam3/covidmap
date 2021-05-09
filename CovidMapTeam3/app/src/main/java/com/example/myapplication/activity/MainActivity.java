package com.example.myapplication.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.DailyNotification.NotificationReceiver;
import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dailyNotification();
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_news, R.id.navigation_test, R.id.navigation_tracking, R.id.navigation_setting)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);


        /*Button button_back_to_LA =  findViewById(R.id.my_location);
        if(button_back_to_LA != null) {
            button_back_to_LA.setVisibility(View.INVISIBLE);
        }*/

        // https://medium.com/@yonatanvlevin/the-minimum-interval-for-periodicwork-is-15-minutes-same-as-jobscheduler-periodic-job-eb2d63716d1f Interval
        PeriodicWorkRequest saveRequest =
                new PeriodicWorkRequest.Builder(Recorder.class, 15, TimeUnit.MINUTES).addTag("Periodic")
                        // Constraints
                        .build();

        WorkManager.getInstance(getApplicationContext()).enqueue(saveRequest);

        logToFile();


    }

    public void dailyNotification(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTime().compareTo(new Date()) < 0)
            Log.v(TAG, " time has past");
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            Log.v(TAG, " alarmManager-setRepeating");
        }
    }

    public interface OnBackListener {
        // return True if the listener has consumed the event, false otherwise.
        boolean onBackListener();
    }

    OnBackListener mOnBackListener;

    // set webView onBack
    public void setOnBackListener(OnBackListener onBackListener) {
        mOnBackListener = onBackListener;
    }

    @Override
    public void onBackPressed() {
        if (mOnBackListener == null || !mOnBackListener.onBackListener()) {
            super.onBackPressed();
        }
    }

    private void logToFile(){
        if ( isExternalStorageWritable() ) {

            File appDirectory = new File( Environment.getExternalStorageDirectory() + "/MyPersonalAppFolder" );
            File logDirectory = new File( appDirectory + "/logs" );
            File logFile = new File( logDirectory, "logcat_" + System.currentTimeMillis() + ".txt" );

            // create app folder
            if ( !appDirectory.exists() ) {
                appDirectory.mkdir();
            }

            // create log folder
            if ( !logDirectory.exists() ) {
                logDirectory.mkdir();
            }

            // clear the previous logcat and then write the new one to the file
            try {
                Process process = Runtime.getRuntime().exec("logcat -c");
                Runtime.getRuntime().exec("logcat -f" + " /sdcard/Logcat.txt");
            } catch ( IOException e ) {
                e.printStackTrace();
            }

            Log.d("MainActivity", "Readable & Writable");

            appendLog("My Log");

            try {
                writeToFile("My Log");
            } catch (IOException e){
                e.printStackTrace();
            }
        } else if ( isExternalStorageReadable() ) {
            // only readable
            Log.d("MainActivity", "Readable");
        } else {
            // not accessible
            Log.d("MainActivity", "not accessible");
        }


    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
            return true;
        }
        return false;
    }

    public void appendLog(String text)
    {
        File path = getApplicationContext().getExternalFilesDir(null);
        Date date = new Date();
        String dateString = new SimpleDateFormat("-MM-dd-yyyy", Locale.US).format(date);
        String filename = "log" + dateString + ".txt";

        File file = new File(path, filename);
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
            buf.append("Application Start on ");
            buf.append(new Date().toString());
            buf.newLine();
            buf.flush();
            buf.close();


        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void writeToFile(String text) throws IOException {
        File path = getApplicationContext().getExternalFilesDir(null);
        File file = new File(path, "my-file-name.txt");
        FileOutputStream stream = new FileOutputStream(file);
        try {
            stream.write("text-to-write".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stream.close();
            Log.d("MainActivity", "finished writing");
        }
    }
}