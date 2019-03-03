package com.example.isaacenlow.time4dealz;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.Map;

public class updateRewards extends AppCompatActivity {
    DynamoDBMapper dynamoDBMapper;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_rewards);
        AWSMobileClient.getInstance().initialize(this).execute();
        final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());


        dynamoDBMapper = DynamoDBMapper
                .builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();
    }

    @DynamoDBTable(tableName = "ExampleSchoolRewards")
    public class UpdateRewardsUtil {
        private String rewardName = "";
        private int points;
        private int itemId;

        @DynamoDBAttribute(attributeName = "reward_name")
        public String getRewardName() {
            return rewardName;
        }

        @DynamoDBAttribute(attributeName = "points")
        public int getPoints() {
            return points;
        }

        @DynamoDBHashKey(attributeName = "ItemId")
        @DynamoDBAttribute(attributeName = "ItemId")
        public int getItemId() {
            return itemId;
        }

        void setItemId(int itemId) {
            this.itemId = itemId;

        }

        void setRewardName(String rewardName) {
            this.rewardName = rewardName;
        }

        void setPoints(int points) {
            this.points = points;
        }
    }

    public class BackgroundWorker extends AsyncTask<String, Void, String> {
        int id = 1;

        @Override
        protected String doInBackground(String... strings) {
            final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName("ExampleSchoolRewards")
                    .withAttributesToGet("ItemId");
            ScanResult scanResult = dynamoDBClient.scan(scanRequest);
            int i = 1;
            while (i > 0) {
                i = 0;
                for (Map<String, AttributeValue> item : scanResult.getItems()) {
                    Log.d("items: ", String.valueOf(item.get("ItemId")));
                    if (Integer.parseInt(item.get("ItemId").getN()) == id) {
                        id += 1;
                        i++;
                    }
                }
            }
            Log.d("ID Read", String.valueOf(id));
                final UpdateRewardsUtil util = new UpdateRewardsUtil();
                TextView updateRewardName = findViewById(R.id.insert_reward_name);
                TextView updateRewardPoints = findViewById(R.id.insert_reward_points);
                util.setRewardName(updateRewardName.getText().toString());
                util.setPoints(Integer.parseInt(updateRewardPoints.getText().toString()));
                util.setItemId(id);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dynamoDBMapper.save(util);
                    }
                }).start();
            return null;
            }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(updateRewards.this, "Success", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void saveReward(View view) {
        BackgroundWorker backgroundWorker = new BackgroundWorker();
        backgroundWorker.execute();
    }
}
