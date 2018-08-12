package com.example.isaacenlow.time4dealz;

public class LocationItems {

    LocationsDO locationItem;
    String category;

    public LocationItems(LocationsDO locationsDO) {
        this.locationItem = locationsDO;
        this.category = locationsDO.getCategory();
    }

    public String getCategory() {
        return category;
    }
}
