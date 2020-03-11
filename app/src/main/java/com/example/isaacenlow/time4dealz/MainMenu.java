package com.example.isaacenlow.time4dealz;

import android.animation.AnimatorSet;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
//import android.support.annotation.RequiresApi;
//import android.support.v4.content.LocalBroadcastManager;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import static androidx.constraintlayout.widget.Constraints.TAG;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.UserStateListener;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

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
    ArrayList<String> locationAddressList = new ArrayList<>();
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
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect("https://calendar.radford.edu/view/monthif").userAgent("Mozilla").data("name", "jsoup").get();
                    //Log.d("html", document.getElementsByClass("lw_cal_spotlight_event").toString());
                    int x = 1;
                    for (Element element : document.select("h2")) {
                        Log.d("heading: " + x++, element.text());
                    }
                    //Log.d("html", document.select("h2").text());
                } catch (IOException e) {
                    Log.getStackTraceString(e);
                }
            }
        }).start();*/
        Intent notifications = new Intent(getApplicationContext(), MyFirebaseMessagingService.class);
        //startService(notifications);
        /*FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
                        Toast.makeText(MainMenu.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });*/
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

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private class BackgroundWorker extends AsyncTask<String, Void, String> {
        ArrayList<Event> teams = new ArrayList<>();
        int i = 0;

        @Override
        protected String doInBackground(String... strings) {
            /*final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
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
                    .withAttributesToGet("opponent")
                    .withAttributesToGet("time")
                    .withAttributesToGet("date")
                    .withAttributesToGet("url");
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
                            item.get("location").getS(),
                            item.get("time").getS(),
                            item.get("url").getS(),
                            null,//item.get("url").getS(),
                            calendar, 0);
                    teams.add(one);
                    //Log.d("Item", one.getPlace());
                } catch (Exception e) {
                }
            }*/
            List<Element> elementList = new ArrayList<>();
            try {
                final String json = "https://calendar.radford.edu/live/calendar/view/all?user_tz=America%2FDetroit&syntax=%3Cwidget%20type%3D%22events_calendar%22%3E%3Carg%20id%3D%22mini_cal_heat_map%22%3Etrue%3C%2Farg%3E%3Carg%20id%3D%22thumb_width%22%3E200%3C%2Farg%3E%3Carg%20id%3D%22thumb_height%22%3E200%3C%2Farg%3E%3Carg%20id%3D%22hide_repeats%22%3Etrue%3C%2Farg%3E%3Carg%20id%3D%22show_groups%22%3Etrue%3C%2Farg%3E%3Carg%20id%3D%22show_locations%22%3Efalse%3C%2Farg%3E%3Carg%20id%3D%22show_tags%22%3Etrue%3C%2Farg%3E%3Carg%20id%3D%22use_tag_classes%22%3Efalse%3C%2Farg%3E%3Carg%20id%3D%22search_all_events_only%22%3Etrue%3C%2Farg%3E%3Carg%20id%3D%22use_modular_templates%22%3Etrue%3C%2Farg%3E%3C%2Fwidget%3E";
                Gson gson = new GsonBuilder().setLenient().create();
                String trim = readURL(json).trim();
                JsonObject jsonObject = gson.fromJson(trim, JsonObject.class);
                System.out.println(jsonObject.get("events"));
                JsonObject jsonObject1 = jsonObject.get("events").getAsJsonObject();
                Calendar current = Calendar.getInstance();
                Calendar future = Calendar.getInstance();
                Calendar today = Calendar.getInstance();
                future.set(current.get(Calendar.YEAR), current.get(Calendar.MONTH) + 3, current.getActualMaximum(Calendar.DAY_OF_MONTH));
                System.out.println(new SimpleDateFormat("yyyyMMdd").format(current.getTime()));

                while(current.before(future) && teams.size() < 5) {
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
                            locationAddressList.add(jsonArray.get(i).getAsJsonObject().get("location").getAsString());
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
            MainTeamAdapter adapter;
            /*Calendar calendar = Calendar.getInstance();
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
            }*/
            RecyclerView myView = findViewById(R.id.recycler1);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            adapter = new MainTeamAdapter(getApplicationContext(), teams);
            myView.setLayoutManager(layoutManager);
            myView.setAdapter(adapter);
            myView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //int position = myView.getChildLayoutPosition(view);

                }
            });
        }
    }

    public static String readURL(String webservice) throws IOException
    {
        URL url = new URL(webservice);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuffer buffer = new StringBuffer();
        int read;
        char[] chars = new char[1024];
        while ((read = bufferedReader.read(chars)) != -1)
        {
            buffer.append(chars, 0, read);
        }
        return buffer.toString();
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
        intent.putStringArrayListExtra("LocationAddressList", locationAddressList);
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
}
