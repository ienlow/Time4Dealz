package com.example.isaacenlow.time4dealz;

import android.content.Context;
import android.os.AsyncTask;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.ArrayList;


public class TrackerBackgroundWorker extends AsyncTask<String, Void, String> {
    Context context;
    ScanResult scanResult;
    private DynamoDBMapper dynamoDBMapper;
    ArrayList<Event> teams = new ArrayList<>();
    boolean finished = false;
    TrackerBackgroundWorker (Context ctx) {
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
                .withAttributesToGet("active")
                .withAttributesToGet("latitude")
                .withAttributesToGet(("longitude"));
        scanResult = dynamoDBClient.scan(scanRequest);

        finished = true;
        return null;
    }

    public ScanResult getItems() {
        return scanResult;
    }

    public boolean isFinished() {
        return finished;
    }
}
