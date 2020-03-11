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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
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


public class Tracker extends Service implements GoogleApiClient.OnConnectionFailedListener {
    private LocationCallback mLocationCallback;
    private LatLng lebanon = new LatLng(36.896034, -82.068117);
    private LatLng sterlingCoord = new LatLng(39.040899, -77.037234);
    private String sterling = "46194 walpole terr, sterling, va";
    private String gdit = "15036 Conference Center Dr, Chantilly, VA 20151";
    private LocationRequest mLocationRequest = new LocationRequest();
    private boolean mRequestingLocationUpdates;
    private FusedLocationProviderClient mFusedLocationClient;
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
    DynamoDBMapper dynamoDBMapper;
    ArrayList<LocationAddress> locationAddressList = new ArrayList<>();
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

        TrackerBackgroundWorker trackerBackgroundWorker = new TrackerBackgroundWorker();
        trackerBackgroundWorker.execute();

        locationItem = new LocationsDO();
        locationAddressList.add(getAddressFromLocation(getApplicationContext(), sterling));
        locationAddressList.add(getAddressFromLocation(getApplicationContext(), gdit));
    }

    public void instantiateAWSDBClient() {
        // Instantiate a AmazonDynamoDBMapperClient
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentials());
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder().dynamoDBClient(dynamoDBClient).build();
    }

    class TrackerBackgroundWorker extends AsyncTask<String, Void, String> {
        ScanResult scanResult;

        @Override
        protected String doInBackground(String... strings) {
            final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentials());
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName("ExampleSchool")
                    .withAttributesToGet("latitude")
                    .withAttributesToGet(("longitude"));
            scanResult = dynamoDBClient.scan(scanRequest);
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
                        for (int j = 0; j < currentEvents.size(); j++) {
                            locationAddressList.add(getAddressFromLocation(getApplicationContext(), currentEvents.get(j).location));
                        }
                    }
                    current.add(Calendar.DAY_OF_MONTH, 1);
                }
            } catch (IOException e) {
                Log.e("Unable to retrieve data", e.getLocalizedMessage());
            }
            Log.e("Element List size", String.valueOf(elementList.size()));
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
                            for (LocationAddress locationAddress : locationAddressList) {  //Map<String, AttributeValue> item : scanResult.getItems()
                                    //Log.d("Test", item.get("latitude").getN() + " " + item.get("longitude").getN());
                                    if (((locationAddress.getLongitude() - mCurrentLocation.longitude) < .001)
                                            && ((locationAddress.getLongitude() - mCurrentLocation.longitude) > -.001)
                                            && ((locationAddress.getLatitude() - mCurrentLocation.latitude) < .001)
                                            && ((locationAddress.getLatitude() - mCurrentLocation.latitude) > -.001)) {
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
        final String userName = prefs.getString("userId", "");
        final SavePoints accountUtil = new SavePoints();
        try {
            Thread.sleep(500);
        } catch (Exception e){}
        accountUtil.setPoints(prefs.getInt("points", 0));
        accountUtil.setUserName(userName);
        // Instantiate a AmazonDynamoDBMapperClient
        new Thread(new Runnable() {
            @Override
            public void run() {
                instantiateAWSDBClient();
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