package com.example.isaacenlow.time4dealz;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telecom.Call;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import static java.security.AccessController.getContext;

/**
 * Created by isaac on 2/24/2018.
 */

public class MainMenu extends AppCompatActivity {
    private Intent intent;
    private int i;
    private DynamoDBMapper dynamoDBMapper;
    private long timeLeftInMilliseconds = 0, startTime = 0, timeSwapBuff = 0, updateTime = 0;
    private TextView timerText, pointsText, displayPoints;
    private BroadcastReceiver br;
    boolean timerPaused = false, timerStarted = false;
    private int seconds, minutes, hours;
    private Handler handler;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private ImageButton profileButton;
    private ImageView imageView;
    ArrayList<Event> orderedEvents = new ArrayList<>();
    ArrayList<Event> upcomingEvents = new ArrayList<>();
    ArrayList<Event> currentEvents = new ArrayList<>();
    double latitude[] = new double[]{}, longitude[] = new double[]{};
    public static final String MY_PREFS = "MyPrefs";
    AnimatorSet animatorSet;
    //Radford long = -80.5764477 lat = 37.1318
    //Sterling long = -77.405630 lat = 39.037318
    //Dedmon long = -80.5416 lat = 37.1385

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        animatorSet = new AnimatorSet();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        String userIdUrl = "";
        prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        editor = prefs.edit();
        if (!prefs.getString("imageURL", "").equals("null")) {
            userIdUrl = prefs.getString("imageURL", "");
        }
        else {
            userIdUrl = "https://s3.amazonaws.com/timedealz-deployments-mobilehub-204377156/Icons/radford+logo.png";
        }
        timerText = findViewById(R.id.timer);
        handler = new Handler();
        profileButton = findViewById(R.id.profileButton);
        Glide
                .with(this)
                .load(userIdUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(profileButton);
        pointsText = findViewById(R.id.pointsEarned);
        displayPoints = findViewById(R.id.points);
        displayPoints.setText(String.valueOf(prefs.getInt("points", 0)));
        timerStarted = prefs.getBoolean("timer started", false);
        //Log.d("checked", setTracking(); ? "true" : "false");
        BackgroundWorker backgroundWorker = new BackgroundWorker();
        backgroundWorker.execute();

        br = new BroadcastReceiver() {
            int successCount = 0;
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("Success")) {
                    if (successCount < 1) {
                        if (!timerStarted) {
                            editor.putBoolean("timer started", true);
                            editor.apply();
                            startTime = intent.getLongExtra("start time", SystemClock.uptimeMillis());
                            handler.post(updateTimer);
                        } else if (timerPaused) {
                            startTime = SystemClock.uptimeMillis();
                            handler.post(updateTimer);
                            timerPaused = false;
                        } else {
                            editor.putBoolean("timer started", true);
                            editor.apply();
                            startTime = intent.getLongExtra("start time", SystemClock.uptimeMillis());
                            handler.post(updateTimer);
                        }
                        Log.d("onReceive Time", String.valueOf(startTime));
                        successCount++;
                    }
                    timerText.setVisibility(View.VISIBLE);
                }
                else if (intent.getAction().equals("Fail")) {
                        successCount = 0;
                        timerText.setText("00:00:00");
                        timerPaused = true;
                        handler.removeCallbacks(updateTimer);
                        timerText.setVisibility(View.INVISIBLE);
                        editor.putBoolean("timer started", false);
                        editor.apply();
                }
                else if (intent.getAction().equals("Logout")) {
                    successCount = 0;
                    editor.putBoolean("timer started", false);
                    editor.apply();
                    finish();
                }
            }
        };
    }

    private class BackgroundWorker extends AsyncTask<String, Void, String> {
        ArrayList<Event> teams = new ArrayList<>();
        int i = 0;

        @Override
        protected String doInBackground(String... strings) {
            final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                    .build();
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName("ExampleSchool")
                    .withAttributesToGet("indexName")
                    .withAttributesToGet("sport")
                    .withAttributesToGet("location")
                    .withAttributesToGet("playing_against")
                    .withAttributesToGet("time")
                    .withAttributesToGet("date")
                    .withAttributesToGet("URL");
            ScanResult result = dynamoDBClient.scan(scanRequest);
            for (Map<String, AttributeValue> item : result.getItems()) {
                // Create new event for every item and format here
                try {
                    int year = 0, month = 0, day = 0;
                    Calendar calendar = Calendar.getInstance();
                    Scanner scanner = new Scanner(item.get("date").getS());
                    scanner.useDelimiter("/");
                    if (scanner.hasNext()) {
                        month = Integer.valueOf(scanner.next());
                        day = Integer.valueOf(scanner.next());
                        year = Integer.valueOf(scanner.next());
                        Log.d("DATE_ADAPTER: ", String.valueOf(year) + " " + String.valueOf(month) + " " + String.valueOf(day));
                    }
                    calendar.set(year, month, day);
                    Event one = new Event(
                            item.get("sport").getS(),
                            item.get("date").getS(),
                            item.get("playing_against").getS(),
                            item.get("location").getS(),
                            item.get("time").getS(),
                            item.get("URL").getS(),
                            calendar, 0);
                    teams.add(one);
                    //Log.d("Item", one.getPlace());
                } catch (Exception e) {
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            MainTeamAdapter adapter;
            Calendar calendar = Calendar.getInstance();
            DatePicker datePicker = new DatePicker(getApplicationContext());
            // order the events
            for (int i = 0; i < teams.size(); i++) {
                for (int j = 0; j < teams.size()-i-1; j++) {
                    int month1 = 0, month2 = 0, year1 = 0, year2 = 0, day1 = 0, day2 = 0, hour1 = 0, hour2 = 0, minute1 = 0, minute2 = 0;
                    Scanner scanner = new Scanner(teams.get(j).date);
                    Scanner scanner1 = new Scanner((teams.get(j).time));
                    scanner.useDelimiter("/");
                    scanner1.useDelimiter(":");
                    if (scanner.hasNext()) {
                        month1 = Integer.valueOf(scanner.next());
                        day1 = Integer.valueOf(scanner.next());
                        year1 = Integer.valueOf(scanner.next());
                    }
                    if (scanner1.hasNext()) {
                        hour1 = Integer.valueOf(scanner1.next());
                        minute1 = Integer.valueOf(scanner1.next());
                    }
                    scanner = new Scanner(teams.get(j+1).date);
                    scanner1 = new Scanner(teams.get(j+1).time);
                    scanner.useDelimiter("/");
                    scanner1.useDelimiter(":");
                    if (scanner.hasNext()) {
                        month2 = Integer.valueOf(scanner.next());
                        day2 = Integer.valueOf(scanner.next());
                        year2 = Integer.valueOf(scanner.next());
                    }
                    if (scanner1.hasNext()) {
                        hour2 = Integer.valueOf(scanner1.next());
                        minute2 = Integer.valueOf(scanner1.next());
                    }
                    scanner1.close();
                    scanner.close();
                    Calendar calendar1 = Calendar.getInstance();
                    Calendar calendar2 = Calendar.getInstance();
                    calendar1.set(year1, month1, day1, hour1, minute1);
                    calendar2.set(year2, month2, day2, hour2, minute2);
                    if (calendar1.after(calendar2)) {
                        final Event tmpCal = teams.get(j);
                        teams.set(j, teams.get(j+1));
                        teams.set(j+1, tmpCal);
                    }
                    //Log.d("DATE_ORDERED: ", String.valueOf(year1) + " " + String.valueOf(month1) + " " + String.valueOf(day1));
                }
            }

            for (int i = 0; i < teams.size(); i++) {
                int month = 0, year = 0, day = 0;
                Calendar calendar1 = Calendar.getInstance();
                Scanner scanner = new Scanner(teams.get(i).date);
                scanner.useDelimiter("/");
                if (scanner.hasNext()) {
                    month = Integer.valueOf(scanner.next()) - 1;
                    day = Integer.valueOf(scanner.next());
                    year = Integer.valueOf(scanner.next());
                    Log.d("DATE1: ", String.valueOf(year) + " " + String.valueOf(month) + " " + String.valueOf(day));
                }
                scanner.close();
                calendar1.set(year, month, day);
                if ((datePicker.getMonth() == month &&
                        datePicker.getYear() == year &&
                        datePicker.getDayOfMonth() == day)) {
                    orderedEvents.add(teams.get(i));
                } else if (calendar1.after(calendar)) {
                    orderedEvents.add(teams.get(i));
                }
            }
            final RecyclerView myView = findViewById(R.id.recycler1);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            adapter = new MainTeamAdapter(getApplicationContext(), orderedEvents);
            myView.setLayoutManager(layoutManager);
            myView.setAdapter(adapter);
            myView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = myView.getChildLayoutPosition(view);

                }
            });
        }
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
                seconds = seconds % 60;
                minutes = minutes % 60;

                String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

                handler.post(this);
                timerText.setText(timeLeftFormatted);
                //Log.d("timer", "COUNTING");
            }
        }
    };

    public void openProfile(View view) {
        intent = new Intent(this, Profile.class);
        startActivity(intent);
    }

    public void openSettings(View view) {
        intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    protected void onResume(){
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("Success"));
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("Fail"));
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("Logout"));
        pointsText = findViewById(R.id.pointsEarned);
        displayPoints.setText(String.valueOf(prefs.getInt("points", 0)));
    }

    protected void onPause () {
        super.onPause();
    }

    public void teamSchedules(View view) {
        intent = new Intent(this, TeamSchedules.class);
        startActivity(intent);
    }

    public void Rewards(View view) {
        intent = new Intent(this, Rewards.class);
        startActivity(intent);
    }

    public void teamRosters(View view) {
        intent = new Intent(this, TeamsRosters.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimer);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
    }

    @Override
    public void onBackPressed() {
        finish();
        moveTaskToBack(true);
    }
}
