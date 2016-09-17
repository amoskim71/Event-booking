package com.gacsoft.letsmeethere;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Gacsoft on 9/5/2016.
 */
public class EventDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "letsmeethere";
    private static final int DATABASE_VERSION = 9;
    private static final String EVENTS_TABLE = "events";
    private static final String COMMENTS_TABLE = "comments";

    private static final String KEY_KEY = "id"; //db key
    private static final String KEY_ID = "eventid"; //uuid of event
    private static final String KEY_NAME = "name"; //name of event
    private static final String KEY_OWNED = "owned"; //can this user modify this event? (is he owner)
    private static final String KEY_WHEN = "whenEvent";
    private static final String KEY_LONGITUDE = "longitude"; //geological location of event
    private static final String KEY_LATITUDE = "latitude"; //geological location of event
    private static final String KEY_ISNEW = "isnew"; //geological location of event
    private static final String KEY_ISMODIFIED = "ismodified"; //geological location of event

    private static final String KEY_EMAIL = "email";
    private static final String KEY_POST = "post"; //text of post

    public EventDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createEventsTable =
                "CREATE TABLE " + EVENTS_TABLE + "("
                + KEY_KEY + " TEXT PRIMARY KEY,"
                + KEY_ID + " TEXT,"
                + KEY_NAME + " TEXT,"
                + KEY_OWNED + " INTEGER,"
                + KEY_WHEN + " INTEGER,"
                + KEY_LONGITUDE + " REAL,"
                + KEY_LATITUDE + " REAL,"
                + KEY_ISNEW + " INTEGER,"
                + KEY_ISMODIFIED + " INTEGER" + ")";
        db.execSQL(createEventsTable);

        String createCommentsTable =
                "CREATE TABLE " + COMMENTS_TABLE + "("
                        + KEY_KEY + " TEXT PRIMARY KEY,"
                        + KEY_ID + " TEXT,"
                        + KEY_NAME + " TEXT,"
                        + KEY_EMAIL + " TEXT,"
                        + KEY_WHEN + " INTEGER,"
                        + KEY_POST + " TEXT" + ")";
        db.execSQL(createCommentsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + COMMENTS_TABLE);
        onCreate(db);
    }

    public void addEvent(Event newEvent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_KEY, newEvent.getKey());
        values.put(KEY_ID, newEvent.getId());
        values.put(KEY_NAME, newEvent.getName());
        values.put(KEY_OWNED, newEvent.isOwned());
        values.put(KEY_WHEN, newEvent.getWhen().getTime());
        values.put(KEY_LONGITUDE, newEvent.getLongitude());
        values.put(KEY_LATITUDE, newEvent.getLatitude());
        values.put(KEY_ISNEW, newEvent.isNew());
        values.put(KEY_ISMODIFIED, newEvent.isModified());
        db.insert(EVENTS_TABLE, null, values);
        db.close();
    }

    public void addComment(Comment comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_KEY, comment.getKey());
        values.put(KEY_ID, comment.getEvent());
        values.put(KEY_NAME, comment.getName());
        values.put(KEY_EMAIL, comment.getEmail());
        values.put(KEY_WHEN, comment.getWhen().getTime());
        values.put(KEY_POST, comment.getPost());
        db.insert(COMMENTS_TABLE, null, values);
        db.close();
    }

    public List<Comment> getComments(String eventId) {
        List<Comment> comments = new ArrayList<Comment>();
        String selectEvents = "SELECT * FROM " + COMMENTS_TABLE
                + " WHERE " + KEY_ID + " = '" + eventId + "'"
                + " ORDER BY " + KEY_WHEN + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectEvents, null);

        while (cursor.moveToNext()) {
            Comment comment = new Comment(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    new Date(cursor.getLong(4)),
                    cursor.getString(5));
            comments.add(comment);
        }
        cursor.close();
        db.close();
        return comments;
    }

    public List<Event> getEvents() {
        List<Event> events = new ArrayList<Event>();
        String selectEvents = "SELECT * FROM " + EVENTS_TABLE + " ORDER BY " + KEY_WHEN + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectEvents, null);

        while (cursor.moveToNext()) {
            Event event = new Event(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3) == 1,
                    new Date(cursor.getLong(4)),
                    cursor.getDouble(5),
                    cursor.getDouble(6));
            if (cursor.getInt(7) == 1) event.setNew(true);
            if (cursor.getInt(8) == 1) event.setModified(true);
            events.add(event);
        }

        cursor.close();
        db.close();
        return events;
    }

    public List<Event> getFutureEvents() {
        List<Event> events = new ArrayList<Event>();
        long now = Calendar.getInstance().getTime().getTime();
        String selectEvents = "SELECT * FROM " + EVENTS_TABLE +
                " WHERE " + KEY_WHEN + " > " + now +
                " ORDER BY " + KEY_WHEN + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectEvents, null);

        while (cursor.moveToNext()) {
            Event event = new Event(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3) == 1,
                    new Date(cursor.getLong(4)),
                    cursor.getDouble(5),
                    cursor.getDouble(6));
            if (cursor.getInt(7) == 1) event.setNew(true);
            if (cursor.getInt(8) == 1) event.setModified(true);
            events.add(event);
            System.out.println(event.getLatitude());
        }

        cursor.close();
        db.close();
        return events;
    }

    public List<Event> getPastEvents() {
        List<Event> events = new ArrayList<Event>();
        long now = Calendar.getInstance().getTime().getTime();
        String selectEvents = "SELECT * FROM " + EVENTS_TABLE +
                " WHERE " + KEY_WHEN + " < " + now +
                " ORDER BY " + KEY_WHEN + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectEvents, null);

        while (cursor.moveToNext()) {
            Event event = new Event(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3) == 1,
                    new Date(cursor.getLong(4)),
                    cursor.getDouble(5),
                    cursor.getDouble(6));
            if (cursor.getInt(7) == 1) event.setNew(true);
            if (cursor.getInt(8) == 1) event.setModified(true);
            events.add(event);
        }

        cursor.close();
        db.close();
        return events;
    }

    public void update(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();

        String filter = KEY_KEY + " = '" + event.getKey() + "'";

        ContentValues values = new ContentValues();
        values.put(KEY_KEY, event.getKey());
        values.put(KEY_ID, event.getId());
        values.put(KEY_NAME, event.getName());
        values.put(KEY_OWNED, event.isOwned() ? 1 : 0);
        values.put(KEY_WHEN, event.getWhen().getTime());
        values.put(KEY_LONGITUDE, event.getLongitude());
        values.put(KEY_LATITUDE, event.getLatitude());
        values.put(KEY_ISNEW, event.isNew() ? 1 : 0 );
        values.put(KEY_ISMODIFIED, event.isModified() ? 1 : 0);

        db.update(EVENTS_TABLE, values, filter, null);
        db.close();
    }

    public void clear() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + EVENTS_TABLE);
        db.execSQL("DELETE FROM " + COMMENTS_TABLE);
    }
}
