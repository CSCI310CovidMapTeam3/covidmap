package com.example.myapplication.ui.home;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.myapplication.activity.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.LocationServices;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class HomeFragmentTest {

    MainActivity mainActivity;
    HomeFragment homeFragment;
    private boolean locationPermissionGranted;
    private SharedViewModel sharedViewModel;
    private FusedLocationProviderClient fusedLocationProviderClient;

    String observedValue = "";
    @Before
    public void setUp() {

        try(ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                Assert.assertNotNull(activity);
                mainActivity = activity;
            });
            scenario.moveToState(Lifecycle.State.CREATED);
        }

        homeFragment = new HomeFragment();
        // startFragment(homeFragment);
        FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(homeFragment, "FRAGMENT_TAG");
        if (!fragmentManager.isDestroyed())
            fragmentTransaction.commit();

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mainActivity);
    }

    // Dummy Test Case on the execution of the start of testing function
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.myapplication", appContext.getPackageName());
    }

    // Dummy White Box Test Case on the successful start of HomeFragment
    @Test
    public void testHomeFragment() {
        assertTrue(homeFragment.testDummyFunction());
    }

    // White Box Test Case No. 1
    @Test
    public void testHomeFragmentRequestPermission() {
        locationPermissionGranted = false;
        assertFalse(getLocationPermission());
        locationPermissionGranted = true;
        assertTrue(getLocationPermission());
    }

    // White Box Test Case No. 2
    @Test
    public void testHomeFragmentGetDeviceLocation() {
        locationPermissionGranted = false;
        assertFalse(getDeviceLocation());
        locationPermissionGranted = true;
        assertTrue(getDeviceLocation());
    }

    // White Box Test Case No. 3
    @Test
    public void testHomeFragmentLastKnownLocation() {
        locationPermissionGranted = true;
        getDeviceLocation();

        homeFragment.setLastKnownLocation(null);
        assertNull(homeFragment.getLastKnownLocation());

        Location loc = new Location("");
        loc.setLatitude(34.0522);
        loc.setLongitude(-118.2437);

        homeFragment.setLastKnownLocation(loc);
        assertEquals(34.0522, homeFragment.getLastKnownLocation().getLatitude(), 0.01);
        assertEquals(-118.2437, homeFragment.getLastKnownLocation().getLongitude(), 0.01);

        loc.setLatitude(-33.5066);
        loc.setLongitude(150.9831);

        homeFragment.setLastKnownLocation(loc);
        assertEquals(-33.5066, homeFragment.getLastKnownLocation().getLatitude(), 0.01);
        assertEquals(150.9831, homeFragment.getLastKnownLocation().getLongitude(), 0.01);
    }

    // White Box Test Case No. 4
    @Test
    public void testHomeFragmentChangeDefaultLocation() {
        homeFragment.setDefaultCity("Los Angeles City");
        assertEquals("Los Angeles City", homeFragment.getDefaultCity());
        homeFragment.setDefaultCity("");
        assertEquals("", homeFragment.getDefaultCity());
    }

    // White Box Test Case No. 5
    @Test
    public void SharedViewModelBasicGetSetData() {
        sharedViewModel = ViewModelProviders.of(mainActivity).get(SharedViewModel.class);
        sharedViewModel.setNameDataBackground("Los Angeles City");

        final Lock lock = new ReentrantLock();
        final Condition goAhead = lock.newCondition();
        /* Here goes everything you need to do before "pausing" */
        lock.lock();
        // The reason why we need to pause the clock is because we need to wait for the main thread
        try {
            /**
             * Set whatever time limit you want/need
             * You can also use notifiers like goAhead.signal(), from within another thread
             */
            goAhead.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            assertEquals("Los Angeles City", sharedViewModel.getNameDataBackground());
        } finally {
            lock.unlock();
        }

        assertEquals("Los Angeles City", sharedViewModel.getNameDataBackground());
    }

    private boolean getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(mainActivity.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && locationPermissionGranted) {
            return true;
        } else {
            ActivityCompat.requestPermissions(mainActivity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return false;
        }
    }

    private boolean getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(mainActivity, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            homeFragment.setLastKnownLocation(task.getResult());
                        }
                    }
                });
                return true;
            }
            else{
                throw new SecurityException("locationPermissionGranted==false");
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
            return false;
        }
    }

    // White Box Test Case No. 17
    @Test
    public void testgetSnippetByCityName() {
        String NOTFOUND = " do not have informations in our system!";
        homeFragment.loadCityList();
        String testcityName1 = "Los Angeles City";
        String testName1 = homeFragment.getSnippetByCityName(testcityName1);
        assertEquals("Total cases: 492519\n" +
                "Case Rate: 12376\n" +
                "Death: 9154\n" +
                "14-Day Case: 10017\n" +
                "14-Day Case Rate: 251",testName1);
        String testcityName2 = "Seattle";
        String testName2 = homeFragment.getSnippetByCityName(testcityName2);
        assertEquals( testcityName2, testName2);
    }

    // White Box Test Case No. 18
    @Test
    public void testgetSnippetByCityNameExceptionHandle() {
        homeFragment.loadCityList();
        String testcityName1 = "Chernobyl";
        String snippetChernobyl = homeFragment.getSnippetByCityName(testcityName1);
        assertEquals("Total cases: 0\n" +
                "Case Rate: --- No Population Data ---\n" +
                "Death: 0\n" +
                "14-Day Case: 0\n" +
                "14-Day Case Rate: --- No Population Data ---",snippetChernobyl);
    }
}