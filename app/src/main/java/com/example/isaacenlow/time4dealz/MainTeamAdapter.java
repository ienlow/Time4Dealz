package com.example.isaacenlow.time4dealz;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Scanner;

public class MainTeamAdapter extends RecyclerView.Adapter<MainTeamAdapter.MyViewHolder> {
    Context context;
    Drawable drawable;
    ArrayList<Event> list;
    LayoutInflater inflater;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView sport_holder;
        TextView date_holder;
        TextView opponent_holder;
        TextView location_holder;
        TextView time_holder;
        ImageView opponentimage_holder;
        TextView place;
        TextView upcoming_events;
        public MyViewHolder(View v) {
            super(v);
            upcoming_events = v.findViewById(R.id.upcoming_events);
            sport_holder = v.findViewById(R.id.team_sport);
            date_holder = v.findViewById(R.id.team_date);
            location_holder = v.findViewById(R.id.team_location);
            opponentimage_holder = v.findViewById(R.id.imageView);
            place = v.findViewById(R.id.place);
        }
    }

    public MainTeamAdapter(Context context, ArrayList<Event> _list) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        list = _list;
    }

    @NonNull
    @Override
    public MainTeamAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_menu_recycler_row, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        Event teamItem = list.get(position);
        //Log.d("item", teamItem.getSportDate());
        //holder.mTextView.setText(teamItem.getSportDate() + " " + teamItem.getOpponentLocation());
        holder.sport_holder.setText(list.get(position).getSport());
        holder.date_holder.setText(list.get(position).getDate());
        holder.location_holder.setText(list.get(position).getLocation());
        /*int hour = 0, minute = 0;
        Scanner scanner = new Scanner(list.get(position).getTime());
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
        scanner.close();*/
        Glide
                .with(context)
                .load(teamItem.getURL())
                .into(holder.opponentimage_holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CurrentGame.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}