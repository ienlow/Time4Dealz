package com.example.isaacenlow.time4dealz;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.UserStateListener;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.google.firebase.FirebaseApp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Map;

import static com.amazonaws.mobile.client.results.SignInState.DONE;

/**
 * Created by isaac on 2/21/2018.
 */



public class LoginScreen extends AppCompatActivity {

    Context context;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String username, password;
    public static final String MY_PREFS = "MyPrefs";
    AmazonDynamoDBClient dynamoDBClient;
    private Intent intent;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.login_screen);
        //FirebaseApp.initializeApp(this);
        //AWSMobileClient.getInstance().initialize(this).execute();
        //dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());

        prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        editor = prefs.edit();
        ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET}, 123);


    }

    @SuppressLint("NewApi")
    class BackgroundWorker extends AsyncTask<String, Void, String>
    {
        private String match = "false";
        @Override
        protected String doInBackground(String... strings) {
            /*ScanRequest scanRequest = new ScanRequest()
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
                        Intent intentTracker = new Intent(getApplicationContext(), Tracker.class);
                        startForegroundService(intentTracker);
                    }
                    //intent = new Intent(getApplicationContext(), MainMenu.class);
                    if (intent.getClass().getSimpleName().equals(MainMenu.class.getSimpleName())) {editor.putBoolean("logged in", true);}// set logged in to true
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
            return match;*/

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (match.equals("false")) {
                Toast.makeText(LoginScreen.this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
            }
            Log.d("Is User Logged in: ", String.valueOf(IdentityManager.getDefaultIdentityManager().isUserSignedIn()));
        }
    }

    /*
    Create Main Menu intent and save login info
     */
    public void createMainMenu(View view) {
        intent = new Intent(getApplicationContext(), MainMenu.class);
        //BackgroundWorker backgroundWorker = new BackgroundWorker();
        //backgroundWorker.execute();
    }

    public void createAccount(View view) {
        Intent intent = new Intent(this, CreateAccount.class);
        startActivity(intent);
    }

    public void adminPage(View view) {
        intent = new Intent(this, AdminPage.class);
        //BackgroundWorker backgroundWorker = new BackgroundWorker();
        //backgroundWorker.execute();
    }

    @Override
    public void onBackPressed() {
        finish();
        moveTaskToBack(true);
    }
}