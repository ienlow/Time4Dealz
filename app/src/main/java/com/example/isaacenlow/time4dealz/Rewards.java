package com.example.isaacenlow.time4dealz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Rewards extends AppCompatActivity {
    SharedPreferences preferences;
    TextView rewardsPoints;
    int points = 0;
    public static final String MY_PREFS = "MyPrefs";
    ImageView imageView;
    ListView listView;
    DynamoDBMapper dynamoDBMapper;
    InsertNewRewardExp insertNewRewardExp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rewards);
        imageView = findViewById(R.id.rewards_logo);
        preferences = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        points = preferences.getInt("points", 0);
        rewardsPoints = findViewById(R.id.rewards_points);
        rewardsPoints.setText(String.valueOf(points));
        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute();
        Map<String,String> expressionAttributesNames = new HashMap<>();
        expressionAttributesNames.put("#email","email");

        Map<String,AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":emailValue",new AttributeValue().withS(email));
        QueryRequest queryRequest = new QueryRequest()
                .withTableName("ExampleSchoolUserRewards")
                .withKeyConditionExpression()
    }

    @DynamoDBTable(tableName = "ExampleSchoolUserRewards")
    public static class InsertNewRewardExp {

        @DynamoDBHashKey(attributeName = "userId")
        @DynamoDBAttribute(attributeName = "userId")
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        @DynamoDBAttribute(attributeName = "rewardName")
        public String getRewardName() {
            return rewardName;
        }

        public void setRewardId(String rewardName) {
            this.rewardName = rewardName;
        }

        @DynamoDBAttribute(attributeName = "expDt")
        public Long getExpDt() {
            return expDt;
        }

        public void setExpDt(Long expDt) {
            this.expDt = expDt;
        }

        String userId;
        String rewardName;
        Long expDt;
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
            final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentials());
            DynamoDB dynamoDB
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
            listView = findViewById(R.id.rewards_list);
            String userName = preferences.getString("userId", "");
            RewardsAdapter adapter = new RewardsAdapter(context, R.layout.rewards_adapter, list, points, userName);
            listView.setAdapter(adapter);
        }
    }

    public void insertItem(View view) {
        Log.i("View Value: ", String.valueOf(view.getId()));

    }
}