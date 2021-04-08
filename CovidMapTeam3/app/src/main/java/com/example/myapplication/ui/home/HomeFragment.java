package com.example.myapplication.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.DataBase.TestCenterDBHelper;
import com.example.myapplication.DataBase.WebSpider;
import com.example.myapplication.activity.AboutActivity;
import com.example.myapplication.activity.MapsActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.setting.SettingViewModel;
import com.example.myapplication.ui.tracking.TrackingViewModel;
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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private HomeViewModel homeViewModel;

    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap map;
    private CameraPosition cameraPosition;

    // The entry point to the Places API.
    private PlacesClient placesClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted. (We changed the logic so it should now be centered at LA)
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 10; // City Level
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private Location lastKnownLocation;

    private static String defaultCity = "";

    private SharedViewModel sharedViewModel;

    private TrackingViewModel trackingViewModel;
    ArrayList<City> cities;
    private boolean lastKnownLocationInLA = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
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

        // Reference: https://nabeelj.medium.com/android-how-to-share-data-between-fragments-using-viewmodel-and-livedata-android-mvvm-9fc463af5152
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);
        trackingViewModel = ViewModelProviders.of(requireActivity()).get(TrackingViewModel.class);

        Observer<String> nameObserver = new Observer<String>() {
            @Override
            public void onChanged(String name) {
                defaultCity = name;
            }
        };

        sharedViewModel.getNameData().observe(getViewLifecycleOwner(), nameObserver);

        root.findViewById(R.id.share_screenshot).setOnClickListener(v -> {
            captureScreen();
        });

        root.findViewById(R.id.my_location).setOnClickListener(v -> {
            if (lastKnownLocation != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(34.0522, -118.2437), DEFAULT_ZOOM));
//                        new LatLng(lastKnownLocation.getLatitude(),
//                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
            }
        });

        loadNewestList();
        return root;
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        Log.i(TAG, "On Map Ready");
        // defaultCity = new ViewModelProvider(this).get(SettingViewModel.class).getCity().getValue();
        Log.i(TAG, defaultCity);

        googleMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));


        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        // Add markers in Santa Monica, Culver City, Beverly Hills, West Hollywood, Los Angeles
        LatLng santaMonicaLatLng = new LatLng(34.0195, -118.4912);
        Marker santaMonica = googleMap.addMarker(
                new MarkerOptions()
                    .position(santaMonicaLatLng)
                    .title("Santa Monica")
                    .snippet(getSnippetByCityName(getSnippetByCityName("Santa Monica")))
                    .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColorFromCityName("Santa Monica"))));

        LatLng culverCityLatLng = new LatLng(34.0211, -118.3965);
        Marker culverCity = googleMap.addMarker(
                new MarkerOptions()
                    .position(culverCityLatLng)
                    .title("Culver City")
                    .snippet(getSnippetByCityName(getSnippetByCityName("Culver City")))
                    .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColorFromCityName("Culver City"))));

        LatLng beverlyHillsLatLng = new LatLng(34.0736, -118.4004);
        Marker beverlyHills = googleMap.addMarker(
                new MarkerOptions()
                    .position(beverlyHillsLatLng)
                    .title("Beverly Hills")
                    .snippet(getSnippetByCityName(getSnippetByCityName("Beverly Hills")))
                    .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColorFromCityName("Beverly Hills"))));

        LatLng westHollywoodLatLng = new LatLng(34.0900, -118.3617);
        Marker westHollywood = googleMap.addMarker(
                new MarkerOptions()
                    .position(westHollywoodLatLng)
                    .title("West Hollywood")
                    .snippet(getSnippetByCityName(getSnippetByCityName("West Hollywood")))
                    .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColorFromCityName("West Hollywood"))));

        LatLng losAngelesLatLng = new LatLng(34.0522, -118.2437);
        Marker losAngeles = googleMap.addMarker(
                new MarkerOptions()
                    .position(losAngelesLatLng)
                    .title("Los Angeles City")
                    .snippet(getSnippetByCityName(getSnippetByCityName("Los Angeles City")))
                    .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColorFromCityName("Los Angeles City"))));

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(34.0224, -118.2852))); // Position on USC

        TestCenterDBHelper inst = TestCenterDBHelper.getInstance(getContext());

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle(marker.getTitle());
                alertDialogBuilder.setMessage(marker.getSnippet());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show(); // Show Dialog
                return true;
            }
        });

        if (defaultCity.compareTo("Santa Monica") == 0){
            santaMonica.showInfoWindow();
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(santaMonicaLatLng)); // Position on Santa Monica
        }
        else if (defaultCity.compareTo("Culver City") == 0){
            culverCity.showInfoWindow();
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(culverCityLatLng)); // Position on Santa Monica
        }
        else if (defaultCity.compareTo("Beverly Hills") == 0){
            beverlyHills.showInfoWindow();
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(beverlyHillsLatLng)); // Position on Santa Monica
        }
        else if (defaultCity.compareTo("West Hollywood") == 0){
            westHollywood.showInfoWindow();
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(westHollywoodLatLng)); // Position on Santa Monica
        }
        else if (defaultCity.compareTo("Los Angeles City") == 0){
            losAngeles.showInfoWindow();
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(losAngelesLatLng)); // Position on Santa Monica
        }
    }

    // load default List
    public void loadCityList(){
        cities = new ArrayList<City>();

        Location santaMonicaCityCenter = new Location("");
        Location culverCityCityCenter = new Location("");
        Location beverlyHillsCityCenter = new Location("");
        Location westHollywoodCityCenter = new Location("");
        Location losAngelesCityCityCenter = new Location("");

        santaMonicaCityCenter.setLatitude(34.0195);
        santaMonicaCityCenter.setLongitude(-118.4912);

        culverCityCityCenter.setLatitude(34.0211);
        culverCityCityCenter.setLongitude(-118.3965);

        beverlyHillsCityCenter.setLatitude(34.0736);
        beverlyHillsCityCenter.setLongitude(-118.4004);

        westHollywoodCityCenter.setLatitude(34.0900);
        westHollywoodCityCenter.setLongitude(-118.3617);

        losAngelesCityCityCenter.setLatitude(34.0522);
        losAngelesCityCityCenter.setLongitude(-118.2437);

        City santaMonica = new City("Santa Monica", 1, santaMonicaCityCenter, 91577, 4515, 156, 0, 62);
        cities.add(santaMonica);
        City culverCity = new City("Culver City", 2, culverCityCityCenter, 39169, 2131, 96, 0, 26);
        cities.add(culverCity);
        City beverlyHills = new City("Beverly Hills", 3, beverlyHillsCityCenter, 34186, 2566, 34, 0, 35);
        cities.add(beverlyHills);
        City westHollywood = new City("West Hollywood", 4, westHollywoodCityCenter, 36450, 2194, 35, 0, 78);
        cities.add(westHollywood);
        City losAngelesCity = new City("Los Angeles City", 5, losAngelesCityCityCenter, 3979576, 492519, 9154, 0, 10017);
        cities.add(losAngelesCity);

        City chernobyl = new City("Chernobyl");
        cities.add(chernobyl);
    }

    public void loadNewestList(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        int [] DEFAULT = {0, 0, 0, 0, 0};
        if(WebSpider.getResults() == null){
            loadCityList();
            return;
        }
        int[] newFourteenCases = WebSpider.getForteenCases();
        if(newFourteenCases == null){
            loadCityList();
        }else{
            cities = new ArrayList<City>();

            Location santaMonicaCityCenter = new Location("");
            Location culverCityCityCenter = new Location("");
            Location beverlyHillsCityCenter = new Location("");
            Location westHollywoodCityCenter = new Location("");
            Location losAngelesCityCityCenter = new Location("");

            santaMonicaCityCenter.setLatitude(34.0195);
            santaMonicaCityCenter.setLongitude(-118.4912);

            culverCityCityCenter.setLatitude(34.0211);
            culverCityCityCenter.setLongitude(-118.3965);

            beverlyHillsCityCenter.setLatitude(34.0736);
            beverlyHillsCityCenter.setLongitude(-118.4004);

            westHollywoodCityCenter.setLatitude(34.0900);
            westHollywoodCityCenter.setLongitude(-118.3617);

            losAngelesCityCityCenter.setLatitude(34.0522);
            losAngelesCityCityCenter.setLongitude(-118.2437);

            City santaMonica = new City("Santa Monica", 1, santaMonicaCityCenter, 91577, 4515, 156, 0, newFourteenCases[0]);
            cities.add(santaMonica);
            City culverCity = new City("Culver City", 2, culverCityCityCenter, 39169, 2131, 96, 0, newFourteenCases[1]);
            cities.add(culverCity);
            City beverlyHills = new City("Beverly Hills", 3, beverlyHillsCityCenter, 34186, 2566, 34, 0, newFourteenCases[2]);
            cities.add(beverlyHills);
            City westHollywood = new City("West Hollywood", 4, westHollywoodCityCenter, 36450, 2194, 35, 0, newFourteenCases[3]);
            cities.add(westHollywood);
            City losAngelesCity = new City("Los Angeles City", 5, losAngelesCityCityCenter, 3979576, 492519, 9154, 0, newFourteenCases[4]);
            cities.add(losAngelesCity);

            City chernobyl = new City("Chernobyl");
            cities.add(chernobyl);
        }

    }


    private float getMarkerColorFromCityName(String name){
        for (City city : cities){
            if (city.getCityName().equals(name)){
                return city.getMarkerColor();
            }
        }
        return BitmapDescriptorFactory.HUE_BLUE;
    }

    public String getSnippetByCityName(String name){
        StringBuilder sb = new StringBuilder();
        for (City city : cities){
            if (city.getCityName().equals(name)){

                sb.append("Total cases: ");
                sb.append(city.getCaseNumber());
                sb.append("\n");
                sb.append("Case Rate: ");
                try {
                    sb.append((int)city.getCaseRate());
                } catch (IllegalStateException ise){
                    sb.append("--- No Population Data ---");
                } catch (Exception e){
                    sb.append("--- Internal Error ---");
                }
                sb.append("\n");
                sb.append("Death: ");
                sb.append(city.getDeathNumber());
                sb.append("\n");
                sb.append("14-Day Case: ");
                sb.append(city.getFourteenDayCaseNumber());
                sb.append("\n");
                sb.append("14-Day Case Rate: ");
                try {
                    sb.append((int)city.getNewCaseRate());
                } catch (IllegalStateException ise){
                    sb.append("--- No Population Data ---");
                } catch (Exception e){
                    sb.append("--- Internal Error ---");
                }
                return sb.toString();
            }
        }
        return name;
        //return name+" do not have informations in our system!";
    }

    public String getDefaultCity() {
        return defaultCity;
    }

    public void setDefaultCity(String defaultCity) {
        HomeFragment.defaultCity = defaultCity;
    }

    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    public void setLastKnownLocation(Location lastKnownLocation) {
        this.lastKnownLocation = lastKnownLocation;
    }

    public boolean testDummyFunction(){
        return true;
    }

    public boolean testGetLocationPermission(){
        getLocationPermission();
        return true;
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
                                if (checkInLACounty() != -1){
                                    lastKnownLocationInLA = true;
                                    sharedViewModel.setCountyData("Los Angeles County");
                                }
                                else if (lastKnownLocationInLA){
                                    lastKnownLocationInLA = false;
                                    sharedViewModel.setCountyData("Not in Los Angeles County");
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                                    alertDialogBuilder.setTitle("Friendly Warning");
                                    alertDialogBuilder.setMessage("Out of Los Angeles County \n(Current Service Area)");
                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show(); // Show Dialog
                                }
                            }
                            else{
                                sharedViewModel.setCountyData("Los Angeles County");
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

    private int checkInLACounty(){ // 1 means in LA, 0 means location unknown, -1 means not in LA
        if (lastKnownLocation != null) {
            if (lastKnownLocation.getLatitude() > 33.5 &&  lastKnownLocation.getLatitude() < 34.1){
                if (lastKnownLocation.getLongitude() > -118.6 && lastKnownLocation.getLongitude() < -118){
                    Log.d(TAG, "checkInLACounty: In LA");
                    return 1;
                }
            }
            Log.d(TAG, "checkInLACounty: Not In LA");
            return -1;
        }
        Log.d(TAG, "checkInLACounty: Null Location");
        return 0; // Location is Null Default to True
    }


}