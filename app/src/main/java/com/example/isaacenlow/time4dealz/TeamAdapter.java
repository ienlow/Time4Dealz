package com.example.isaacenlow.time4dealz;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
    List<Event> teams;

    /**
     * Get number of layouts being used
     * @return
     */
    @Override
    public int getViewTypeCount() {
        return 3;
    }

    /**
     * Get the position type
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return teams.get(position).type;
    }

    TeamAdapter(Context context, int resource, List<Event> teams) {
        super(context, resource, teams);
        this.context = context;
        this.teams = teams;
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
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder = null;
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            if (teams.get(i).type == 1) {
                view = mInflater.inflate(R.layout.current_events_adapter, null);
            } else if (teams.get(i).type == 2) {
                view = mInflater.inflate(R.layout.upcoming_events_adapter, null);
            } else {
                view = mInflater.inflate(R.layout.team_schedules_adapter, null);
            }
            holder = new Holder();
            holder.upcoming_events = view.findViewById(R.id.upcoming_events);
            holder.sport_holder = view.findViewById(R.id.team_sport);
            holder.date_holder = view.findViewById(R.id.team_date);
            holder.opponent_holder = view.findViewById(R.id.team_opponent);
            holder.location_holder = view.findViewById(R.id.team_location);
            holder.time_holder = view.findViewById(R.id.team_time);
            holder.opponentimage_holder = view.findViewById(R.id.imageView);
            holder.place = view.findViewById(R.id.place);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        if (holder != null) {
            holder.sport_holder.setText(teams.get(i).getSport());
            holder.date_holder.setText(teams.get(i).getDate());
            holder.opponent_holder.setText(teams.get(i).getOpponent());
            holder.location_holder.setText(teams.get(i).getLocation());
            int hour = 0, minute = 0;
            Scanner scanner = new Scanner(teams.get(i).getTime());
            scanner.useDelimiter(":");
            if (scanner.hasNext()) {
                hour = Integer.valueOf(scanner.next());
                minute = Integer.valueOf(scanner.next());
            }
            if (hour > 12 && minute == 0) {
                hour -= 12;
                holder.time_holder.setText(String.valueOf(hour + ":" + minute + "0 PM"));
            } else if (hour == 12 && minute == 0) {
                holder.time_holder.setText(String.valueOf(hour + ":" + minute + "0 PM"));
            }
            scanner.close();
            Glide
                    .with(context)
                    .load(teams.get(i).getURL())
                    .into(holder.opponentimage_holder);
        }
        // https://bumptech.github.io/glide/doc/getting-started.html
        return view;
    }
}
