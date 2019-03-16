package com.example.isaacenlow.time4dealz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.ArrayList;
import java.util.Map;

public class Rewards extends AppCompatActivity {
    SharedPreferences preferences;
    TextView rewardsPoints;
    int points = 0;
    public static final String MY_PREFS = "MyPrefs";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rewards);
        preferences = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        points = preferences.getInt("points", 0);
        rewardsPoints = findViewById(R.id.rewards_points);
        rewardsPoints.setText(String.valueOf(points));
        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute();
    }

    /**
     * Get the rewards from database to load into list view adapter
     */
    class BackgroundWorker extends AsyncTask<String, Void, String> {
        ArrayList<RewardItem> list = new ArrayList<>();
        Context context;

        BackgroundWorker(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {
            final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName("ExampleSchoolRewards")
                    .withAttributesToGet("reward_name")
                    .withAttributesToGet("points");
            ScanResult scanResult = dynamoDBClient.scan(scanRequest);
            for (Map<String, AttributeValue> item : scanResult.getItems()) {
                list.add(new RewardItem(item.get("reward_name").getS(), Integer.valueOf(item.get("points").getN())));
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            for (int i = 0; i < list.size()-1; i++) {
                for (int j = 0; j < list.size()-i-1; j++) {
                    if (list.get(j).points > list.get(j+1).points) {
                        final RewardItem rewardItem = list.get(j);
                        list.set(j, list.get(j+1));
                        list.set(j+1, rewardItem);
                    }
                }
            }
            ListView listView = findViewById(R.id.rewards_list);
            RewardsAdapter adapter = new RewardsAdapter(context, R.layout.rewards_adapter, list, points);
            listView.setAdapter(adapter);
        }
    }
}