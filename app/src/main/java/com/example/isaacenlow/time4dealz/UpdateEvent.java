package com.example.isaacenlow.time4dealz;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Scanner;

public class UpdateEvent extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_event);
        teams = new ArrayList<>();
        currentEvents = new ArrayList<>();
        upcomingEvents = new ArrayList<>();
        orderedTeams = new ArrayList<>();
        backgroundWorker = new BackgroundWorker();
        backgroundWorker.execute();
    }

    ArrayList<Event> teams, currentEvents, upcomingEvents, orderedTeams;
    BackgroundWorker backgroundWorker;

    private class BackgroundWorker extends AsyncTask<String, Void, String> {
        ArrayList<Event> teams = new ArrayList<>();
        DynamoDBMapper dynamoDBMapper;

        @Override
        protected String doInBackground(String... strings) {
            final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                    .build();

            /**Condition condition= new Condition()
             .withComparisonOperator(ComparisonOperator.GE)
             .withAttributeValueList(new AttributeValue().withS("indexName"));**/

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
                    int year = 0, month = 0, day = 0, hour = 0, minute = 0;
                    Calendar calendar = Calendar.getInstance();
                    Scanner scanner = new Scanner(item.get("date").getS());
                    scanner.useDelimiter("/");
                    if (scanner.hasNext()) {
                        month = Integer.valueOf(scanner.next()) - 1;
                        day = Integer.valueOf(scanner.next());
                        year = Integer.valueOf(scanner.next());
                        //Log.d("DATE_ADAPTER: ", String.valueOf(year) + " " + String.valueOf(month) + " " + String.valueOf(day));
                    }
                    Scanner scanner1 = new Scanner(item.get("time").getS());
                    scanner1.useDelimiter(":");
                    if (scanner1.hasNext()) {
                        hour = Integer.valueOf(scanner1.next());
                        minute = Integer.valueOf(scanner1.next());
                    }
                    calendar.set(year, month, day, hour, minute);
                    // default type is 0
                    Event one = new Event(
                            item.get("sport").getS(),
                            item.get("date").getS(),
                            item.get("opponent").getS(),
                            item.get("location").getS(),
                            item.get("time").getS(),
                            item.get("url").getS(),
                            null,//item.get("imageUrl").getS(),
                            calendar, 0);
                    teams.add(one);
                    scanner.close();
                    scanner1.close();
                    //Log.d("Item", one.getPlace());
                } catch (Exception e) {
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
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
                    orderedTeams.add(teams.get(i));
                    currentEvents.add(teams.get(i));
                } else if (calendar1.after(calendar)) {
                    orderedTeams.add(teams.get(i));
                    upcomingEvents.add(teams.get(i));
                }
            }
            //Log.d("teams list", orderedTeams.size() + "");
            //Log.d("current events list", currentEvents.size() + "");
            //Log.d("upcoming events list", upcomingEvents.size() + "");
            if (orderedTeams.size() > 0 ) {
                orderedTeams.get(0).type = 0;
            }

            ListView listView = findViewById(R.id.AdminResults);
            TeamAdapter adapter = new TeamAdapter(getApplicationContext(), R.layout.team_schedules_adapter, orderedTeams);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getApplicationContext(), CurrentGame.class);
                    intent.putExtra("info", teams.get(i).sport);
                    startActivity(intent);
                }
            });
        }
    }


    public void AddEvent(View view) {
        Intent intent = new Intent(this, CreateEvent.class);
        startActivity(intent);
    }
}
