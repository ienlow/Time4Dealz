package com.example.isaacenlow.time4dealz;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TeamSchedules extends AppCompatActivity {
    String sport = "Basketball";
    ArrayList<Teams> teams = new ArrayList<>();
    Teams one;
    String done;
    BackgroundWorker backgroundWorker;
    //ProgressBar progressBar = findViewById(R.id.progressBar);

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_schedules);

        display();
    }

    public void display() {
        backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute("");
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (!backgroundWorker.finished()) {
            //progressBar.setVisibility(View.VISIBLE);
        }
        teams = backgroundWorker.getTeams();
        //done = teams.get(1).getPlace();
        ListView listView = findViewById(R.id.results);
        TeamAdapter adapter = new TeamAdapter(getApplicationContext(), R.layout.adapter_layout, teams);
        listView.setAdapter(adapter);
    }
}
