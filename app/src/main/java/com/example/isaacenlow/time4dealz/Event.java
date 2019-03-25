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
    String opponent;
    String location;
    String time;
    String URL;
    Calendar calendar;

    public Event(String _sport, String _date, String _opponent, String _location, String _time, String _URL, Calendar calendar) {
        this.sport = _sport;
        this.date = _date;
        this.opponent = _opponent;
        this.location = _location;
        this.time = _time;
        this.URL = _URL;
        this.calendar = calendar;
    }

    public String getSport() {
        return sport;
    }

    public String getDate() {
        return date;
    }

    public String getOpponent() {
        return opponent;
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

    public Calendar getCalendar() {return calendar;}
}
