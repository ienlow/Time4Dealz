package com.example.isaacenlow.time4dealz;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private int DEFAULT_ZOOM = 15;
    private LatLng radford = new LatLng(37.1318, -80.5764477);
    private LatLng lebanon = new LatLng(36.896034, -82.068117);
    private LatLng dedmon = new LatLng(37.1385, -80.5416);
    private LatLng sterling = new LatLng(39.040899, -77.037234);
    private boolean mRequestingLocationUpdates = false;
    private LocationCallback mLocationCallback;
    private List<LatLng> locations = new ArrayList<>();
    private ScanResult scanResult;
    private LocationRequest mLocationRequest = new LocationRequest();
    private int i;
    private String mMapState = "Points";
    //import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper; use for db calls



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Google API
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        BackgroundWorker backgroundWorker = new BackgroundWorker();
        backgroundWorker.execute();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    for (int i = 0; i < locations.size(); i++) {
                        LatLng mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        if (locations != null) {
                            mMap.addCircle(new CircleOptions().center(locations.get(i)).radius(100));
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
                    }
                }
            }
        };
    }

    private class BackgroundWorker extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName("ExampleSchool")
                    .withAttributesToGet("active")
                    .withAttributesToGet("latitude")
                    .withAttributesToGet(("longitude"));
            scanResult = dynamoDBClient.scan(scanRequest);
            return null;
        }

        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);
            for (Map<String, AttributeValue> item : scanResult.getItems()) {
                if (item.get("active").getBOOL()) {
                    locations.add(new LatLng(Double.parseDouble(item.get("latitude").getN()), Double.parseDouble(item.get("longitude").getN())));
                }
            }
            Toast.makeText(getApplicationContext(), locations.size() + "", Toast.LENGTH_SHORT).show();
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
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Add a marker in Radford and move the camera
        //mMap.addMarker(new MarkerOptions().position(sterling).title("Marker in Radford"));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mMap.getCameraPosition()));

        //createLocationRequest();

        startLocationUpdates();

        mMap.setMyLocationEnabled(true);
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
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        Log.i("Stop", "connection");
    }
}
