package com.example.isaacenlow.time4dealz;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import java.util.List;

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

    public TeamAdapter(Context context, int resource, List<Event> items) {
        super(context, resource, items);
        this.context = context;
        this.list = items;
    }

    public class Holder {
        TextView sport_holder;
        ImageView opponent_holder;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder = null;
        Event teamItem = getItem(i);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (i >= 1) {
            view = mInflater.inflate(R.layout.adapter_layout, null);
            holder = new Holder();
            holder.sport_holder = view.findViewById(R.id.place);
            holder.opponent_holder = view.findViewById(R.id.imageView2);
            view.setTag(holder);
        }
        else {
            view = mInflater.inflate(R.layout.adapter_layout2, null);
            holder = new Holder();
            holder.sport_holder = view.findViewById(R.id.place2);
            holder.opponent_holder = view.findViewById(R.id.imageView3);
            view.setTag(holder);
        }

        sportDate = teamItem.getSportDate();
        opponentLocation = teamItem.getOpponentLocation();
        holder.sport_holder.setText(teamItem.getSportDate() + " " + teamItem.getOpponentLocation());
        // https://bumptech.github.io/glide/doc/getting-started.html
        Glide
                .with(context)
                .load(teamItem.getURL())
                .into(holder.opponent_holder);
        return view;
    }
}
