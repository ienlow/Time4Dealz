package com.example.isaacenlow.time4dealz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

public class Settings extends AppCompatActivity {

    private Switch locationServices;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        locationServices = findViewById(R.id.location_services);
        prefs = getSharedPreferences("MY_PREFS", 0);
        editor = prefs.edit();
        locationServices.setChecked(prefs.getBoolean("checked", false));

        locationServices.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Intent intent;
                if (!compoundButton.isChecked()) {
                    intent = new Intent(getApplicationContext(), Tracker.class);
                    editor.putBoolean("tracking", false); // set tracking to false
                    editor.putBoolean("enabled", false); // button enabled is false
                    //editor.putLong("timestarted", 0);
                    editor.putBoolean("timer started", false);
                    editor.apply();
                    stopService(intent); // stop tracking
                }
                else {
                    intent = new Intent(getApplicationContext(), Tracker.class);
                    editor.putBoolean("tracking", true); // set tracking to true
                    editor.putBoolean("enabled", true); // button enabled is true
                    editor.apply();
                    startForegroundService(intent); // start tracking
                }
                editor.putBoolean("checked", b);
                editor.apply();
            }
        });
    }

    public void viewLocation(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}
