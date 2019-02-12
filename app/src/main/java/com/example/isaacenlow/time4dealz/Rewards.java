package com.example.isaacenlow.time4dealz;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class Rewards extends AppCompatActivity {
    SharedPreferences preferences;
    TextView rewardsPoints;
    public static final String MY_PREFS = "MyPrefs";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rewards);
        preferences = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        int points = preferences.getInt("points", 0);
        rewardsPoints = findViewById(R.id.rewards_points);
        rewardsPoints.setText(String.valueOf(points));
        ArrayList<RewardItem> list = new ArrayList<>();
        list.add(new RewardItem("10% OFF", 50));
        list.add(new RewardItem("20% OFF 1 Item", 100));
        list.add(new RewardItem("Buy One Get One 50% OFF", 200));
        list.add(new RewardItem("30% OFF Radford University hat", 300));
        list.add(new RewardItem("15% OFF One Item", 400));
        list.add(new RewardItem("EVERYTHING'S FREE", 1000));
        ListView listView = findViewById(R.id.rewards_list);
        RewardsAdapter adapter = new RewardsAdapter(this, R.layout.rewards_adapter, list, points);
        listView.setAdapter(adapter);
    }
}