package com.example.isaacenlow.time4dealz;

import android.content.SharedPreferences;
import android.graphics.ColorFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.prefs.Preferences;

public class Rewards extends AppCompatActivity {
    ProgressBar progressBar500, progressBar1000;
    SharedPreferences preferences;
    Button redeemReward500, redeemReward1000;
    TextView rewardsPoints;
    public static final String MY_PREFS = "MyPrefs";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rewards);
        redeemReward500 = findViewById(R.id.redeemReward500);
        redeemReward1000 = findViewById(R.id.redeemReward1000);
        progressBar500 = findViewById(R.id.rewardsProgress500);
        progressBar1000 = findViewById(R.id.rewardsProgress1000);
        preferences = getSharedPreferences(MY_PREFS, 0);
        int points = preferences.getInt("points", 0);
        progressBar500.setProgress(points);
        progressBar1000.setProgress(points);
        rewardsPoints = findViewById(R.id.rewards_points);
        rewardsPoints.setText(String.valueOf(points));

        if (points >= 20) {
            redeemReward500.setVisibility(View.VISIBLE);
        }
        if (points >= 100) {
            redeemReward1000.setVisibility(View.VISIBLE);
        }
    }
}
