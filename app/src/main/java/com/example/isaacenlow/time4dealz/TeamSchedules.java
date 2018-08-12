package com.example.isaacenlow.time4dealz;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    String sport = "Basketball";
    ArrayList<Teams> teams = new ArrayList<Teams>();
    ArrayList<LocationItems> locations = new ArrayList<>();
    String info = "Dedmon Center";
    private DynamoDBMapper dynamoDBMapper;
    LocationsDO locationItem;
    //final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
    int i = 0;
    String readItem = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {



        // Instantiate a AmazonDynamoDBMapperClient
        final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_schedules);
        locationItem = new LocationsDO();


        new Thread(new Runnable() {
            @Override
            public void run() {
                ScanRequest scanRequest = new ScanRequest()
                        .withTableName("ExampleSchool")
                        .withAttributesToGet("category");
                ScanResult result = dynamoDBClient.scan(scanRequest);
                for (Map<String, AttributeValue> item : result.getItems()){
                    Teams one = new Teams(item.toString(), info);
                    teams.add(one);
                    Log.d("Item", one.getPlace());
                    //Scanner scanner = new Scanner(item.toString());
                    //while (scanner.hasNext()) {
                        //scanner.useDelimiter("");
                       // readItem = scanner.next();

                    //}
                }
            }
        }).start();


        // Instantiate a AmazonDynamoDBMapperClient

            try {
                if (locationItem.getCategory() != null) {
                    //Teams one = new Teams(locationItem.getCategory(), info);
                    //teams.add(one);
                }
                else {
                    for (i = 1; i < 3; i++) {
                        locationItem = new LocationsDO();
                        while (locationItem.getCategory() == null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    locationItem = dynamoDBMapper.load(LocationsDO.class, i);
                                }
                            }).start();
                        }
                        Teams one = new Teams(locationItem.getCategory(), info);
                        teams.add(one);
                    }
                }

            } catch (Exception e) {
            }
        ListView listView = (ListView) findViewById(R.id.results);
        TeamAdapter adapter = new TeamAdapter(this, R.layout.adapter_layout, teams);
        listView.setAdapter(adapter);
            readItem();
    }

    public void readItem() {

    }
}
