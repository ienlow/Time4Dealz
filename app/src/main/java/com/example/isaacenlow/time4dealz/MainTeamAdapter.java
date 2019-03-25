package com.example.isaacenlow.time4dealz;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MainTeamAdapter extends RecyclerView.Adapter<MainTeamAdapter.MyViewHolder> {
    Context context;
    Drawable drawable;
    ArrayList<Event> list;
    LayoutInflater inflater;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ImageView opponentHolder;
        public MyViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.row1);
            opponentHolder = v.findViewById(R.id.row2);
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
                .inflate(R.layout.recycler_row, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        Event teamItem = list.get(position);
        //Log.d("item", teamItem.getSportDate());
        //holder.mTextView.setText(teamItem.getSportDate() + " " + teamItem.getOpponentLocation());
        Glide
                .with(context)
                .load(teamItem.getURL())
                .into(holder.opponentHolder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CurrentGame.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
