package com.example.isaacenlow.time4dealz;

import android.app.DatePickerDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBNativeBoolean;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class CreateEvent extends AppCompatActivity {
    TextView getAddress, opponentText, getVenueName;
    Button createEventBtn;
    String address_line;
    String strAddress = "";
    double latitude, longitude;
    CheckBox activeCheck;
    int day, month, year;
    DynamoDBMapper dynamoDBMapper;
    Spinner sportSpinner, timeSpinner;
    CalendarView calendarView;
    final CreateNewEventDBUtil eventDBUtil = new CreateNewEventDBUtil();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event);
        AWSMobileClient.getInstance().initialize(this).execute();
        final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        getVenueName = findViewById(R.id.createEventVenue);
        getAddress = findViewById(R.id.createEventAddress);
        createEventBtn = findViewById(R.id.createEventDate);
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this);
        createEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });
        //calendarView = findViewById(R.id.calendarView);
        DatePickerDialog.OnDateSetListener onDateSetListener;
        DatePicker datePicker = datePickerDialog.getDatePicker();
        datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                Toast.makeText(getApplicationContext(), String.valueOf(datePicker.getMonth() + 1 + "/" + datePicker.getDayOfMonth() + "/" + datePicker.getYear()), Toast.LENGTH_SHORT).show();
                eventDBUtil.setDate(datePicker.getMonth() + 1 + "/" + datePicker.getDayOfMonth() + "/" + datePicker.getYear());
            }
        });
        /*calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                Toast.makeText(CreateEvent.this, year + "/" + month + "/" + day, Toast.LENGTH_SHORT).show();
                eventDBUtil.setDate(month + "/" + day + "/" + year);
            }
        });*/
        opponentText = findViewById(R.id.createEventOpponent);
        activeCheck = findViewById(R.id.createEventActive);
        timeSpinner = findViewById(R.id.timeSpinner);
        sportSpinner = findViewById(R.id.sportSpinner);
        ArrayAdapter timeAdapter = ArrayAdapter.createFromResource(this, R.array.time, R.layout.support_simple_spinner_dropdown_item);
        timeAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        // onItemClickListener for time spinner
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!adapterView.getItemAtPosition(i).toString().equals("Select a time")) {
                    Scanner scanner = new Scanner(adapterView.getItemAtPosition(i).toString());
                    if (scanner.hasNext()) {
                        Scanner scanner1 = new Scanner(scanner.next());
                        scanner1.useDelimiter(":");
                        int hour = Integer.valueOf(scanner1.next());
                        if (scanner.next().equals("PM")) {
                            hour += 12;
                        }
                        int minute = Integer.valueOf(scanner1.next());
                        eventDBUtil.setTime(hour + ":" + minute);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        timeSpinner.setAdapter(timeAdapter);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.sports, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        // onItemClickListener for sport spinner
        sportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                eventDBUtil.setSport(adapterView.getItemAtPosition(i).toString());
                switch (adapterView.getItemAtPosition(i).toString()) {
                    case "Men's Basketball":
                        eventDBUtil.setURL("https://s3.amazonaws.com/timedealz-deployments-mobilehub-204377156/Icons/basketballd_icon.png");
                        break;
                    case "Tennis":
                        eventDBUtil.setURL("https://s3.amazonaws.com/timedealz-deployments-mobilehub-204377156/Icons/basketballd_icon.png");
                        break;
                    case "Baseball":
                        eventDBUtil.setURL("https://s3.amazonaws.com/timedealz-deployments-mobilehub-204377156/Icons/basketballd_icon.png");
                        break;
                    case "Soccer":
                        eventDBUtil.setURL("https://s3.amazonaws.com/timedealz-deployments-mobilehub-204377156/Icons/basketballd_icon.png");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sportSpinner.setAdapter(adapter);
        dynamoDBMapper = DynamoDBMapper
                .builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();
    }

    public String getAddressFromLocation(Context context, String strAddress) {
        if (!strAddress.equals("")) {
            try {
                Geocoder coder = new Geocoder(context, Locale.US);
                List<Address> test = coder.getFromLocationName(strAddress, 2);
                Address add = test.get(0);
                address_line = add.getAddressLine(0);
                address_line += " " + add.getLatitude();
                latitude = add.getLatitude();
                longitude = add.getLongitude();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return address_line;
    }

    /**
     * Get's the address when checking location
     * @param view
     */
    public void getAddressOnClick(View view) {
        strAddress = getVenueName.getText().toString() + ", " + getAddress.getText().toString();
        if(!getVenueName.getText().toString().equals("") && !getAddress.getText().toString().equals("")) {
            Toast.makeText(this, getAddressFromLocation(getApplicationContext(), strAddress), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get's the address and saves the event
     * @param view
     */
    public void saveEventOnClick(View view) {
        eventDBUtil.setActive(activeCheck.isChecked());
        eventDBUtil.setOpponent(opponentText.getText().toString());
        eventDBUtil.setLocation(getVenueName.getText().toString());
        strAddress = getVenueName.getText().toString() + ", " + getAddress.getText().toString();
        if (!getVenueName.getText().toString().equals("") && !getAddress.getText().toString().equals("")) {
            getAddressFromLocation(getApplicationContext(), strAddress);
            CreateEventBackgroundWorker backgroundWorker = new CreateEventBackgroundWorker();
            backgroundWorker.execute();
        }
    }

    @DynamoDBTable(tableName = "ExampleSchool")
    public class CreateNewEventDBUtil {
        private String sport;
        private String date;
        private String time;
        private String opponent;

        private String location;
        private String URL;
        private double latitude, longitude;
        private int itemId;
        private boolean active = false;

        @DynamoDBAttribute(attributeName = "URL")
        public String getURL() { return URL; }

        @DynamoDBAttribute(attributeName = "sport")
        public String getSport() {
            return sport;
        }

        @DynamoDBAttribute(attributeName = "longitude")
        public double getLongitude() { return longitude; }

        @DynamoDBAttribute(attributeName = "latitude")
        public double getLatitude() { return latitude; }

        @DynamoDBAttribute(attributeName = "date")
        public String getDate() {
            return date;
        }

        @DynamoDBAttribute(attributeName = "time")
        public String getTime() {
            return time;
        }

        @DynamoDBAttribute(attributeName = "playing_against")
        public String getOpponent() {
            return opponent;
        }

        @DynamoDBAttribute(attributeName = "active")
        @DynamoDBNativeBoolean
        public boolean getActive() { return active; }

        @DynamoDBHashKey(attributeName = "itemId")
        @DynamoDBAttribute(attributeName = "itemId")
        public int getItemId() {
            return itemId;
        }

        @DynamoDBAttribute(attributeName = "location")
        public String getLocation() { return location; }

        void setItemId(int itemId) { this.itemId = itemId; }

        void setSport(String sport) {
            this.sport = sport;
        }

        void setDate(String date) {
            this.date = date;
        }

        void setTime(String time) {
            this.time = time;
        }

        void setOpponent(String opponent) {
            this.opponent = opponent;
        }

        void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        void setActive(boolean active) {
            this.active = active;
        }

        void setLocation(String location) {
            this.location = location;
        }

        void setURL(String URL) { this.URL = URL; }
    }

    public class CreateEventBackgroundWorker extends AsyncTask<String, Void, String> {
        int id = 1;

        @Override
        protected String doInBackground(String... strings) {
            final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName("ExampleSchool")
                    .withAttributesToGet("itemId");
            ScanResult scanResult = dynamoDBClient.scan(scanRequest);
            int i = 1;
            while (i > 0) {
                i = 0;
                for (Map<String, AttributeValue> item : scanResult.getItems()) {
                    Log.d("items ", String.valueOf(item.get("itemId")));
                    if (Integer.parseInt(item.get("itemId").getN()) == id) {
                        id += 1;
                        i++;
                    }
                }
            }
            Log.d("ID Read", String.valueOf(id));
            eventDBUtil.setItemId(id);
            eventDBUtil.setLatitude(latitude);
            eventDBUtil.setLongitude(longitude);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    dynamoDBMapper.save(eventDBUtil);
                }
            }).start();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(CreateEvent.this, "Success", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
