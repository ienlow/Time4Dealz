package com.example.isaacenlow.time4dealz;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Isaac Enlow on 12/5/2016.
 */

/**
 * Creates custom adapter for list.
 */
public class TeamAdapter extends ArrayAdapter<Teams> {
    Context context;
    Drawable drawable;

    public TeamAdapter(Context context, int resource, List<Teams> items) {
        super(context, resource, items);
        this.context = context;
    }

    public class Holder {
        TextView sport_holder;
        //TextView team_holder;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder = null;
        Teams teamItem = getItem(i);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = mInflater.inflate(R.layout.adapter_layout, null);
            holder = new Holder();
            holder.sport_holder = (TextView) view.findViewById(R.id.place);
            //holder.team_holder = (TextView) view.findViewById(R.id.team);
            view.setTag(holder);
        }
        else
            holder = (Holder) view.getTag();
        holder.sport_holder.setText(teamItem.getPlace() + " " + teamItem.getTeam());
       // holder.team_holder.setText(teamItem.getTeam());
        return view;
    }
}
