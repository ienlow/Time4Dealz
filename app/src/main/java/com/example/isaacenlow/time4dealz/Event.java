package com.example.isaacenlow.time4dealz;

import android.graphics.Bitmap;
import android.media.Image;
import android.widget.ImageView;

/**
 * Created by Isaac Enlow on 12/5/2016.
 */

public class Event {
    String sport_date;
    String opponent_location;
    String URL;

    public Event(String _sport_date, String _opponent_location, String _URL) {
        this.sport_date = _sport_date;
        this.opponent_location = _opponent_location;
        this.URL = _URL;
    }

    public String getSportDate() {
        return sport_date;
    }

    public String getOpponentLocation() {
        return opponent_location;
    }

    public String getURL() {
        return URL;
    }
}
