package com.example.isaacenlow.time4dealz;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    TextView[] textViews;
    boolean tracking;
    private ImageView slot1;
    public static final String MY_PREFS = "MyPrefs";
    //Radford long = -80.5764477 lat = 37.1318
    //Sterling long = -77.405630 lat = 39.037318
    //Dedmon long = -80.5416 lat = 37.1385

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        editor = prefs.edit();
        timerText = findViewById(R.id.timer);
        handler = new Handler();
        textViews = new TextView[3];
        profileButton = findViewById(R.id.profileButton);
        Glide
                .with(this)
                .load("https://s3.amazonaws.com/timedealz-deployments-mobilehub-204377156/Icons/20881984_1283029611819396_6052734634897167129_n+(2).jpg")
                .apply(RequestOptions.circleCropTransform())
                .into(profileButton);
        timerStarted = prefs.getBoolean("timer started", false);
        pointsText = findViewById(R.id.pointsEarned);
        displayPoints = findViewById(R.id.points);
        displayPoints.setText(String.valueOf(prefs.getInt("points", 0)));
        //Log.d("checked", setTracking(); ? "true" : "false");
        BackgroundWorker backgroundWorker = new BackgroundWorker();
        backgroundWorker.execute();

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("Success")) {
                    i = 0;
                    if (!timerStarted) {
                        if (prefs.getBoolean("timer started", false)) {
                            startTime = prefs.getLong("timestarted", 0);
                            Log.d("start time", String.valueOf(startTime));
                        }
                        else {
                            startTime = SystemClock.uptimeMillis();
                        }
                        handler.post(updateTimer);
                        editor.putBoolean("timer started", true);
                        editor.apply();
                    }
                    if (timerPaused) {
                        startTime = SystemClock.uptimeMillis();
                        handler.post(updateTimer);
                        timerPaused = false;
                    }
                    timerText.setVisibility(View.VISIBLE);
                }
                else if (intent.getAction().equals("Fail")) {
                    if (i == 0) {
                        timerText.setText("00:00:00");
                        timerPaused = true;
                        handler.removeCallbacks(updateTimer);
                        timerText.setVisibility(View.INVISIBLE);
                        editor.putBoolean("timer started", false);
                        editor.apply();
                    }
                    i++;
                }
                else if (intent.getAction().equals("Logout")) {
                    finish();
                }
            }
        };
    }

    private class BackgroundWorker extends AsyncTask<String, Void, String> {
        ArrayList<Event> teams = new ArrayList<>();

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
                    Event one = new Event("            " + (item.get("sport").getS()) + "                 " + (item.get("date").getS()), "\n                  " + (item.get("playing_against").getS())
                            + "\n                  " + item.get("location").getS()
                            + "\n                  " + item.get("time").getS(), item.get("URL").getS());
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
            RecyclerView myView = findViewById(R.id.recycler1);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            adapter = new MainTeamAdapter(getApplicationContext(), teams);
            myView.setLayoutManager(layoutManager);
            myView.setAdapter(adapter);
            Log.d("list", teams.size() + "");
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

                String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

                handler.post(this);
                timerText.setText(timeLeftFormatted);
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
        if (prefs.getBoolean("timer started", false)) {
            startTime = prefs.getLong("timestarted", 0);
            Log.d("start time", String.valueOf(startTime));
            handler.post(updateTimer);
        }
        pointsText = findViewById(R.id.pointsEarned);
        displayPoints.setText(String.valueOf(prefs.getInt("points", 0)));
    }

    protected void onPause () {
        super.onPause();
        editor.putLong("timestarted", startTime);
        editor.apply();
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
        editor.putLong("timestarted", startTime);
        editor.apply();
        handler.removeCallbacks(updateTimer);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
        Log.d("Destroy", String.valueOf(startTime));
    }
}
