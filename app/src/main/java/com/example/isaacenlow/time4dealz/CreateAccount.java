package com.example.isaacenlow.time4dealz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;

import java.util.HashMap;
import java.util.Map;

import static com.amazonaws.mobile.client.internal.oauth2.OAuth2Client.TAG;

public class CreateAccount extends Activity {
    private DynamoDBMapper dynamoDBMapper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);
    }



    public void createAccount(View view) {
        EditText userNameText = findViewById(R.id.createUserName);
        EditText passwordText = findViewById(R.id.createPassword);
        EditText emailText = findViewById(R.id.createEmail);
        final String username = userNameText.getText().toString();
        final String password = passwordText.getText().toString();
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("email", emailText.getText().toString());
        AWSMobileClient.getInstance().signUp(username, password, attributes, null, new Callback<SignUpResult>() {
            @Override
            public void onResult(final SignUpResult signUpResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Sign-up callback state: " + signUpResult.getConfirmationState());
                        if (!signUpResult.getConfirmationState()) {
                            final UserCodeDeliveryDetails details = signUpResult.getUserCodeDeliveryDetails();
                            makeToast("Confirm sign-up with: " + details.getDestination());
                            Intent intent = new Intent(getApplicationContext(), ConfirmSignup.class);
                            intent.putExtra("username", username);
                            intent.putExtra("password", password);
                            startActivity(intent);
                            finish();
                        } else {
                            makeToast("Sign-up done.");
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Sign-up error", e);
            }
        });

    }

    private void makeToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
