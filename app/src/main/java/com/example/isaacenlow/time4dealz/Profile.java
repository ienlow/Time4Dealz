package com.example.isaacenlow.time4dealz;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

public class Profile extends AppCompatActivity {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Button getPoints;
    private TextView displayPoints;
    private int points = 0;
    BroadcastReceiver br;
    ImageView imageView;
    private Button trackingButton;
    public static final String MY_PREFS = "MyPrefs";

   @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.profile);

       prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
       editor = prefs.edit();

       imageView = findViewById(R.id.image_profile);
       Glide
               .with(this)
               .load("https://s3.amazonaws.com/timedealz-deployments-mobilehub-204377156/Icons/20881984_1283029611819396_6052734634897167129_n+(2).jpg")
               .apply(RequestOptions.circleCropTransform())
               .into(imageView);

       if (prefs != null)
           points = prefs.getInt("points", 0);
       displayPoints = findViewById(R.id.pointsEarned3);
       displayPoints.setText(String.valueOf(points));
   }

   public void resetPoints(View view) {
       setContentView(R.layout.profile);
       if (prefs != null) {
           editor.putInt("points", 0);
           editor.apply();
       }
       points = prefs.getInt("points", 0);
   }

   public void getPoints(View view) {
       Toast.makeText(this, String.valueOf(points), Toast.LENGTH_SHORT).show();
   }

   public void logout(View view) {
        editor.putBoolean("logged in", false);
        editor.putBoolean("timer started", false);
        editor.apply();
        Intent intent = new Intent(this, Tracker.class);
        stopService(intent);

        // Restart app
       Intent i = getBaseContext().getPackageManager()
               .getLaunchIntentForPackage( getBaseContext().getPackageName() );
       i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
       intent = new Intent("Logout");
       LocalBroadcastManager.getInstance(this).sendBroadcastSync(intent);
       startActivity(i);
       i = new Intent(this, LoginScreen.class);
       startActivity(i);
       finish();
   }
}
