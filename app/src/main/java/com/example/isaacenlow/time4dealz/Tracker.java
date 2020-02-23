package com.example.isaacenlow.time4dealz;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

import java.util.Map;


public class Tracker extends Service implements GoogleApiClient.OnConnectionFailedListener {
    private LocationCallback mLocationCallback;
    private LatLng lebanon = new LatLng(36.896034, -82.068117);
    private LocationRequest mLocationRequest = new LocationRequest();
    private boolean mRequestingLocationUpdates;
    private FusedLocationProviderClient mFusedLocationClient;
    private DynamoDBMapper dynamoDBMapper;
    private LocationsDO locationItem;
    private int i, points = 0;
    private BroadcastReceiver br;
    private long timeLeftInMilliseconds = 0, startTime = 0, timeSwapBuff = 0, updateTime = 0;
    boolean timerPaused = false, timerStarted = false;
    private int seconds, minutes, hours;
    private Handler handler;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private NotificationCompat.Builder mNotification;
    Notification notification;
    public static final String MY_PREFS = "MyPrefs";
    String channel = "com.example.isaacenlow.time4dealz";

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        editor = prefs.edit();
        handler = new Handler();
        createNotificationChannel();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channel).setSmallIcon(android.R.drawable.ic_menu_mylocation).setContentTitle("Location in use");
        notification = mBuilder.build();
        startForeground(1, notification);
        if (prefs != null) {
            editor.putBoolean("Tracking", true);
            editor.apply();
        }

        // Instantiate a AmazonDynamoDBMapperClient
        /*final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();*/

        TrackerBackgroundWorker trackerBackgroundWorker = new TrackerBackgroundWorker();
        trackerBackgroundWorker.execute();

        locationItem = new LocationsDO();
    }

    class TrackerBackgroundWorker extends AsyncTask<String, Void, String> {
        ScanResult scanResult;

        @Override
        protected String doInBackground(String... strings) {
            /*final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName("ExampleSchool")
                    .withAttributesToGet("latitude")
                    .withAttributesToGet(("longitude"));
            scanResult = dynamoDBClient.scan(scanRequest);*/
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        Boolean isAtEvent = false;
                        LatLng mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        try {
                            for (Map<String, AttributeValue> item : scanResult.getItems()) {
                                    Log.d("Test", item.get("latitude").getN() + " " + item.get("longitude").getN());
                                    if (((Double.parseDouble(item.get("longitude").getN()) - mCurrentLocation.longitude) < .001)
                                            && ((Double.parseDouble(item.get("longitude").getN()) - mCurrentLocation.longitude) > -.001)
                                            && ((Double.parseDouble(item.get("latitude").getN()) - mCurrentLocation.latitude) < .001)
                                            && ((Double.parseDouble(item.get("latitude").getN()) - mCurrentLocation.latitude) > -.001)) {
                                        Intent intentTwo = new Intent("Success");
                                        i = 0;
                                        if (!timerStarted) {
                                            startTime = SystemClock.uptimeMillis();
                                            handler.post(updateTimer);
                                        }
                                        if (timerPaused) {
                                            startTime = SystemClock.uptimeMillis();
                                            handler.post(updateTimer);
                                            timerPaused = false;
                                        }
                                        intentTwo.putExtra("start time", startTime);
                                        isAtEvent = true;
                                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcastSync(intentTwo);
                                    }
                            } if (!isAtEvent) {
                                Intent intent = new Intent("Fail");
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcastSync(intent);
                                if (i == 0) {
                                    timerPaused = true;
                                }
                                i++;
                            }
                        }
                        catch (Exception E){
                            Log.d("Tracker", "Error, retrying...");
                        }
                    }
                }
            };

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
            createLocationRequest();
            startLocationUpdates();
        }
    }

    /**
     * Create request to update location.
     */
    protected void createLocationRequest() {
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        mRequestingLocationUpdates = true;
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            timerStarted = true;
            if (!timerPaused) {
                timeLeftInMilliseconds = SystemClock.uptimeMillis() - startTime;
                updateTime = timeSwapBuff + timeLeftInMilliseconds;
                seconds = (int) (updateTime / 1000);
                minutes = seconds / 60;
                hours = minutes / 60;

                handler.post(this);
            }
            else {
                editor = getSharedPreferences(MY_PREFS, MODE_PRIVATE).edit();
                if (prefs != null)
                    points = prefs.getInt("points", 0) + minutes + seconds;
                editor.putInt("points", points);
                editor.apply();
            }
        }
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification";
            String description = "Start";
            int importance = NotificationManager.IMPORTANCE_NONE;
            NotificationChannel channel = new NotificationChannel("com.example.isaacenlow.time4dealz", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @DynamoDBTable(tableName = "ExampleSchoolUserAccounts")
    public class SavePoints {
        private String userName = "";
        private int utilPoints = 0;

        @DynamoDBAttribute(attributeName = "userPoints")
        public int getPoints() {
            return utilPoints;
        }

        public void setPoints(int points) {
            this.utilPoints = points;
        }

        @DynamoDBHashKey(attributeName = "userID")
        @DynamoDBAttribute(attributeName = "userID")
        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        mFusedLocationClient.flushLocations();
        Intent intent = new Intent("Fail");
        LocalBroadcastManager.getInstance(this).sendBroadcastSync(intent);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
        if (prefs != null) {
            points = prefs.getInt("points", 0) + minutes + seconds;
            editor.putInt("points", points);
            editor.putBoolean("tracking", false);
            editor.apply();
        }
        handler.removeCallbacks(updateTimer);
        final String userName = prefs.getString("username", "");
        final SavePoints accountUtil = new SavePoints();
        try {
            Thread.sleep(500);
        } catch (Exception e){}
        accountUtil.setPoints(prefs.getInt("points", 0));
        accountUtil.setUserName(userName);
        new Thread(new Runnable() {
            @Override
            public void run() {
                dynamoDBMapper.save(accountUtil);
            }
        }).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}