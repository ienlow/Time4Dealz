package com.example.isaacenlow.time4dealz;

import android.content.Context;
import android.os.AsyncTask;

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

public class BackgroundWorker extends AsyncTask<String, Void, String> {
    Context context;
    private DynamoDBMapper dynamoDBMapper;
    ArrayList<Event> teams = new ArrayList<>();
    String categories[];
    String info = "Dedmon Center";
    boolean finished = false;
    BackgroundWorker (Context ctx) {
        context = ctx;
    }

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
        finished = true;
        return "finished";
    }

    public boolean finished() {
        return finished;
    }

    public ArrayList<Event> getTeams() {
        return teams;
    }
}
