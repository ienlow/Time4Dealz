package com.example.isaacenlow.time4dealz;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.util.prefs.Preferences;

public class Rewards extends AppCompatActivity {
    ProgressBar progressBar;
    SharedPreferences preferences;
    Button redeemReward500, redeemReward1000;
    public static final String MY_PREFS = "MyPrefs";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rewards);
        redeemReward500 = findViewById(R.id.redeemReward500);
        redeemReward1000 = findViewById(R.id.redeemReward1000);
        progressBar = findViewById(R.id.rewardsProgress);
        preferences = getSharedPreferences(MY_PREFS, 0);
        int points = preferences.getInt("points", 0);
        progressBar.setProgress(preferences.getInt("points", 0));

        if (points >= R.integer.Reward500) {
            redeemReward500.setVisibility(View.VISIBLE);
        }
        else if (points >= R.integer.Reward1000) {
            redeemReward1000.setVisibility(View.VISIBLE);
        }
    }
}
