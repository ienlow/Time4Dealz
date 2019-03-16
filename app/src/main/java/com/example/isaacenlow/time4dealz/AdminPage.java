package com.example.isaacenlow.time4dealz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
}
