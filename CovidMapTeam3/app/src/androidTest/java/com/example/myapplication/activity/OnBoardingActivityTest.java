package com.example.myapplication.activity;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.example.myapplication.R;
import com.example.myapplication.activity.OnBoardingActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class OnBoardingActivityTest {


    @Rule
    public ActivityTestRule<OnBoardingActivity> mActivityTestRule = new ActivityTestRule<>(OnBoardingActivity.class);

    @Test
    public void checkVisibility() {
        // find start button
        ViewInteraction startBtnViewInteraction = onView(ViewMatchers.withId(R.id.start_btn));
        // check button visibility
        startBtnViewInteraction.check(matches(not(isDisplayed())));
        // swipe left
        onView(withId(R.id.view_page)).perform(swipeLeft());
        // check button visibility
        startBtnViewInteraction.check(matches(not(isDisplayed())));
        // swipe left
        onView(withId(R.id.view_page)).perform(swipeLeft());
        // check button visibility
        startBtnViewInteraction.check(matches(isDisplayed()));
        // swipe right
        onView(withId(R.id.view_page)).perform(swipeRight());
        // check button visibility
        startBtnViewInteraction.check(matches(not(isDisplayed())));
    }


}
