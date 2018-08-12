package com.example.isaacenlow.time4dealz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Profile extends AppCompatActivity {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Button getPoints;
    private TextView displayPoints;
    private int points = 0;
    private Button trackingButton;
    public static final String MY_PREFS = "MyPrefs";

   @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.profile);

       prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
       editor = prefs.edit();
       trackingButton = findViewById(R.id.tracking);
       if (prefs.getBoolean("tracking", false))
           trackingButton.setText("Disable Tracking");
       else {
           trackingButton.setText("Enable Tracking");
       }
       trackingButton.setOnClickListener(new View.OnClickListener() {
           @RequiresApi(api = Build.VERSION_CODES.O)
           @Override
           public void onClick(View v) {
               Intent intent = new Intent(getApplicationContext(), Tracker.class);
               if (prefs.getBoolean("tracking", false)) {
                   trackingButton.setText("Enable Tracking");
                   editor.putBoolean("tracking", false);
                   editor.apply();
                   stopService(intent);
               }
               else {
                   trackingButton.setText("Disable Tracking");
                   editor.putBoolean("tracking", true);
                   editor.apply();
                   startForegroundService(intent);
               }
           }
       });

       if (prefs != null)
           points = prefs.getInt("points", 0);
       displayPoints = (TextView) findViewById(R.id.displayPoints);
       displayPoints.setText(String.valueOf(points));
   }

   /*public void resetPoints(View view) {
       setContentView(R.layout.profile);
       displayPoints = (TextView) findViewById(R.id.displayPoints);
       if (prefs != null) {
           editor.putInt("points", 0);
           editor.apply();
       }
       points = prefs.getInt("points", 0);
       displayPoints.setText(String.valueOf(points));
   }*/

    public void viewLocation(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

   public void getPoints(View view) {
       Toast.makeText(this, String.valueOf(points), Toast.LENGTH_SHORT).show();
   }
}
