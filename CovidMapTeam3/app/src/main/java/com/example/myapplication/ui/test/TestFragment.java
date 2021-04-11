package com.example.myapplication.ui.test;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.DataBase.TestCenter;
import com.example.myapplication.DataBase.TestCenterDBHelper;

import com.example.myapplication.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TestFragment extends Fragment implements OnMapReadyCallback {



    private static final String TAG = testMapActivity.class.getSimpleName();

    private GoogleMap map;
    private CameraPosition cameraPosition;

    // The entry point to the Places API.
    private PlacesClient placesClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 10; // City Level
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private Location lastKnownLocation;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // [START_EXCLUDE silent]
        // Construct a PlacesClient
        Places.initialize(getActivity().getApplicationContext(), BuildConfig.MAPS_API_KEY);
        placesClient = Places.createClient(getActivity());

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        final SupportMapFragment myMAPF = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        myMAPF.getMapAsync(this);

        root.findViewById(R.id.share_screenshot).setOnClickListener(v -> {
            captureScreen();
        });

        return root;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        Log.i(TAG, "On Map Ready");

        // Add a marker in USC and move the camera
//        LatLng usc = new LatLng(34.0224, -118.2852);
//        googleMap.addMarker(new MarkerOptions()
//                .position(usc)
//                .title("USC")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        googleMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

//        // Add markers in Santa Monica, Culver City, Beverly Hills, West Hollywood, Los Angeles
//        LatLng santaMonicaLatLng = new LatLng(34.0195, -118.4912);
//        Marker santaMonica = googleMap.addMarker(
//                new MarkerOptions()
//                        .position(santaMonicaLatLng)
//                        .title("Santa Monica")
//                        .snippet("Total cases: 100")
//                        .icon(BitmapDescriptorFactory.defaultMarker(40)));

        TestCenterDBHelper inst = TestCenterDBHelper.getInstance(getContext());
        ArrayList<TestCenter> testList = inst.getListTestCenter();
        for (TestCenter center : testList) {
            LatLng tempLoc = new LatLng(center.getLat(), center.getLon());
            Marker temp = googleMap.addMarker(
                    new MarkerOptions()
                            .position(tempLoc)
                            .title(center.getName())
                            .snippet(center.getAddress())
                            .icon(BitmapDescriptorFactory.defaultMarker(40)));
        }

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle(marker.getTitle());
                alertDialogBuilder.setMessage(marker.getSnippet());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();//将dialog显示出来
                return true;
            }
        });

    }

    public void captureScreen()
    {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback()
        {

            @Override
            public void onSnapshotReady(Bitmap snapshot)
            {
                Bitmap bitmap = snapshot;

                Date now = new Date();
                android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

                String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";
                try {
                    File imageFile = new File(mPath);

                    String path = MediaStore.Images.Media.insertImage(
                            getContext().getContentResolver(), bitmap, "CaseData" + Calendar.getInstance().getTime()
                                    + ".png", null);

                    Uri screenshotUri = Uri.parse(path);

                    final Intent emailIntent = new Intent(
                            android.content.Intent.ACTION_SEND);
                    emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    emailIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                    emailIntent.setType("image/png");
                    getContext().startActivity(Intent.createChooser(emailIntent,
                            "Share Case Data"));
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };

        map.snapshot(callback);
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;

        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
}