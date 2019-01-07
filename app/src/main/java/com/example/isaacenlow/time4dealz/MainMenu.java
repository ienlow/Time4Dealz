package com.example.isaacenlow.time4dealz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.Tag;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static java.security.AccessController.getContext;

/**
 * Created by isaac on 2/24/2018.
 */

public class MainMenu extends AppCompatActivity {
    private Intent intent;
    private int i;
    private DynamoDBMapper dynamoDBMapper;
    private Button button6;
    private CountDownTimer timer;
    private long timeLeftInMilliseconds = 0, startTime = 0, timeSwapBuff = 0, updateTime = 0;
    private TextView timerText, pointsText, displayPoints;
    HorizontalScrollView horizontalScrollView;
    private BroadcastReceiver br;
    boolean timerPaused = false, timerStarted = false;
    private int seconds, minutes, hours;
    private Handler handler;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private ImageButton profileButton;
    TableRow tableRow;
    ArrayList<TextView> viewArrayList;
    TextView[] textViews;
    private ImageView slot1;
    public static final String MY_PREFS = "MyPrefs";
    //Radford long = -80.5764477 lat = 37.1318
    //Sterling long = -77.405630 lat = 39.037318
    //Dedmon long = -80.5416 lat = 37.1385

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
        //textViews[2] = findViewById(R.id.textView4);
        timerStarted = prefs.getBoolean("timer started", false);
        pointsText = findViewById(R.id.pointsEarned);
        displayPoints = findViewById(R.id.points);
        displayPoints.setText(String.valueOf(prefs.getInt("points", 0)));
        //final boolean timerStarted = prefs.geBoolean("timer started", false);
        ArrayList list = new ArrayList<>();
        list.add("test1");
        list.add("test2");
        list.add("test2");
        list.add("test2");
        list.add("test2");
        RecyclerView myView = findViewById(R.id.recycler1);
        if (myView == null) {
            Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        myView.setLayoutManager(layoutManager);
        myView.setAdapter(new MainTeamAdapter(getApplicationContext(), list));

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("Success")) {
                    i = 0;
                    //progress.setVisibility(View.INVISIBLE);
                    if (!timerStarted) {
                        startTime = SystemClock.uptimeMillis();
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
                    // progress.setVisibility(View.INVISIBLE);
                    //Log.d("fail", "fail");
                }
                else if (intent.getAction().equals("Logout")) {
                    finish();
                }
            }
        };
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
        setContentView(R.layout.main_menu);
        ArrayList list = new ArrayList<>();
        list.add("test1");
        list.add("test2");
        list.add("test2");
        list.add("test2");
        list.add("test2");
        BackgroundWorker backgroundWorker = new BackgroundWorker(getApplication());
        backgroundWorker.execute("");
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RecyclerView myView = findViewById(R.id.recycler1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        myView.setLayoutManager(layoutManager);
        myView.setAdapter(new MainTeamAdapter(getApplicationContext(), backgroundWorker.getTeams()));
        timerStarted = prefs.getBoolean("timer started", false);
        //button6.setText("vs. Georgia Southern \n @ Dedmon Center");
        timerText = findViewById(R.id.timer);
        pointsText = findViewById(R.id.pointsEarned);
        displayPoints = findViewById(R.id.points);
        profileButton = findViewById(R.id.profileButton);
        Glide
                .with(this)
                .load("https://s3.amazonaws.com/timedealz-deployments-mobilehub-204377156/Icons/20881984_1283029611819396_6052734634897167129_n+(2).jpg")
                .apply(RequestOptions.circleCropTransform())
                .into(profileButton);
        displayPoints.setText(String.valueOf(prefs.getInt("points", 0)));
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("Success"));
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("Fail"));
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("Logout"));
        if (prefs.getBoolean("timer started", false)) {
            startTime = prefs.getLong("timestarted", 0);
            handler.post(updateTimer);
        }
    }

    protected void onPause () {
        super.onPause();
        editor.putLong("timestarted", startTime);
        editor.apply();

    }

    public void teamSchedules(View view) {
        setContentView(R.layout.loading);
        intent = new Intent(this, TeamSchedules.class);
        startActivity(intent);
    }

    public void Rewards(View view) {
        intent = new Intent(this, Rewards.class);
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
