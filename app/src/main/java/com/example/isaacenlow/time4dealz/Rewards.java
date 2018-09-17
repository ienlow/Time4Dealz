package com.example.isaacenlow.time4dealz;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import java.util.prefs.Preferences;

public class Rewards extends AppCompatActivity {
    ProgressBar progressBar;
    SharedPreferences preferences;
    public static final String MY_PREFS = "MyPrefs";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rewards);
        progressBar = findViewById(R.id.rewardsProgress);
        preferences = getSharedPreferences(MY_PREFS, 0);
        progressBar.setProgress(preferences.getInt("points", 0));
    }
}
