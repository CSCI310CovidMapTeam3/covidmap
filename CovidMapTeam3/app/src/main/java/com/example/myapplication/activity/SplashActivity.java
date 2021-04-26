package com.example.myapplication.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences setPreferences = getSharedPreferences("app", Context.MODE_PRIVATE);
        if (setPreferences.getBoolean("isFirst", true)) {
            Intent i = new Intent(SplashActivity.this, OnBoardingActivity.class);
            startActivity(i);
            finish();
            return;
        }
        getWindow().setBackgroundDrawableResource(R.drawable.splash_background);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // This method will be executed once the timer is over
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, 3 * 1000);
    }

}
