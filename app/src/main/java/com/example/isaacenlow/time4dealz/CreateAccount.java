package com.example.isaacenlow.time4dealz;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class CreateAccount extends AppCompatActivity {
    private DynamoDBMapper dynamoDBMapper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);

        AWSMobileClient.getInstance().initialize(this).execute();
        final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());


        dynamoDBMapper = DynamoDBMapper
                .builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();
    }

    @DynamoDBTable(tableName = "ExampleSchoolUserAccounts")
    public class CreateAccountUtil {
        private String userName = "";
        private String password = "";
        private String imageURL = "";
        private int points = 0;

        @DynamoDBAttribute(attributeName = "userPoints")
        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        @DynamoDBAttribute(attributeName = "imageURL")
        public String getImageURL() {
            return imageURL;
        }

        public void setImageURL(String imageURL) {
            this.imageURL = imageURL;
        }

        @DynamoDBHashKey(attributeName = "userID")
        @DynamoDBAttribute(attributeName = "userID")
        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        @DynamoDBAttribute(attributeName = "password")
        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    class BackgroundWorker extends AsyncTask<String, Void, String> {
        TextView userName, password, passwordConfirm;

        @Override
        protected String doInBackground(String... strings) {
            userName = findViewById(R.id.createUserName);
            password = findViewById(R.id.createPassword);
            passwordConfirm = findViewById(R.id.createPasswordConfirm);

            final CreateAccountUtil util = new CreateAccountUtil();
            util.setUserName(userName.getText().toString());
            util.setPassword(password.getText().toString());
            util.setImageURL("null");
            util.setPoints(0);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (password.getText().toString().equals(passwordConfirm.getText().toString())) {
                        dynamoDBMapper.save(util);
                    }
                    else {
                        Toast.makeText(CreateAccount.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                    }
                }
            }).start();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("Account Created", "Username: " + userName.getText().toString() + " Password: " + password.getText().toString());
            finish();
        }
    }

    public void createAccount(View view) {
        BackgroundWorker backgroundWorker = new BackgroundWorker();
        backgroundWorker.execute();
    }
}
