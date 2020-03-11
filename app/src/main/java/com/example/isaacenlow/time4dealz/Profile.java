package com.example.isaacenlow.time4dealz;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.UserStateListener;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class Profile extends AppCompatActivity {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Button getPoints;
    private TextView displayPoints, userName;
    private int points = 0;
    BroadcastReceiver br;
    ImageView imageView;
    private Button profileAdminBtn;
    public static final String MY_PREFS = "MyPrefs";
    DynamoDBMapper dynamoDBMapper;
    AmazonDynamoDBClient dynamoDBClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.profile);

       /*AWSMobileClient.getInstance().initialize(this).execute();
        dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());


       dynamoDBMapper = DynamoDBMapper
               .builder()
               .dynamoDBClient(dynamoDBClient)
               .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
               .build();*/

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

       displayPoints = findViewById(R.id.points_profile);
       userName = findViewById(R.id.profileUserName);
       displayPoints.setText(String.valueOf(prefs.getInt("points", 0)));
       userName.setText(AWSMobileClient.getInstance().getUsername());


   }

   @DynamoDBTable(tableName = "user")
   public class saveProfileInfo {
       private String email="";
       private String password="";
       private String age="";
       private String status="";
       private String phoneNumber="";

       @DynamoDBAttribute(attributeName = "")
       public String getPassword() {
           return password;
       }

       public void setPassword(String password) {
           this.password = password;
       }

       @DynamoDBAttribute(attributeName = "")
       public String getAge() {
           return age;
       }

       public void setAge(String age) {
           this.age = age;
       }

       @DynamoDBAttribute(attributeName = "")
       public String getStatus() {
           return status;
       }

       public void setStatus(String status) {
           this.status = status;
       }

       @DynamoDBAttribute(attributeName = "")
       public String getPhoneNumber() {
           return phoneNumber;
       }

       public void setPhoneNumber(String phoneNumber) {
           this.phoneNumber = phoneNumber;
       }

       @DynamoDBAttribute(attributeName = "")
       public String getEmail() {
           return email;
       }

       public void setEmail(String email) {
           this.email = email;
       }
   }

   public void getPoints(View view) {
       Toast.makeText(this, String.valueOf(points), Toast.LENGTH_SHORT).show();
   }

   public void logout(View view) throws Exception {
        AWSMobileClient.getInstance().signOut();
       Intent intent = new Intent(getBaseContext(), Tracker.class);
       stopService(intent);
       //editor.putBoolean("logged in", false);
       editor.putBoolean("timer started", false);
       editor.putInt("points", 0);
       editor.apply();
       // Restart app
       Intent i = getBaseContext().getPackageManager()
               .getLaunchIntentForPackage( getBaseContext().getPackageName() );
       i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
       intent = new Intent("Logout");
       LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcastSync(intent);
       startActivity(i);
       Toast.makeText(this, String.valueOf(AWSMobileClient.getInstance().isSignedIn()), Toast.LENGTH_SHORT).show();
       AWSMobileClient.getInstance().addUserStateListener(new UserStateListener() {
           @Override
           public void onUserStateChanged(UserStateDetails userStateDetails) {
               switch (userStateDetails.getUserState()){
                   case GUEST:
                       Log.i("userState", "user is in guest mode");
                       break;
                   case SIGNED_OUT:
                       Log.i("userState", "user is signed out");
                       break;
                   case SIGNED_IN:
                       Log.i("userState", "user is signed in");
                       break;
                   case SIGNED_OUT_USER_POOLS_TOKENS_INVALID:
                       Log.i("userState", "need to login again");
                       break;
                   case SIGNED_OUT_FEDERATED_TOKENS_INVALID:
                       Log.i("userState", "user logged in via federation, but currently needs new tokens");
                       break;
                   default:
                       Log.e("userState", "unsupported");
               }
           }
       });
       i = new Intent(getApplicationContext(), LoginScreen.class);
       startActivity(i);
       finish();
   }
}
