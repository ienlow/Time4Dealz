package com.example.isaacenlow.time4dealz;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.ByteArrayOutputStream;
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
    List<Teams> list;

    public TeamAdapter(Context context, int resource, List<Teams> items) {
        super(context, resource, items);
        this.context = context;
        this.list = items;
    }

    public class Holder {
        TextView sport_holder;
        ImageView team_holder;
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
            holder.team_holder = (ImageView) view.findViewById(R.id.imageView2);
            view.setTag(holder);
        }
        else
            holder = (Holder) view.getTag();
        holder.sport_holder.setText(teamItem.getPlace() + " " + teamItem.getTeam());
        // https://bumptech.github.io/glide/doc/getting-started.html
        Glide
                .with(context)
                .load(teamItem.getURL())
                .into(holder.team_holder);
        return view;
    }
}
