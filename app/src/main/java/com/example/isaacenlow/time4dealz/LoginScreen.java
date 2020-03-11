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
    private EditText usernameText, passwordText;
    private String TAG;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        usernameText = findViewById(R.id.username);
        passwordText = findViewById(R.id.password);
        //FirebaseApp.initializeApp(this);
        //AWSMobileClient.getInstance().initialize(this).execute();

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
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName("ExampleSchoolUserAccounts")
                    .withAttributesToGet("userID")
                    .withAttributesToGet("password")
                    .withAttributesToGet("imageURL")
                    .withAttributesToGet("userPoints");
            ScanResult scanResult = dynamoDBClient.scan(scanRequest);
            for (Map<String, AttributeValue> item : scanResult.getItems()) {
                if (username.equals(item.get("userID").getS())) {
                    if (!prefs.getBoolean("tracking", false) && prefs.getBoolean("enabled", true)) {
                        // set tracking to true and start service
                        editor.putBoolean("tracking", true);
                        editor.putBoolean("enabled", true);
                        editor.apply();
                        Intent intentTracker = new Intent(getApplicationContext(), Tracker.class);
                        startForegroundService(intentTracker);
                    }
                    editor.putString("userId", username);
                    editor.putString("password", password);
                    editor.putString("imageURL", item.get("imageURL").getS());
                    editor.putInt("points", Integer.valueOf(item.get("userPoints").getN()));
                    editor.apply();
                    Log.d("createMainMenu", "activity started");
                    //startActivity(intent);
                    finish();
                }
            }
            return null;
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
        username = usernameText.getText().toString();
        password = passwordText.getText().toString();
        AWSMobileClient.getInstance().signIn(username, password, null, new Callback<SignInResult>() {
            @Override
            public void onResult(final SignInResult signInResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Sign-in callback state: " + signInResult.getSignInState());
                        switch (signInResult.getSignInState()) {
                            case DONE:
                                makeToast("Sign-in done.");
                                Log.i("User ID: ", AWSMobileClient.getInstance().getUsername());
                                Log.i("Identity: ", AWSMobileClient.getInstance().getIdentityId());
                                dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentials());
                                BackgroundWorker backgroundWorker = new BackgroundWorker();
                                backgroundWorker.execute();
                                intent = new Intent(getApplicationContext(), MainMenu.class);
                                startActivity(intent);
                                finish();
                                break;
                            case SMS_MFA:
                                makeToast("Please confirm sign-in with SMS.");
                                break;
                            case NEW_PASSWORD_REQUIRED:
                                makeToast("Please confirm sign-in with new password.");
                                break;
                            default:
                                makeToast("Unsupported sign-in confirmation: " + signInResult.getSignInState());
                                break;
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Sign-in error", e);
            }
        });
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

    private void makeToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        moveTaskToBack(true);
    }
}