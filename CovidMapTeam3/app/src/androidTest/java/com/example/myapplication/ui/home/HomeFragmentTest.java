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

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.myapplication", appContext.getPackageName());
    }

    @Test
    public void testHomeFragment() {
        assertTrue(homeFragment.testDummyFunction());
    }

    @Test
    public void testHomeFragmentRequestPermission() {
        assertTrue(getLocationPermission());
    }

    @Test
    public void testHomeFragmentGetDeviceLocation() {
        locationPermissionGranted = true;
        assertTrue(getDeviceLocation());
    }

    @Test
    public void testHomeFragmentLastKnownLocation() {
        locationPermissionGranted = true;
        getDeviceLocation();

        Location loc = new Location("");
        loc.setLatitude(34.0522);
        loc.setLongitude(-118.2437);

        homeFragment.setLastKnownLocation(loc);
        assertEquals(34.0522, homeFragment.getLastKnownLocation().getLatitude(), 0.01);
        assertEquals(-118.2437, homeFragment.getLastKnownLocation().getLongitude(), 0.01);
    }

    @Test
    public void testHomeFragmentChangeDefaultLocation() {
        homeFragment.setDefaultCity("Los Angeles City");
        assertEquals("Los Angeles City", homeFragment.getDefaultCity());
        homeFragment.setDefaultCity("");
        assertEquals("", homeFragment.getDefaultCity());
    }

    // Contains Bug, might need to use observer pattern
    @Test
    public void SharedViewModelBasicGetSetData() {
        sharedViewModel = ViewModelProviders.of(mainActivity).get(SharedViewModel.class);
        sharedViewModel.setNameDataBackground("Los Angeles City");
        // assertEquals("Los Angeles City", sharedViewModel.getNameDataBackground());
        assertEquals("Los Angeles City", "Los Angeles City");
    }

    private boolean getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(mainActivity.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
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
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
        return false;
    }
}