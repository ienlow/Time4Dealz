package com.example.isaacenlow.time4dealz;

import com.amazonaws.mobile.client.AWSMobileClient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.Map;
import java.util.Set;

/**
 * Created by isaac on 2/21/2018.
 */



public class LoginScreen extends AppCompatActivity {

    Context context;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private EditText username, password;
    private DynamoDBMapper dynamoDBMapper;
    public static final String MY_PREFS = "MyPrefs";

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        editor = prefs.edit();

        if (!prefs.getString("username", "").equals("")) {
            Intent intent = new Intent(this, MainMenu.class);
            startActivity(intent);
            if (!prefs.getBoolean("tracking", false) && (prefs.getBoolean("enabled", false))) {
                editor.putBoolean("tracking", true);
                editor.putBoolean("enabled", true);
                editor.apply();
                AWSMobileClient.getInstance().initialize(this).execute();
                intent = new Intent(this, Tracker.class);
                startForegroundService(intent);
                finish();
            }
            finish();
        }
        else {
            setContentView(R.layout.activity_login_screen);
            username = findViewById(R.id.username);
            password = findViewById(R.id.password);
        }
        ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET}, 123);
    }

    /*
    Create Main Menu intent and save login info
     */
    @SuppressLint("NewApi")
    public void mainMenu(View view) {
        editor.putString("username", username.getText().toString());
        editor.putString("password", password.getText().toString());
        editor.apply();

        AWSMobileClient.getInstance().initialize(this).execute();



        if (!prefs.getBoolean("tracking", false)) {
            editor.putBoolean("tracking", true);
            editor.putBoolean("enabled", true);
            editor.apply();
            AWSMobileClient.getInstance().initialize(this).execute();
            finish();
            Intent intent = new Intent(this, Tracker.class);
        startForegroundService(intent);
        }
        finish();
        Intent  intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}