package com.example.isaacenlow.time4dealz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;

import androidx.appcompat.app.AppCompatActivity;


import static android.content.Context.MODE_PRIVATE;

public class SplashScreen extends AppCompatActivity {
    private SharedPreferences prefs;
    public static final String MY_PREFS = "MyPrefs";

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.splash_screen);
        prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i("INIT", "onResult: " + userStateDetails.getUserState());
                        switch (userStateDetails.getUserState()) {
                            case SIGNED_IN:
                                startActivity(new Intent(getApplicationContext(), MainMenu.class));
                                finish();
                                break;
                            case SIGNED_OUT:
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(2000);
                                        } catch(Exception e) {
                                            e.printStackTrace();
                                        } finally {
                                            showSignIn();
                                        }
                                    }
                                }).start();
                                break;
                            default:
                                AWSMobileClient.getInstance().signOut();
                                showSignIn();
                                break;
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("INIT", "Initialization error.", e);
                    }
                }
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    private void showSignIn() {
        try {
            AWSMobileClient.getInstance().showSignIn(
                    this,
                    SignInUIOptions.builder()
                            .nextActivity(MainMenu.class)
                            .build());
        } catch (Exception e) {
            Log.e("", e.toString());
        }
    }
}
