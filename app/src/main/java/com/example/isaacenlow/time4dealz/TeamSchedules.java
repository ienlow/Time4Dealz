package com.example.isaacenlow.time4dealz;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewOutlineProvider;
import android.widget.ListView;

import java.util.ArrayList;

public class TeamSchedules extends AppCompatActivity {
    ArrayList<Event> teams, tmp;
    Event one;
    String done;
    BackgroundWorker backgroundWorker;
    //ProgressBar progressBar = findViewById(R.id.progressBar);

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_schedules);
        tmp = new ArrayList<>();
        teams = new ArrayList<>();
        display();
    }

    public void display() {
        backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute("");
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (!backgroundWorker.finished()) {
            //progressBar.setVisibility(View.VISIBLE);
        }
        tmp = backgroundWorker.getTeams();
        // https://javarevisited.blogspot.com/2016/05/how-to-reverse-arraylist-in-place-in-java.html
        int size = tmp.size();
        for (int i = 0; i < size / 2; i++) {
            final Event event = tmp.get(i);
            tmp.set(i, tmp.get(size - i - 1)); // swap
            tmp.set(size - i - 1, event); // swap
        }
        ListView listView = findViewById(R.id.results);
        TeamAdapter adapter = new TeamAdapter(getApplicationContext(), R.layout.adapter_layout, tmp);
        listView.setAdapter(adapter);
    }
}
