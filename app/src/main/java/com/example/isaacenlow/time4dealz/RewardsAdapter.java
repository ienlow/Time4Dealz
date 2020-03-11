package com.example.isaacenlow.time4dealz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;
import static com.example.isaacenlow.time4dealz.Rewards.MY_PREFS;

public class RewardsAdapter extends ArrayAdapter<RewardItem> {
    ArrayList<RewardItem> list;
    Context context;
    int points;
    String userName;

    /**
     *
     * @param context
     * @param resource
     * @param list is sorted list from Rewards.java
     * @param points is user points from Rewards.java
     */
    RewardsAdapter(@NonNull Context context, int resource, ArrayList<RewardItem> list, int points, String userName) {
        super(context, resource, list);
        this.list = list;
        this.context = context;
        this.points = points;
        this.userName = userName;
    }

    public class Holder {
        TextView discountText, pointsNeeded;
        ProgressBar progressBar;
        Button redeemButton;
        ImageView imageView;
    }

    public View getView(final int i, View view, final ViewGroup viewGroup) {
        Holder holder = null;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (view == null) {
            view = inflater.inflate(R.layout.rewards_adapter, null);
            holder = new Holder();
            holder.discountText = view.findViewById(R.id.discount_text);
            holder.imageView = view.findViewById(R.id.reward_view);
            holder.progressBar = view.findViewById(R.id.rewardsProgress);
            holder.redeemButton = view.findViewById(R.id.redeemReward);
            holder.pointsNeeded = view.findViewById(R.id.points_needed);
            holder.redeemButton.setOnClickListener(new View.OnClickListener() {
                // when reward is redeemed, insert it and the expDt into the db
                @Override
                public void onClick(View view) {
                    AmazonDynamoDBClient amazonDynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentials());
                    DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder().dynamoDBClient(amazonDynamoDBClient).build();
                    Rewards.InsertNewRewardExp insertNewRewardExp = new Rewards.InsertNewRewardExp();
                    insertNewRewardExp.setUserId(userName + getItem(i).discount);
                    insertNewRewardExp.setRewardId(getItem(i).discount);
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_MONTH, 10);
                    insertNewRewardExp.setExpDt(calendar.getTimeInMillis()/1000);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dynamoDBMapper.save(insertNewRewardExp);
                        }
                    }).start();
                    Intent intent = new Intent(context, RewardBarcode.class);
                    context.startActivity(intent);
                }
            });
        }
        else {
            holder = (Holder) view.getTag();
        }
        RewardItem rewardItem = getItem(i);
        view.setTag(holder);
        holder.discountText.setText(rewardItem.discount);
        holder.pointsNeeded.setText(points + " of " + rewardItem.points);
        holder.progressBar.setMax(rewardItem.points);
        holder.progressBar.setProgress(points);
        if (points >= rewardItem.points) {
            holder.redeemButton.setVisibility(View.VISIBLE);
        }
        else {
            holder.redeemButton.setVisibility(View.INVISIBLE);
        }
        Log.d("RewardsItem", "Discount: " + rewardItem.discount + " points: " + rewardItem.points + " position: " + i);
        return view;
    }
}
