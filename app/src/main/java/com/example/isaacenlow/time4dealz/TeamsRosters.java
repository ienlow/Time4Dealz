package com.example.isaacenlow.time4dealz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class TeamsRosters extends AppCompatActivity {

    TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_rosters);
        textView = findViewById(R.id.basketball_btn);
    }

    public void loadRoster(View view) {
        String sport = getResources().getResourceEntryName(view.getId());
        Intent intent = new Intent(getApplicationContext(), Roster.class);
        switch(sport) {
            case "basketball_btn":
                intent.putExtra("sport", "MEN'S BASKETBALL");
                break;
            case "baseball_btn":
                intent.putExtra("sport", "BASEBALL");
                break;
            case "soccer_btn":
                intent.putExtra("sport", "SOCCER");
                break;
            case "tennis_btn":
                intent.putExtra("sport", "TENNIS");
                break;
            case "default":
                intent.putExtra("sport", "ERROR");
                break;
        }
        startActivity(intent);
    }
}
