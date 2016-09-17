package com.gacsoft.letsmeethere;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.util.TypedValue;
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
public class CommentAdapter extends BaseAdapter {
    private List<Comment> comments;
    private Context context;
    private static LayoutInflater inflater=null;
    int padding;

    static class Holder {
        TextView name;
        TextView date;
        TextView email;
        TextView post;
        View leftPadding;
        View rightPadding;
    }

    public CommentAdapter(CommentsActivity activity, List<Comment> comments) {
        this.comments = comments;
        context = activity;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resources r = context.getResources();
        padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, r.getDisplayMetrics());
    }

    @Override
    public int getCount() {
        return comments.size();
    }

    @Override
    public Object getItem(int i) {
        return comments.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View reuseView, ViewGroup viewGroup) {
        View row = reuseView;

        if (reuseView == null) {
            row = inflater.inflate(R.layout.comment_list_item, null);
            Holder holder = new Holder();
            holder.name = (TextView) row.findViewById(R.id.name);
            holder.date = (TextView) row.findViewById(R.id.date);
            holder.email = (TextView) row.findViewById(R.id.email);
            holder.post = (TextView) row.findViewById(R.id.post);
            holder.leftPadding = (View) row.findViewById(R.id.leftPadding);
            holder.rightPadding = (View) row.findViewById(R.id.rightPadding);
            row.setTag(holder);
        }
        Holder holder = (Holder) row.getTag();
        holder.name.setText(comments.get(i).getName());
        String timestamp;
        Date when = comments.get(i).getWhen();
        if (DateUtils.isToday(when.getTime())) {
            timestamp = context.getString(R.string.today);
        } else {
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
            timestamp =df.format(when);
        }
        timestamp = timestamp.concat(", " + android.text.format.DateFormat.format("kk:mm", when).toString());
        holder.date.setText(timestamp);
        holder.email.setText("<" + comments.get(i).getEmail() + ">");
        holder.post.setText(comments.get(i).getPost());
        if (SessionManager.getInstance().getEmail().equals(comments.get(i).getEmail())) {
            ViewGroup.LayoutParams paramsL = holder.leftPadding.getLayoutParams();
            paramsL.width = 0;
            holder.leftPadding.setLayoutParams(paramsL);
            ViewGroup.LayoutParams paramsR = holder.rightPadding.getLayoutParams();
            paramsR.width = padding;
            holder.rightPadding.setLayoutParams(paramsR);
//            holder.leftPadding.requestLayout();
//            View c = (View) row.findViewById(R.id.commentField);
//            c.requestLayout();
//            holder.rightPadding.requestLayout();
        } else {
            ViewGroup.LayoutParams paramsL = holder.leftPadding.getLayoutParams();
            paramsL.width = padding;
            holder.leftPadding.setLayoutParams(paramsL);
            ViewGroup.LayoutParams paramsR = holder.rightPadding.getLayoutParams();
            paramsR.width = 0;
            holder.rightPadding.setLayoutParams(paramsR);
//            holder.leftPadding.requestLayout();
//            View c = (View) row.findViewById(R.id.commentField);
//            c.requestLayout();
//            holder.rightPadding.requestLayout();
        }
        return row;
    }
}
