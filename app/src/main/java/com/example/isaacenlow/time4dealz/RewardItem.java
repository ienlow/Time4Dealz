package com.example.isaacenlow.time4dealz;

public class RewardItem {
    String discount = "";
    int points;
    boolean isDisabled;

    public RewardItem(String discount, int points, boolean isDisabled) {
        this.discount = discount;
        this.points = points;
        this.isDisabled = isDisabled;
    }
}
