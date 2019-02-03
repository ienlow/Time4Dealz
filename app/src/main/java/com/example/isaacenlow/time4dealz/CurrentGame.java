package com.example.isaacenlow.time4dealz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class CurrentGame extends AppCompatActivity {
    ImageButton backArrow;
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.current_game);
        //Intent intent = getIntent();
        SharedPreferences preferences = getSharedPreferences("MyPrefs", 0);
        SharedPreferences.Editor editor = preferences.edit();
        TextView textView = findViewById(R.id.points2);
        textView.setText(String.valueOf(preferences.getInt("points", 0)));
        backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}
