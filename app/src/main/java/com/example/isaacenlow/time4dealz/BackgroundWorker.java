package com.example.isaacenlow.time4dealz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.provider.Settings;
import android.system.Os;
import android.util.Log;
import android.widget.ImageView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class BackgroundWorker extends AsyncTask<String, Void, String> {
    Context context;
    private DynamoDBMapper dynamoDBMapper;
    ArrayList<Teams> teams = new ArrayList<>();
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
                .withAttributesToGet("sport")
                .withAttributesToGet("location")
                .withAttributesToGet("playing_against")
                .withAttributesToGet("time")
                .withAttributesToGet("date")
                .withAttributesToGet("URL");
        ScanResult result = dynamoDBClient.scan(scanRequest);
        for (Map<String, AttributeValue> item : result.getItems()) {
            try {
                // https://bumptech.github.io/glide/doc/getting-started.html#background-threads
                //String url = item.get("URL").getS();
                //try {
               // } catch(Exception e) {Log.d("error", e.toString());}
                Teams one = new Teams("            " + (item.get("sport").getS()) + "                 " + (item.get("date").getS()), "\n                  " + (item.get("playing_against").getS())
                        + "\n                  " + item.get("location").getS()
                        + "\n                  " + item.get("time").getS(), null);
                teams.add(one);
                //Glide.with(context).clear(futureTarget);
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

    public ArrayList<Teams> getTeams() {
        return teams;
    }
}
