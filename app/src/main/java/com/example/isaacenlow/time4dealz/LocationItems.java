package com.example.isaacenlow.time4dealz;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;

public class LocationItems {

    LocationsDO locationItem;
    private String _userId;
    private int _itemId;
    private String _category;
    private Double _latitude;
    private Double _longitude;
    private String _name;

    public LocationItems(LocationsDO locationsDO) {
        this.locationItem = locationsDO;
        this._category = locationsDO.getCategory();
        this._itemId = locationsDO.getItemId();
        this._latitude = locationsDO.getLatitude();
        this._longitude = locationsDO.getLongitude();
    }

    @DynamoDBHashKey(attributeName = "itemId")
    /*@DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }*/
    //@DynamoDBRangeKey(attributeName = "itemId")
    @DynamoDBAttribute(attributeName = "itemId")
    public int getItemId() {
        return _itemId;
    }

    public void setItemId(final int _itemId) {
        this._itemId = _itemId;
    }
    @DynamoDBIndexHashKey(attributeName = "category", globalSecondaryIndexName = "Categories")
    public String getCategory() {
        return _category;
    }

    public void setCategory(final String _category) {
        this._category = _category;
    }
    @DynamoDBAttribute(attributeName = "latitude")
    public Double getLatitude() {
        return _latitude;
    }

    public void setLatitude(final Double _latitude) {
        this._latitude = _latitude;
    }
    @DynamoDBIndexRangeKey(attributeName = "longitude", globalSecondaryIndexName = "Categories")
    public Double getLongitude() {
        return _longitude;
    }

    public void setLongitude(final Double _longitude) {
        this._longitude = _longitude;
    }
    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return _name;
    }

    public void setName(final String _name) {
        this._name = _name;
    }
}
