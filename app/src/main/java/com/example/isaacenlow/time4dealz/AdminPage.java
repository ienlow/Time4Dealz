package com.example.isaacenlow.time4dealz;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;


public class AdminPage extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_page);
    }

    public void updateRewards(View view) {
        Intent intent = new Intent(this, UpdateRewards.class);
        startActivity(intent);
    }

    public void updateEvent(View view) {
        Intent intent = new Intent(this, UpdateEvent.class);
        startActivity(intent);
    }
}
