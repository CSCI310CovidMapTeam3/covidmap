package com.example.myapplication.activity;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.test.espresso.intent.Intents;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.google.common.collect.Iterables;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.ext.truth.content.IntentSubject.assertThat;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SplashActivityTest {

    @Rule
    public ActivityTestRule<SplashActivity> mActivityTestRule = new ActivityTestRule<>(SplashActivity.class);

    @Before
    public void intentsInit() {
        // initialize Espresso Intents capturing
        Intents.init();
    }

    @After
    public void intentsTeardown() {
        // release Espresso Intents capturing
        Intents.release();
    }

    @Test
    public void splashActivityTest() {
        SharedPreferences setPreferences = mActivityTestRule.getActivity()
                .getSharedPreferences("app", Context.MODE_PRIVATE);
        // check activity
        if (setPreferences.getBoolean("isFirst", true)) {
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                    assertThat(Iterables.getOnlyElement(Intents.getIntents()))
                            .hasComponentClass(OnBoardingActivity.class),1000);
        } else {
            Intents.assertNoUnverifiedIntents();
        }
    }
}
