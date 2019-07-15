package com.example.isaacenlow.time4dealz;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by isaac on 2/21/2018.
 */



public class LoginScreen extends AppCompatActivity {

    Context context;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private EditText username, password;
    public static final String MY_PREFS = "MyPrefs";
    AmazonDynamoDBClient dynamoDBClient;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AWSMobileClient.getInstance().initialize(this).execute();
        dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());

        prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        editor = prefs.edit();
        ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET}, 123);

        if (prefs.getBoolean("logged in", false)) {
            // if there isn't already a tracking service running and tracking prefs is set to true
            if (!prefs.getBoolean("tracking", false) && (prefs.getBoolean("enabled", false))) {
                editor.putBoolean("tracking", true);
                editor.apply();
                Intent intent = new Intent(this, Tracker.class);
                startForegroundService(intent);
                //finish();
            }
            Intent intent = new Intent(this, MainMenu.class);
            editor.putBoolean("logged in", true);
            editor.apply();
            startActivity(intent);
            Log.d("onCreate", "activity started");
            finish();
        }
        else {
            setContentView(R.layout.login_screen);
            username = findViewById(R.id.username);
            password = findViewById(R.id.password);
            username.setText(prefs.getString("username", ""));
            password.setText(prefs.getString("password", ""));
        }
    }

    @SuppressLint("NewApi")
    class BackgroundWorker extends AsyncTask<String, Void, String>
    {
        private String match = "false";
        @Override
        protected String doInBackground(String... strings) {
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName("ExampleSchoolUserAccounts")
                    .withAttributesToGet("userID")
                    .withAttributesToGet("password")
                    .withAttributesToGet("imageURL")
                    .withAttributesToGet("userPoints");
            ScanResult scanResult = dynamoDBClient.scan(scanRequest);
            for (Map<String, AttributeValue> item : scanResult.getItems()) {
                if (username.getText().toString().equals(item.get("userID").getS())
                        && password.getText().toString().equals(item.get("password").getS())) {
                    if (!prefs.getBoolean("tracking", false) && prefs.getBoolean("enabled", true)) {
                        // set tracking to true and start service
                        editor.putBoolean("tracking", true);
                        editor.putBoolean("enabled", true);
                        editor.apply();
                        Intent intent = new Intent(getApplicationContext(), Tracker.class);
                        startForegroundService(intent);
                    }
                    Intent  intent = new Intent(getApplicationContext(), MainMenu.class);
                    editor.putBoolean("logged in", true); // set logged in to true
                    editor.putString("username", username.getText().toString()); // save username and password
                    editor.putString("password", password.getText().toString());
                    editor.putString("imageURL", item.get("imageURL").getS());
                    editor.putInt("points", Integer.valueOf(item.get("userPoints").getN()));
                    editor.apply();
                    Log.d("createMainMenu", "activity started");
                    startActivity(intent);
                    finish();
                    match = "true";
                }
            }
            return match;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (match.equals("false")) {
                Toast.makeText(LoginScreen.this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
    Create Main Menu intent and save login info
     */
    public void createMainMenu(View view) {
        BackgroundWorker backgroundWorker = new BackgroundWorker();
        backgroundWorker.execute();
    }

    public void createAccount(View view) {
        Intent intent = new Intent(this, CreateAccount.class);
        startActivity(intent);
    }

    public void adminPage(View view) {
        Intent intent = new Intent(this, AdminPage.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        finish();
        moveTaskToBack(true);
    }
}