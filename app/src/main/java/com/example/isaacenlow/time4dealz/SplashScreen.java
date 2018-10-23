package com.example.isaacenlow.time4dealz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
    private SharedPreferences prefs;
    public static final String MY_PREFS = "MyPrefs";

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        if (!prefs.getBoolean("logged in", false)) {
            setContentView(R.layout.splash_screen);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch(Exception e) {
                        e.printStackTrace();
                    } finally {
                        Intent intent = new Intent(SplashScreen.this, LoginScreen.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }).start();
        }
        else {
            Intent intent = new Intent(SplashScreen.this, LoginScreen.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }
}
