package com.example.isaacenlow.time4dealz;

import android.graphics.Bitmap;
import android.media.Image;
import android.widget.ImageView;

/**
 * Created by Isaac Enlow on 12/5/2016.
 */

public class Teams {
    String place;
    String team;
    String URL;
    String index;

    public Teams(String place, String team, String _URL) {
        this.place = place;
        this.team = team;
        this.URL = _URL;
    }

    public String getindex() {
        return index;
    }

    public String getPlace() {
        return place;
    }

    public String getTeam() {
        return team;
    }

    public String getURL() {
        return URL;
    }
}
