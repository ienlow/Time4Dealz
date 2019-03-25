package com.example.isaacenlow.time4dealz;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Isaac Enlow on 12/5/2016.
 */

/**
 * Creates custom adapter for list.
 */
public class TeamAdapter extends ArrayAdapter<Event> {
    Context context;
    Drawable drawable;
    List<Event> list;
    String sportDate = "", opponentLocation = "";
    int j;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    TeamAdapter(Context context, int resource, List<Event> items, SharedPreferences preferences, SharedPreferences.Editor editor) {
        super(context, resource, items);
        this.context = context;
        this.list = items;
        this.preferences = preferences;
        this.editor = editor;
    }

    public class Holder {
        TextView sport_holder;
        TextView date_holder;
        TextView opponent_holder;
        TextView location_holder;
        TextView time_holder;
        ImageView opponentimage_holder;
        TextView place;
        TextView upcoming_events;
        TextView current_event;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder = null;

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (i < 1) {
            view = mInflater.inflate(R.layout.adapter_layout2, null);
            holder = new Holder();
            holder.current_event = view.findViewById(R.id.current_event);
            holder.sport_holder = view.findViewById(R.id.team_sport2);
            holder.date_holder = view.findViewById(R.id.team_date2);
            holder.opponent_holder = view.findViewById(R.id.team_opponent2);
            holder.location_holder = view.findViewById(R.id.team_location2);
            holder.time_holder = view.findViewById(R.id.team_time2);
            holder.opponentimage_holder = view.findViewById(R.id.imageView3);
            holder.place = view.findViewById(R.id.place2);
        }
        else {
            view = mInflater.inflate(R.layout.adapter_layout, null);
            holder = new Holder();
            holder.upcoming_events = view.findViewById(R.id.upcoming_events);
            holder.sport_holder = view.findViewById(R.id.team_sport);
            holder.date_holder = view.findViewById(R.id.team_date);
            holder.opponent_holder = view.findViewById(R.id.team_opponent);
            holder.location_holder = view.findViewById(R.id.team_location);
            holder.time_holder = view.findViewById(R.id.team_time);
            holder.opponentimage_holder = view.findViewById(R.id.imageView);
            holder.place = view.findViewById(R.id.place);
        }

        Event teamItem = getItem(i);
        Calendar calendar1 = Calendar.getInstance();
        view.setTag(holder);
        if (preferences.getBoolean("show upcoming", false) && list.get(++i).getCalendar().after(calendar1)) {
            holder.upcoming_events.setVisibility(View.VISIBLE);
            editor.putBoolean("show upcoming", false);
            editor.apply();
        } else if (teamItem.getCalendar().before(calendar1) && holder.upcoming_events != null){
            holder.upcoming_events.setVisibility(View.GONE);
            editor.putBoolean("show upcoming", true);
            editor.apply();
        }
        holder.sport_holder.setText(teamItem.getSport());
        holder.date_holder.setText(teamItem.getDate());
        if (holder.opponent_holder != null) {
            holder.opponent_holder.setText(teamItem.getOpponent());
            holder.location_holder.setText(teamItem.getLocation());
            holder.time_holder.setText(teamItem.getTime());
        }
        // https://bumptech.github.io/glide/doc/getting-started.html
        Glide
                .with(context)
                .load(teamItem.getURL())
                .into(holder.opponentimage_holder);
        return view;
    }
}
