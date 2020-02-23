package com.example.isaacenlow.time4dealz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import static com.example.isaacenlow.time4dealz.SplashScreen.MY_PREFS;

public class Interests extends AppCompatActivity {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    boolean isProfDevBtnClicked = true, isSportsBtnClicked = true, isBusinessBtnClicked = true, isArtsBtnClicked = true;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.interests);
        prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void toggleButton (View view) {
        Button button = findViewById(view.getId());
        switch (view.getId()) {
            case R.id.profDevBtn :
                view.setBackgroundResource(isProfDevBtnClicked ? R.drawable.login_sign_in : R.drawable.solid_rounded_corners2);
                button.setTextColor(isProfDevBtnClicked ? Color.WHITE : Color.BLACK);
                isProfDevBtnClicked = !isProfDevBtnClicked;
                break;
            case R.id.sportsBtn :
                view.setBackgroundResource(isSportsBtnClicked ? R.drawable.login_sign_in : R.drawable.solid_rounded_corners2);
                button.setTextColor(isSportsBtnClicked ? Color.WHITE : Color.BLACK);
                isSportsBtnClicked = !isSportsBtnClicked;
                break;
            case R.id.businessBtn :
                view.setBackgroundResource(isBusinessBtnClicked ? R.drawable.login_sign_in : R.drawable.solid_rounded_corners2);
                button.setTextColor(isBusinessBtnClicked ? Color.WHITE : Color.BLACK);
                isBusinessBtnClicked = !isBusinessBtnClicked;
                break;
            case R.id.artsBtn :
                view.setBackgroundResource(isArtsBtnClicked ? R.drawable.login_sign_in : R.drawable.solid_rounded_corners2);
                button.setTextColor(isArtsBtnClicked ? Color.WHITE : Color.BLACK);
                isArtsBtnClicked = !isArtsBtnClicked;
                break;
        }
    }

    public void startLoginScreen(View view) {
        editor.putBoolean("interests_selected", true);
        editor.apply();
        Intent intent = new Intent(Interests.this, LoginScreen.class);
        startActivity(intent);
        finish();
    }
}
