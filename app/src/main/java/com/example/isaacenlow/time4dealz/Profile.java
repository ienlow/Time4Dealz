package com.example.isaacenlow.time4dealz;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

public class Profile extends AppCompatActivity {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Button getPoints;
    private TextView displayPoints, userName;
    private int points = 0;
    BroadcastReceiver br;
    ImageView imageView;
    private Button trackingButton;
    public static final String MY_PREFS = "MyPrefs";
    DynamoDBMapper dynamoDBMapper;
    AmazonDynamoDBClient dynamoDBClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.profile);

       AWSMobileClient.getInstance().initialize(this).execute();
        dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());


       dynamoDBMapper = DynamoDBMapper
               .builder()
               .dynamoDBClient(dynamoDBClient)
               .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
               .build();

       prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
       editor = prefs.edit();

       imageView = findViewById(R.id.image_profile);
       String userIdUrl = "";
       if (!prefs.getString("imageURL", "").equals("null")) {
           userIdUrl = prefs.getString("imageURL", "");
       }
       else {
           userIdUrl = "https://s3.amazonaws.com/timedealz-deployments-mobilehub-204377156/Icons/radford+logo.png";
       }
       Glide
               .with(this)
               .load(userIdUrl)
               .apply(RequestOptions.circleCropTransform())
               .into(imageView);

       if (prefs != null)
           points = prefs.getInt("points", 0);
       displayPoints = findViewById(R.id.points_profile);
       userName = findViewById(R.id.profileUserName);
       displayPoints.setText(String.valueOf(points));
       userName.setText(prefs.getString("username", ""));
   }

    @DynamoDBTable(tableName = "ExampleSchoolUserAccounts")
   public class SavePoints {
        private String userName = "";
        private int points = 0;

        @DynamoDBAttribute(attributeName = "userPoints")
        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        @DynamoDBHashKey(attributeName = "userID")
        @DynamoDBAttribute(attributeName = "userID")
        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
   }

   public void resetPoints(View view) {
       setContentView(R.layout.profile);
       if (prefs != null) {
           editor.putInt("points", 0);
           editor.apply();
       }
       points = 0;
   }

   public void getPoints(View view) {
       Toast.makeText(this, String.valueOf(points), Toast.LENGTH_SHORT).show();
   }

   public void logout(View view) {
        editor.putBoolean("logged in", false);
        editor.putBoolean("timer started", false);
        editor.apply();
        final String userName = prefs.getString("username", "");
       final SavePoints accountUtil = new SavePoints();
        new Thread(new Runnable() {
            @Override
            public void run() {
                accountUtil.setPoints(points);
                accountUtil.setUserName(userName);
                dynamoDBMapper.save(accountUtil);
            }
        }).start();
        Intent intent = new Intent(this, Tracker.class);
        stopService(intent);

        // Restart app
       Intent i = getBaseContext().getPackageManager()
               .getLaunchIntentForPackage( getBaseContext().getPackageName() );
       i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
       intent = new Intent("Logout");
       LocalBroadcastManager.getInstance(this).sendBroadcastSync(intent);
       startActivity(i);
       i = new Intent(this, LoginScreen.class);
       startActivity(i);
       finish();
   }
}
