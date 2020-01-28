package com.example.isaacenlow.time4dealz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import static com.example.isaacenlow.time4dealz.Profile.MY_PREFS;

public class Roster extends AppCompatActivity {
    String isaacUrl, jamesUrl, dequanUrl, defaultUrl;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.roster_layout);
        Intent intent = getIntent();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", 0);
        TextView pointsEarned = findViewById(R.id.points_roster);
        pointsEarned.setText(String.valueOf(sharedPreferences.getInt("points", 0)));
        ImageButton profileButton = findViewById(R.id.profile_button_roster);
        SharedPreferences prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        String userIdUrl = "";
        isaacUrl = "https://s3.amazonaws.com/timedealz-deployments-mobilehub-204377156/Icons/20881984_1283029611819396_6052734634897167129_n+(2).jpg";
        jamesUrl = "https://s3.amazonaws.com/timedealz-deployments-mobilehub-204377156/Icons/james_overton.jpg";
        dequanUrl = "https://s3.amazonaws.com/timedealz-deployments-mobilehub-204377156/Icons/deQuan-gause.jpg";
        defaultUrl = "https://s3.amazonaws.com/timedealz-deployments-mobilehub-204377156/Icons/IMG_0282.JPG";

        switch (prefs.getString("username", "")) {
            case "isaac":
                userIdUrl = isaacUrl;
                break;
            case "james":
                userIdUrl = jamesUrl;
                break;
            case "deQuan":
                userIdUrl = dequanUrl;
        }
        Glide
                .with(this)
                .load(userIdUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(profileButton);
        TextView rosterSport = findViewById(R.id.roster_sport);
        rosterSport.setText(intent.getStringExtra("sport"));

        RosterAdapter rosterAdapter;
        ArrayList<Player> players = new ArrayList<>();
        Player player = new Player("TEST", defaultUrl);
        Player player1 = new Player("TEST2", defaultUrl);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        players.add(0, player);
        players.add(1, player1);
        players.add(2, new Player("James", jamesUrl));
        players.add(3, new Player("DeQuan", dequanUrl));
        players.add(4, new Player("Isaac", isaacUrl));
        players.add(5, new Player("BUD", defaultUrl));
        players.add(6, new Player("SPOT", defaultUrl));
        RecyclerView recyclerView = findViewById(R.id.roster_recycler_view);
        rosterAdapter = new RosterAdapter(getApplicationContext(), players);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(rosterAdapter);
    }

    public void openProfile(View view) {
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }
}
