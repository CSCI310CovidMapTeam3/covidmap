package com.example.myapplication.activity;

//import androidx.fragment.app.testing.launchFragmentInContainer;

import android.app.Instrumentation;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.example.myapplication.R;
import com.google.common.collect.Iterables;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.openLinkWithUri;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.ext.truth.content.IntentSubject.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mMainActivityTestRule = new ActivityTestRule<>(MainActivity.class);


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


    //check news feed button in the navigation bar
    @Test
    public void clickNewsFeed() {
        ViewInteraction newsFeedButton = onView(ViewMatchers.withId(R.id.navigation_news));
        newsFeedButton.perform(click());
        newsFeedButton.check(matches(isDisplayed()));
    }

    //check test location button in the navigation bar
    @Test
    public void clickTestLocation() {
        ViewInteraction testButton = onView(ViewMatchers.withId(R.id.navigation_test));
        testButton.perform(click());
        testButton.check(matches(isDisplayed()));
    }

    //check tracking button in the navigation bar
    @Test
    public void clickTracking() {
        ViewInteraction trackingButton = onView(ViewMatchers.withId(R.id.navigation_tracking));
        trackingButton.perform(click());
        trackingButton.check(matches(isDisplayed()));
    }

    //check setting button in the navigation bar
    @Test
    public void clickSetting() {
        ViewInteraction settingButton = onView(ViewMatchers.withId(R.id.navigation_setting));
        settingButton.perform(click());
        settingButton.check(matches(isDisplayed()));
    }

    @Test
    public void clickButtonToAbout() {
        // find setting bottom
        ViewInteraction settingBottom = onView(ViewMatchers.withId(R.id.navigation_setting));
        // click
        settingBottom.perform(click());
        // check about bottom
        ViewInteraction aboutBottom = onView(withId(R.id.about_btn));
        aboutBottom.check(matches(isDisplayed()));
        // click about bottom
        aboutBottom.perform(click());
        // check activity
        assertThat(Iterables.getOnlyElement(Intents.getIntents())).hasComponentClass(AboutActivity.class);
    }

    @Test
    public void checkPressBack() {
        // find setting button
        ViewInteraction settingBottom = onView(ViewMatchers.withId(R.id.navigation_setting));
        // click
        settingBottom.perform(click());
        // check about button
        onView(withId(R.id.about_btn)).check(matches(isDisplayed()));
        // click pressBack
        pressBack();
        // check map displayed
        onView(withId(R.id.map)).check(matches(isDisplayed()));
    }

    @Test
    public void checkSpinner() {
        // to setting
        onView(ViewMatchers.withId(R.id.navigation_setting)).perform(click());
        // get spinner text
        ViewInteraction spinnerTextView = onView(allOf(withId(android.R.id.text1), withParent(withId(R.id.spinner1))));
        // set select item
        String selectStr = "Beverly Hills";
        // get spinner select
        onData(allOf(is(instanceOf(String.class)), is(selectStr))).perform(click());
        // close spinner
        pressBack();
        // check spinner select
        spinnerTextView.check(matches(withText(selectStr)));
    }

    //test requirement 9 that "the user should be able to go to a setting page to view and manage privacy related setting"
    @Test
    public void checkClearAllData() {
        ViewInteraction settingButton = onView(ViewMatchers.withId(R.id.navigation_setting));
        settingButton.perform(click());
        settingButton.check(matches(isDisplayed()));

        ViewInteraction clearAllDataButton = onView(withId(R.id.clear_btn));
        clearAllDataButton.check(matches(isDisplayed()));

        //clearAllDataButton.perform(click());
        //onView(withText("Clear All Data")).inRoot(RootMatchers.isPlatformPopup()).check(matches(isDisplayed()));
        //clearAllDataButton.inRoot(RootMatchers.isPlatformPopup()).check(matches(isDisplayed()));
    }

    /*@Test
    public void checkWebPage() {
        ViewInteraction newsFeedButton = onView(ViewMatchers.withId(R.id.navigation_news));
        newsFeedButton.perform(click());
        newsFeedButton.check(matches(openLinkWithUri("https://twitter.com/dt_covid19")));

        Intents.init();
        Matcher<Intent> expected = allOf(IntentMatchers.hasAction(Intent.ACTION_VIEW), IntentMatchers.hasData("https://twitter.com/dt_covid19"));
        Intents.intending(expected).respondWith(new Instrumentation.ActivityResult(0, null));
        ViewInteraction newsFeedButton = onView(ViewMatchers.withId(R.id.navigation_news));
        newsFeedButton.perform(click());
        Intents.intended(expected);
        Intents.release();
    }*/

}