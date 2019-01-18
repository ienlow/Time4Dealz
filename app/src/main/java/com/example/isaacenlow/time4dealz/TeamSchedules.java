package com.example.isaacenlow.time4dealz;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewOutlineProvider;
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
import java.util.Map;

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
            tmp = teams;
            int size = tmp.size();
            for (int i = 0; i < size / 2; i++) {
                final Event event = tmp.get(i);
                tmp.set(i, tmp.get(size - i - 1)); // swap
                tmp.set(size - i - 1, event); // swap
            }
            ListView listView = findViewById(R.id.results);
            TeamAdapter adapter = new TeamAdapter(getApplicationContext(), R.layout.adapter_layout, tmp);
            listView.setAdapter(adapter);
            Log.d("list", teams.size() + "");
        }
    }
}
