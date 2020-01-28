package com.example.isaacenlow.time4dealz;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class RosterAdapter extends RecyclerView.Adapter<RosterAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Player> players;

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageButton playerImage;
        private MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.player_name);
            playerImage = itemView.findViewById(R.id.player_btn);
        }
    }

    RosterAdapter(Context _context, ArrayList<Player> _players) {
        context = _context;
        players = _players;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.roster_adapter_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Player player = players.get(position);
        holder.name.setText(player.name);
        Glide
                .with(context)
                .load(player.url)
                .apply(RequestOptions.centerCropTransform())
                .into(holder.playerImage);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }
}
