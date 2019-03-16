package com.example.isaacenlow.time4dealz;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class RewardsAdapter extends ArrayAdapter<RewardItem> {
    ArrayList<RewardItem> list;
    Context context;
    int points;

    /**
     *
     * @param context
     * @param resource
     * @param list is sorted list from Rewards.java
     * @param points is user points from Rewards.java
     */
    RewardsAdapter(@NonNull Context context, int resource, ArrayList<RewardItem> list, int points) {
        super(context, resource, list);
        this.list = list;
        this.context = context;
        this.points = points;
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
                @Override
                public void onClick(View view) {
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
        holder.pointsNeeded.setText(rewardItem.points + " of " + rewardItem.points);
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
