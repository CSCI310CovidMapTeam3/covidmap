package com.example.myapplication.ui.home;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.DailyNotification.NotificationReceiver;
import com.example.myapplication.DataBase.HistoryDBHelper;
import com.example.myapplication.DataBase.HistoryItem;
import com.example.myapplication.DataBase.TestCenterDBHelper;
import com.example.myapplication.DataBase.WebSpider;
import com.example.myapplication.R;
import com.example.myapplication.ui.tracking.TrackingViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.ButtCap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.O)
public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private HomeViewModel homeViewModel;

    private static final String TAG = "HomeFragment";

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

    private Button backLA;

    private Date selectedDate = new Date(Timestamp.valueOf("2021-04-24 13:00:27.627").getTime());

    private String geoFencingCity = "";

    private LocationRequest mLocationRequest;

    private final long UPDATE_INTERVAL = 60 * 1000;  /* 60 second */
    private final long FASTEST_INTERVAL = 10 * 1000; /* 10 second */

    private Uri sound;

    private static final String NOTIFICATION_CHANNEL_ID = "10001";
    private static final String NOTIFICATION_CHANNEL_ID_CUSTOMIZED_SOUND = "10002";
    private boolean useCustomizeSound = false;

    public static final Map<Integer,String> CITY_CODE_TO_NAME;
    static{
        Hashtable<Integer,String> tmp =
                new Hashtable<Integer,String>();
        tmp.put(1,"Santa Monica");
        tmp.put(2,"Culver City");
        tmp.put(3,"Beverly Hills");
        tmp.put(4,"West Hollywood");
        tmp.put(5,"Los Angeles City");
        CITY_CODE_TO_NAME = Collections.unmodifiableMap(tmp);
    }

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

        Observer<Date> dateObserver = new Observer<Date>() {
            @Override
            public void onChanged(Date date) {
                selectedDate = date;
            }
        };

        sharedViewModel.getSelectedDate().observe(getViewLifecycleOwner(), dateObserver);

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

        backLA = root.findViewById(R.id.my_location);

        return root;
    }

    public void captureScreen() {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
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
                } catch (Throwable e) {
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

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle(marker.getTitle());
                alertDialogBuilder.setMessage(marker.getSnippet());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show(); // Show Dialog
                return true;
            }
        });

        if (defaultCity.compareTo("Santa Monica") == 0) {
            santaMonica.showInfoWindow();
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(santaMonicaLatLng)); // Position on Santa Monica
        } else if (defaultCity.compareTo("Culver City") == 0) {
            culverCity.showInfoWindow();
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(culverCityLatLng)); // Position on Santa Monica
        } else if (defaultCity.compareTo("Beverly Hills") == 0) {
            beverlyHills.showInfoWindow();
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(beverlyHillsLatLng)); // Position on Santa Monica
        } else if (defaultCity.compareTo("West Hollywood") == 0) {
            westHollywood.showInfoWindow();
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(westHollywoodLatLng)); // Position on Santa Monica
        } else if (defaultCity.compareTo("Los Angeles City") == 0) {
            losAngeles.showInfoWindow();
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(losAngelesLatLng)); // Position on Santa Monica
        }

        loadTravelTracking();
        startLocationUpdates();
    }

    private void loadTravelTracking() {
        //LatLng USCLatlng = new LatLng(34.0224, -118.2851);
        //LatLng SMCLatlng = new LatLng(34.0166,  -118.4704);
        // LatLng UCLALatlng = new LatLng(34.0689, -118.4452); // sucks

        ArrayList<LatLng> LatLngs = new ArrayList<LatLng>();

        HistoryDBHelper inst = HistoryDBHelper.getInstance(getContext());
        // SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.US);
        // String simpleDate = "24-04-2021 08:00:00";
        try {
            // Timestamp ts = Timestamp.valueOf("2021-04-24 13:00:27.627");
            // Date date = new Date(ts.getTime());
            Date date = selectedDate;
            Log.d(TAG, date.toString());
            ArrayList<HistoryItem> historyItems = inst.retrieveByDate(date);
            //ArrayList<HistoryItem> historyItems = inst.getAllListHistory();
            int count = 0;
            Timestamp lastTimeStamp = new Timestamp(0);
            for (HistoryItem historyItem : historyItems) {
                Log.d(TAG, historyItem.toString() + count);
                Timestamp currentTimeStamp = historyItem.getTimestamp();
                // Ensure that two time stamps are 1 minutes away from each other.
                if (currentTimeStamp.getTime() - lastTimeStamp.getTime() > 1000 * 60) {
                    LatLngs.add(new LatLng(historyItem.getLat(), historyItem.getLon()));
                    lastTimeStamp = currentTimeStamp;
                }
                count++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Load Travel Tracking Date Error");
        }

        Polyline line = map.addPolyline(new PolylineOptions()
                .addAll(LatLngs)
                .startCap(new RoundCap())
                .endCap(new ButtCap())
                .width(15)
                .color(Color.argb(255, 153, 27, 30)));

        line.setTag(new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(selectedDate));
        line.setJointType(JointType.ROUND);
        line.setClickable(true);

        map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                if ((polyline.getColor() == Color.argb(255, 153, 27, 30))) {
                    polyline.setColor(Color.argb(255, 255, 204, 0));
                } else {
                    // The default pattern is a solid stroke.
                    polyline.setColor(Color.argb(255, 153, 27, 30));
                }

                Toast.makeText(getActivity(), "Date " + polyline.getTag().toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    // load default List
    public void loadCityList() {
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
        City losAngelesCity = new City("Los Angeles City", 5, losAngelesCityCityCenter, 27507, 3935, 51, 0, 10017);
        cities.add(losAngelesCity);

        City chernobyl = new City("Chernobyl");
        cities.add(chernobyl);

    }

    public void loadNewestList() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        int[] DEFAULT = {0, 0, 0, 0, 0};
        if (WebSpider.getResults() == null || WebSpider.getTotalResults() == null) {
            loadCityList();
            return;
        }
        int[] newFourteenCases = WebSpider.getVaccinated();
        int[] newTotalCases = WebSpider.getTotalCases();
        if (newFourteenCases == null || newTotalCases == null) {
            loadCityList();
        } else {
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

            City santaMonica = new City("Santa Monica", 1, santaMonicaCityCenter, 91577, newTotalCases[0], 156, 0, newFourteenCases[0]);
            cities.add(santaMonica);
            City culverCity = new City("Culver City", 2, culverCityCityCenter, 39169, newTotalCases[1], 96, 0, newFourteenCases[1]);
            cities.add(culverCity);
            City beverlyHills = new City("Beverly Hills", 3, beverlyHillsCityCenter, 34186, newTotalCases[2], 34, 0, newFourteenCases[2]);
            cities.add(beverlyHills);
            City westHollywood = new City("West Hollywood", 4, westHollywoodCityCenter, 36450, newTotalCases[3], 35, 0, newFourteenCases[3]);
            cities.add(westHollywood);
            City losAngelesCity = new City("Los Angeles City", 5, losAngelesCityCityCenter, 27507, newTotalCases[4], 9154, 0, newFourteenCases[4]);
            cities.add(losAngelesCity);

            City chernobyl = new City("Chernobyl");
            cities.add(chernobyl);
        }

    }


    private float getMarkerColorFromCityName(String name) {
        for (City city : cities) {
            if (city.getCityName().equals(name)) {
                return city.getMarkerColor();
            }
        }
        return BitmapDescriptorFactory.HUE_BLUE;
    }

    public String getSnippetByCityName(String name) {
        StringBuilder sb = new StringBuilder();
        for (City city : cities) {
            if (city.getCityName().equals(name)) {

                sb.append("Total cases: ");
                sb.append(city.getCaseNumber());
                sb.append("\n");
                sb.append("Case Rate: ");
                try {
                    sb.append((int) city.getCaseRate());
                } catch (IllegalStateException ise) {
                    sb.append("--- No Population Data ---");
                } catch (Exception e) {
                    sb.append("--- Internal Error ---");
                }
                sb.append("\n");
                sb.append("Death: ");
                sb.append(city.getDeathNumber());
                sb.append("\n");
                sb.append("Vaccinated Number: ");
                sb.append(city.getFourteenDayCaseNumber());
                sb.append("\n");
                sb.append("Vaccinated Rate: ");
                try {
                    sb.append((int) (city.getNewCaseRate() / 1000));
                    sb.append('%');
                } catch (IllegalStateException ise) {
                    sb.append("--- No Population Data ---");
                } catch (Exception e) {
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

    public boolean testDummyFunction() {
        return true;
    }

    public boolean testGetLocationPermission() {
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
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void getDeviceLocation() {
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
                                if (checkInLACounty(lastKnownLocation) != -1) {
                                    // geofenceHelper(getContext());
                                    lastKnownLocationInLA = true;
                                    sharedViewModel.setCountyData("Los Angeles County");
                                    backLA.setVisibility(View.INVISIBLE);
                                } else if (lastKnownLocationInLA) {
                                    lastKnownLocationInLA = false;
                                    sharedViewModel.setCountyData("Not in Los Angeles County");

                                    backLA.setVisibility(View.VISIBLE);

                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                                    alertDialogBuilder.setTitle("Friendly Warning");
                                    alertDialogBuilder.setMessage("Out of Los Angeles County \n(Current Service Area)");
                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show(); // Show Dialog
                                }
                            } else {
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
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private int checkInLACounty(Location location){
        if (location != null) {
            if (location.getLatitude() > 33.5 &&  location.getLatitude() < 34.8){
                if (location.getLongitude() > -118.9 && location.getLongitude() < -117.6){
                    Log.d(TAG, "checkInLACounty: In LA County");
                    // Start Dummy and Hardcoded Geocoder for our real service area
                    if (location.getLatitude() > 33.99 && location.getLatitude() < 34.10 && location.getLongitude() > -118.52 && location.getLongitude() < -118.20){
                        if (location.getLongitude() < -118.43) {
                            return 1; // Santa Monica
                        } else if (location.getLongitude() < -118.39){
                            if (location.getLatitude() > 34.06){
                                return 3; // Beverly Hills
                            } else {
                                return 2; // Culver City
                            }
                        } else if (location.getLongitude() < -118.33){
                            if (location.getLatitude() > 34.06){
                                return 4; // West Hollywood
                            } else {
                                return 5; // Los Angeles City Downtown
                            }
                        } else {
                            return 5; // Los Angeles City Downtown
                        }
                    } else {
                        return 6; // In LA county but not in our service area
                    }
                }
            }
            Log.d(TAG, "checkInLACounty: Not In LA");
            return -1;
        }
        Log.d(TAG, "checkInLACounty: Null Location");
        return 0; // Location is Null
    }

    private void geofenceHelper(Context mContext) {

        String newGeoFencingCity = "";

        try {
            int checkInLAResult = checkInLACounty(lastKnownLocation);
            if (checkInLAResult >= 1){
                if (checkInLAResult <= 5){
                    newGeoFencingCity = CITY_CODE_TO_NAME.get(checkInLAResult);
                    if(newGeoFencingCity != null){
                        Log.d(TAG, "Geofence: Dummy Geocoder New City is "+newGeoFencingCity);
                    } else{
                        Log.e(TAG, "Geofence: Dummy Geocoder New City return null");
                    }
                } else{
                    newGeoFencingCity = "Los Angeles County";
                    Log.d(TAG, "Geofence: Dummy Geocoder New City return In Los Angeles County");
                }

            } else{
                newGeoFencingCity = "Not in Los Angeles County";
                Log.d(TAG, "Geofence: Dummy Geocoder New City return not in Los Angeles County");
            }

            // If city changed
            if (!newGeoFencingCity.equals(geoFencingCity)) {
                // If the original city is not empty
                if (!geoFencingCity.equals("")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Welcome to ");
                    sb.append(newGeoFencingCity);
                    sb.append("\n");
                    sb.append(getSnippetByCityName(geoFencingCity));
                    Log.d(TAG, "Geofence: Push notification, update City");
                    Log.d(TAG, "Geofence"+sb.toString());
                    sendGeofenceNotification(sb.toString());
                }
            }

            geoFencingCity = newGeoFencingCity;
        } catch (Exception e) {
            Log.e(TAG, "Geofence: Dummy Geocoder Exception during Geocoder. " + e);
            e.printStackTrace();
        }
        Log.d(TAG, "Geofence: Current Geofence City is" + geoFencingCity);
    }

    private void startLocationUpdates() {
        getSoundPreference();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
        settingsClient.checkLocationSettings(locationSettingsRequest);

        try {
            if (locationPermissionGranted) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    getLocationPermission();
                    return;
                }
                fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                // do work here
                                onLocationChanged(locationResult.getLastLocation());
                            }
                        },
                        Looper.myLooper());
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    public void onLocationChanged(Location location) {
        /* New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        */

        // Update Last Known Location
        lastKnownLocation = location;
        geofenceHelper(getContext());
    }

    // Zhian Li: I copyed code from Yijia Chen on notification
    public void sendGeofenceNotification(String message){
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel notificationChannel;

        Intent intent = new Intent(getActivity().getApplicationContext(), HomeFragment.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(getActivity().getApplicationContext(),
                0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity().getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("Geofence NOTIFICATION")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

        if (useCustomizeSound){
            builder.setSound(sound);
        }

        if (useCustomizeSound){
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_CUSTOMIZED_SOUND, "NOTIFICATION_CHANNEL_CUSTOMIZED_SOUND_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            // Set Channel Sound
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            notificationChannel.setSound(sound, audioAttributes);

            Log.d(TAG, "sendGeofenceNotification(): using customized sound");
            builder.setChannelId(NOTIFICATION_CHANNEL_ID_CUSTOMIZED_SOUND);
        } else{
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
        }
        NotificationManager notificationManager = getActivity().getApplicationContext().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);

        // NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(42, builder.build());
    }

    private void getSoundPreference(){
        sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + BuildConfig.APPLICATION_ID + "/" + R.raw.fighton);

        SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getContext());

        String currentSoundSetting = saved_values.getString("count", "default");
        Log.d(TAG, "getSoundPreference(): "+ currentSoundSetting);

        if (currentSoundSetting.equals("Fight On")) {
            useCustomizeSound = true;
        }


    }
}