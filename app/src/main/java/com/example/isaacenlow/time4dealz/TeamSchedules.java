package com.example.isaacenlow.time4dealz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.JsonReader;
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
import com.amazonaws.util.IOUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class TeamSchedules extends AppCompatActivity {
    ArrayList<Event> teams, currentEvents, upcomingEvents, orderedTeams;
    BackgroundWorker backgroundWorker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_schedules);
        teams = new ArrayList<>();
        currentEvents = new ArrayList<>();
        upcomingEvents = new ArrayList<>();
        orderedTeams = new ArrayList<>();
        backgroundWorker = new BackgroundWorker();
        backgroundWorker.execute();
    }

    public static class Page {
        String title;
    }

    private class BackgroundWorker extends AsyncTask<String, Void, String> {
        ArrayList<Event> teams = new ArrayList<>();
        DynamoDBMapper dynamoDBMapper;

        @SuppressLint("SimpleDateFormat")
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

            /*ScanRequest scanRequest = new ScanRequest()
                    .withTableName("ExampleSchool")
                    .withAttributesToGet("indexName")
                    .withAttributesToGet("sport")
                    .withAttributesToGet("location")
                    .withAttributesToGet("opponent")
                    .withAttributesToGet("time")
                    .withAttributesToGet("date")
                    .withAttributesToGet("url")
                    .withAttributesToGet("imageUrl");
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
                while(current.before(future)) {
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
                                    "test",
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
                            orderedTeams.add(one);
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
            Log.d("teams list", orderedTeams.size() + "");
            Log.d("current events list", currentEvents.size() + "");
            Log.d("upcoming events list", upcomingEvents.size() + "");
            if (currentEvents.size() > 0) {
                orderedTeams.get(0).type = 1;
            }
            if (upcomingEvents.size() > 0) {
                orderedTeams.get(currentEvents.size()).type = 2;
            }
            ListView listView = findViewById(R.id.results);;
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

    private String readURL(String webservice) throws IOException
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
}
