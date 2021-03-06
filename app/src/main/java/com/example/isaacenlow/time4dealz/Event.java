package com.example.isaacenlow.time4dealz;

import android.graphics.Bitmap;
import android.media.Image;
import android.widget.ImageView;

import java.util.Calendar;

/**
 * Created by Isaac Enlow on 12/5/2016.
 */

public class Event {
    String sport;
    String date;
    String location;
    String time;
    String URL;
    Calendar calendar;
    String imageUrl;
    int type;

    public Event(String _sport, String _date, String _location, String _time, String _URL, String imageUrl, Calendar calendar, int type) {
        this.sport = _sport;
        this.date = _date;
        this.location = _location;
        this.time = _time;
        this.URL = _URL;
        this.imageUrl = imageUrl;
        this.calendar = calendar;
        this.type = type;
    }

    public String getSport() {
        return sport;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getTime() {
        return time;
    }

    public String getURL() {
        return URL;
    }

    public String getImageUrl() { return  imageUrl; }

    public Calendar getCalendar() {return calendar;}

    public int getType() {return type;}
}
