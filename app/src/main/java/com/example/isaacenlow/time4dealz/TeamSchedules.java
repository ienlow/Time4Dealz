package com.example.isaacenlow.time4dealz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.util.DateUtils;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static com.example.isaacenlow.time4dealz.LoginScreen.MY_PREFS;

public class TeamSchedules extends AppCompatActivity {
    ArrayList<Event> teams, tmp;
    Event one;
    String done;
    BackgroundWorker backgroundWorker;
    //ProgressBar progressBar = findViewById(R.id.progressBar);

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_schedules);
        tmp = new ArrayList<>();
        teams = new ArrayList<>();
        backgroundWorker = new BackgroundWorker();
        backgroundWorker.execute();
    }

    private class BackgroundWorker extends AsyncTask<String, Void, String> {
        ArrayList<Event> teams = new ArrayList<>();
        ArrayList<String> dates = new ArrayList();
        DynamoDBMapper dynamoDBMapper;

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
                            calendar);
                    teams.add(one);
                    scanner.close();
                    dates.add(item.get("date").getS());
                    //Log.d("Item", one.getPlace());
                } catch (Exception e) {
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ArrayList<Calendar> calendars = new ArrayList<>();
            tmp = teams;
            int size = tmp.size();
            Calendar calendar = Calendar.getInstance();
            Log.d("Calendar ", calendar.toString());
            int year = 0;
            int month = 0;
            int day = 0;
            for (int i = 0; i < dates.size(); i++) {
                Scanner scanner = new Scanner(dates.get(i));
                scanner.useDelimiter("/");
                if (scanner.hasNext()) {
                    month = Integer.valueOf(scanner.next());
                    day = Integer.valueOf(scanner.next());
                    year = Integer.valueOf(scanner.next());
                    //Log.d("DATE: ", String.valueOf(year) + " " + String.valueOf(month) + " " + String.valueOf(day));
                }
                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(year, month, day);
                calendars.add(calendar1);
                //Log.d("DATE INDEX: " + i, " year: " + year + " month: " + month + " day: " + day);
            }
            /*for (int i = 0; i < calendars.size(); i++) {
                for (int j = 0; j < calendars.size()-i-1; j++) {
                    if (calendars.get(j).before(calendars.get(j+1))) {
                        final Calendar tmpCal = calendars.get(j);
                        calendars.set(j, calendars.get(j+1));
                        calendars.set(j+1, tmpCal);
                    }
                }
            }*/

            Calendar today = Calendar.getInstance();
            for (int i = 0; i < teams.size(); i++) {
                for (int j = 0; j < teams.size()-i-1; j++) {
                    int month1 = 0, month2 = 0, year1 = 0, year2 = 0, day1 = 0, day2 = 0;
                    Scanner scanner = new Scanner(teams.get(j).date);
                    scanner.useDelimiter("/");
                    if (scanner.hasNext()) {
                        month1 = Integer.valueOf(scanner.next());
                        day1 = Integer.valueOf(scanner.next());
                        year1 = Integer.valueOf(scanner.next());
                        Log.d("DATE1: ", String.valueOf(year1) + " " + String.valueOf(month1) + " " + String.valueOf(day1));
                    }
                    scanner = new Scanner(teams.get(j+1).date);
                    scanner.useDelimiter("/");
                    if (scanner.hasNext()) {
                        month2 = Integer.valueOf(scanner.next());
                        day2 = Integer.valueOf(scanner.next());
                        year2 = Integer.valueOf(scanner.next());
                        Log.d("DATE2: ", String.valueOf(year2) + " " + String.valueOf(month2) + " " + String.valueOf(day2));
                    }
                    scanner.close();
                    Calendar calendar1 = Calendar.getInstance();
                    Calendar calendar2 = Calendar.getInstance();
                    calendar1.set(year1, month1, day1);
                    calendar2.set(year2, month2, day2);
                    if (calendar1.after(calendar2)) {
                        final Event tmpCal = teams.get(j);
                        teams.set(j, teams.get(j+1));
                        teams.set(j+1, tmpCal);
                    }
                }
            }
            for (int i = 0; i < calendars.size(); i++) {
                Log.d("ORDERED DATE: " + i, teams.get(i).getDate());
            }
            /*
            for (int i = 0; i < size / 2; i++) {
                final Event event = tmp.get(i);
                tmp.set(i, tmp.get(size - i - 1)); // swap
                tmp.set(size - i - 1, event); // swap
            }*/

            ListView listView = findViewById(R.id.results);
            SharedPreferences preferences = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("show upcoming", true);
            editor.apply();
            TeamAdapter adapter = new TeamAdapter(getApplicationContext(), R.layout.adapter_layout, teams, preferences, editor);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getApplicationContext(), CurrentGame.class);
                    intent.putExtra("info", tmp.get(i).sport);
                    startActivity(intent);
                }
            });
            editor.putBoolean("show upcoming", true);
            editor.apply();
            Log.d("list", teams.size() + "");
        }
    }
}
