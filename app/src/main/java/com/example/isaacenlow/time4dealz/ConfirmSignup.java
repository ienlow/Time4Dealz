package com.example.isaacenlow.time4dealz;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.example.isaacenlow.time4dealz.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ConfirmSignup extends AppCompatActivity {
    private String TAG;
    DynamoDBMapper dynamoDBMapper;
    String username, password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_account);
        Intent intent = getIntent();
        username  = intent.getStringExtra("username");
        password = intent.getStringExtra("password");
    }

    @DynamoDBTable(tableName = "ExampleSchoolUserAccounts")
    public class CreateAccountUtil {
        private String userName = "";
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
    }

    class BackgroundWorker extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            final CreateAccountUtil util = new CreateAccountUtil();
            util.setUserName(username);
            util.setImageURL("null");
            util.setPoints(0);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    dynamoDBMapper.save(util);
                }
            }).start();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Intent intent = new Intent(getApplicationContext(), MainMenu.class);
            startActivity(intent);
            finish();
        }
    }

    private void makeToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void confirm(View view) {
        EditText codeText = findViewById(R.id.confirmationCode);
        final String code = codeText.getText().toString();
        AWSMobileClient.getInstance().confirmSignUp(username, code, new Callback<SignUpResult>() {
            @Override
            public void onResult(final SignUpResult signUpResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Sign-up callback state: " + signUpResult.getConfirmationState());
                        if (!signUpResult.getConfirmationState()) {
                            final UserCodeDeliveryDetails details = signUpResult.getUserCodeDeliveryDetails();
                            makeToast("Confirm sign-up with: " + details.getDestination());
                        } else {
                            makeToast("Sign-up done.");
                            AWSMobileClient.getInstance().signIn(username, password, null, new Callback<SignInResult>() {
                                @Override
                                public void onResult(final SignInResult signInResult) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d(TAG, "Sign-in callback state: " + signInResult.getSignInState());
                                            switch (signInResult.getSignInState()) {
                                                case DONE:
                                                    makeToast("Sign-in done.");
                                                    Log.i("User ID: ", AWSMobileClient.getInstance().getUsername());
                                                    Log.i("Identity: ", AWSMobileClient.getInstance().getIdentityId());
                                                    final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentials());
                                                    dynamoDBMapper = DynamoDBMapper
                                                            .builder()
                                                            .dynamoDBClient(dynamoDBClient)
                                                            .build();
                                                    BackgroundWorker backgroundWorker = new BackgroundWorker();
                                                    backgroundWorker.execute();
                                                    break;
                                                case SMS_MFA:
                                                    makeToast("Please confirm sign-in with SMS.");
                                                    break;
                                                case NEW_PASSWORD_REQUIRED:
                                                    makeToast("Please confirm sign-in with new password.");
                                                    break;
                                                default:
                                                    makeToast("Unsupported sign-in confirmation: " + signInResult.getSignInState());
                                                    break;
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e(TAG, "Sign-in error", e);
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Confirm sign-up error", e);
            }
        });
    }
}
