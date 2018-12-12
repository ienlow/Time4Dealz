package com.example.isaacenlow.time4dealz;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainTeamAdapter extends RecyclerView.Adapter<MainTeamAdapter.ViewHolder> {
    Context context;
    Drawable drawable;
    ArrayList<String> list;
    LayoutInflater inflater;

    public MainTeamAdapter(Context context, int resource, ArrayList<String> _list) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        list = _list;
       //Toast.makeText(context, "test", Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View teamView = inflater.inflate(R.layout.recycler_row, parent, false);
        return new ViewHolder(teamView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //String event = list.get(position).getSportDate();
        //int event = list[position];
        Toast.makeText(context, "test", Toast.LENGTH_SHORT).show();
        holder.sport_holder.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView sport_holder;
        ViewHolder(View itemView) {
            super(itemView);
            sport_holder = itemView.findViewById(R.id.row1);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
