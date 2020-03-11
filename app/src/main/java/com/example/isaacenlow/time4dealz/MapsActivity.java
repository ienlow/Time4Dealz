package com.example.isaacenlow.time4dealz;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jsoup.nodes.Element;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private int DEFAULT_ZOOM = 15;
    private LatLng radford = new LatLng(37.1318, -80.5764477);
    private LatLng lebanon = new LatLng(36.896034, -82.068117);
    private LatLng dedmon = new LatLng(37.1385, -80.5416);
    private LatLng sterlingCoord = new LatLng(39.040899, -77.037234);
    private String gdit = "15036 Conference Center Dr, Chantilly, VA 20151";
    private String sterling = "46194 walpole terr, sterling, va";
    private boolean mRequestingLocationUpdates = false;
    private LocationCallback mLocationCallback;
    private List<LatLng> locations = new ArrayList<>();
    private ScanResult scanResult;
    private LocationRequest mLocationRequest = LocationRequest.create();
    ArrayList<LocationAddress> locationAddressList = new ArrayList<>();
    private int i;

    private String mMapState = "Points";
    //import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper; use for db calls


    @Override
    public void onCreate(Bundle savedInstanceState) {

        BackgroundWorker backgroundWorker = new BackgroundWorker();
        backgroundWorker.execute();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    for (int i = 0; i < locations.size(); i++) {
                        LatLng mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        if (locations != null) {
                            mMap.addCircle(new CircleOptions().center(locations.get(i)).radius(100));
                        }
                    }
                }
            }
        };
    }

    private class BackgroundWorker extends AsyncTask<String, Void, String> implements OnMapReadyCallback {

        @Override
        protected String doInBackground(String... strings) {
            locationAddressList.add(getAddressFromLocation(getApplicationContext(), sterling));
            locationAddressList.add(getAddressFromLocation(getApplicationContext(), gdit));
            /*final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName("ExampleSchool")
                    .withAttributesToGet("latitude")
                    .withAttributesToGet(("longitude"));
            scanResult = dynamoDBClient.scan(scanRequest);*/
            ArrayList<Event> teams = new ArrayList<>();
            ArrayList<Event> upcomingEvents = new ArrayList<>();
            ArrayList<Event> currentEvents = new ArrayList<>();
            List<Element> elementList = new ArrayList<>();
            try {
                final String json = "https://calendar.radford.edu/live/calendar/view/all?user_tz=America%2FDetroit&syntax=%3Cwidget%20type%3D%22events_calendar%22%3E%3Carg%20id%3D%22mini_cal_heat_map%22%3Etrue%3C%2Farg%3E%3Carg%20id%3D%22thumb_width%22%3E200%3C%2Farg%3E%3Carg%20id%3D%22thumb_height%22%3E200%3C%2Farg%3E%3Carg%20id%3D%22hide_repeats%22%3Etrue%3C%2Farg%3E%3Carg%20id%3D%22show_groups%22%3Etrue%3C%2Farg%3E%3Carg%20id%3D%22show_locations%22%3Efalse%3C%2Farg%3E%3Carg%20id%3D%22show_tags%22%3Etrue%3C%2Farg%3E%3Carg%20id%3D%22use_tag_classes%22%3Efalse%3C%2Farg%3E%3Carg%20id%3D%22search_all_events_only%22%3Etrue%3C%2Farg%3E%3Carg%20id%3D%22use_modular_templates%22%3Etrue%3C%2Farg%3E%3C%2Fwidget%3E";
                Gson gson = new GsonBuilder().setLenient().create();
                String trim = MainMenu.readURL(json).trim();
                JsonObject jsonObject = gson.fromJson(trim, JsonObject.class);
                System.out.println(jsonObject.get("events"));
                JsonObject jsonObject1 = jsonObject.get("events").getAsJsonObject();
                Calendar current = Calendar.getInstance();
                Calendar future = Calendar.getInstance();
                Calendar today = Calendar.getInstance();
                future.set(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH));
                System.out.println(new SimpleDateFormat("yyyyMMdd").format(current.getTime()));
                int x = 0;
                while(current.equals(future)) {
                    if (jsonObject1.get(new SimpleDateFormat("yyyyMMdd").format(current.getTime())) != null) {
                        JsonArray jsonArray = jsonObject1.get(new SimpleDateFormat("yyyyMMdd").format(current.getTime())).getAsJsonArray();
                        System.out.println(current.get(Calendar.DAY_OF_MONTH));
                        for (int i = 0; i < jsonArray.size(); i++) {
                            Calendar timeCal = Calendar.getInstance();
                            timeCal.setTimeInMillis(jsonArray.get(i).getAsJsonObject().get("ts_start").getAsInt());
                            timeCal.setTime(new java.util.Date (timeCal.getTimeInMillis()*1000));
                            Event one = new Event(
                                    jsonArray.get(i).getAsJsonObject().get("title").getAsString(),
                                    new SimpleDateFormat("MM/dd/yyyy HH:mm").format(timeCal.getTime()),
                                    // location
                                    jsonArray.get(i).getAsJsonObject().get("location") != null ? jsonArray.get(i).getAsJsonObject().get("location").getAsString() : "N/A",
                                    String.valueOf(timeCal.getTimeInMillis()/1000),
                                    "",
                                    null,//item.get("imageUrl").getS(),
                                    null, 0);
                            System.out.println("timeCal: " + timeCal.get(Calendar.MONTH) + "" + timeCal.get(Calendar.DAY_OF_MONTH));
                            if (today.get(Calendar.YEAR) == current.get(Calendar.YEAR)
                                    && today.get(Calendar.MONTH) == current.get(Calendar.MONTH)
                                    && today.get(Calendar.DAY_OF_MONTH) == current.get(Calendar.DAY_OF_MONTH)) {
                                currentEvents.add(one);
                            } else {
                                upcomingEvents.add(one);
                            }
                            teams.add(one);
                        }
                        x++;
                        Log.i("loop value", String.valueOf(x));
                        Log.i("current events", String.valueOf(currentEvents.size()));
                        for (int j = 0; j < currentEvents.size(); j++) {
                            locationAddressList.add(getAddressFromLocation(getApplicationContext(), currentEvents.get(j).location));
                        }
                    }
                    current.add(Calendar.DAY_OF_MONTH, 1);
                }
            } catch (IOException e) {
                Log.e("Unable to retrieve data", e.getLocalizedMessage());
            }
            Log.e("Element List size", String.valueOf(teams.size()));
            return null;
        }

        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);
            for (LocationAddress locationAddress : locationAddressList) {
                locations.add(new LatLng(locationAddress.getLatitude(), locationAddress.getLongitude()));
            }
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            Toast.makeText(getApplicationContext(), locations.size() + "", Toast.LENGTH_SHORT).show();
        }

        public LocationAddress getAddressFromLocation(Context context, String strAddress) {
            LocationAddress locationAddress = new LocationAddress();
            if (!strAddress.equals("")) {
                try {
                    Geocoder coder = new Geocoder(context, Locale.US);
                    List<Address> test = coder.getFromLocationName(strAddress, 2);
                    Address add = test.get(0);
                    locationAddress.setAddress(add.getAddressLine(0));
                    locationAddress.setLatitude(add.getLatitude());
                    locationAddress.setLongitude(add.getLongitude());
                    //address_line += " " + add.getLatitude();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return locationAddress;
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            // Add a marker in Radford and move the camera
            //mMap.addMarker(new MarkerOptions().position(sterling).title("Marker in Radford"));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mMap.getCameraPosition()));

            createLocationRequest();

            startLocationUpdates();

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    /**
     * Create request to update location.
     */
    protected void createLocationRequest() {
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        mRequestingLocationUpdates = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    // Save instance state
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(mMapState, i);

        super.onSaveInstanceState(savedInstanceState);
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("failed", "connection");
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("Stop", "connection");
    }
}
