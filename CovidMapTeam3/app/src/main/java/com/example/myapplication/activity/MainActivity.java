package com.example.myapplication.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

}