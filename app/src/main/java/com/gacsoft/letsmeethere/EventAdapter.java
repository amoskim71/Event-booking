package com.gacsoft.letsmeethere;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Gacsoft on 9/10/2016.
 */
public class EventAdapter extends BaseAdapter {
    private List<Event> events;
    private Context context;
    private static LayoutInflater inflater=null;

    static class Holder {
        TextView name;
        TextView date;
        TextView time;
    }

    public EventAdapter(MainActivity mainActivity, List<Event> events) {
        this.events = events;
        context = mainActivity;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int i) {
        return events.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View reuseView, ViewGroup viewGroup) {
        View row = reuseView;

        if (reuseView == null) {
            row = inflater.inflate(R.layout.event_list_item, null);
            Holder holder = new Holder();
            holder.name = (TextView) row.findViewById(R.id.list_item_name);
            holder.date = (TextView) row.findViewById(R.id.list_item_date);
            holder.time = (TextView) row.findViewById(R.id.list_item_time);
            row.setTag(holder);
        }
        Holder holder = (Holder) row.getTag();
        holder.name.setText(events.get(i).getName());
        Date when = events.get(i).getWhen();
        if (DateUtils.isToday(when.getTime())) {
            holder.date.setText(context.getString(R.string.today));
            holder.date.setTextColor(context.getResources().getColor(R.color.red));
        } else {
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
            holder.date.setText(df.format(when));
            holder.date.setTextColor(context.getResources().getColor(R.color.black));
        }
        holder.time.setText(android.text.format.DateFormat.format("kk:mm", when));
        return row;
    }
}
