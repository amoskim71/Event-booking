package com.gacsoft.letsmeethere;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Gacsoft on 9/10/2016.
 */
public class EventSynchronizer {
    Context context;
    ProgressDialog progress;
    SyncDoneListener doneListener;

    public interface SyncDoneListener {
        abstract public void syncDone();
    }

    public EventSynchronizer(Context context, SyncDoneListener doneListener) {
        this.context = context;
        this.doneListener = doneListener;
        progress = new ProgressDialog(context);
        progress.setCancelable(false);
    }

    public void synchronize() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            return;
        }
        //upload pending changes made offline
        uploadPending();

        //get updates from server
        downloadSync();
    }

    private void uploadPending() {
        EventDatabase db = new EventDatabase(context);
        List<Event> events = db.getEvents();

        for (Event event : events) {
            if (event.isNew()) {
                uploadNew(event);
                continue;
            }
            if (event.isModified()) {
                uploadUpdate(event);
            }
        }
        progress.dismiss();
    }

    private void uploadNew(Event _event) {
        final Event event = _event;
        progress.setMessage(context.getString(R.string.synchronizing));
        progress.show();

        String tag_req = "req_syncNew";
        StringRequest req = new StringRequest(Request.Method.POST, AppConfig.NEWEVENT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            System.out.println(response);
                            JSONObject resp = new JSONObject(response);

                            boolean error = resp.getBoolean("error");
                            progress.hide();
                            if (!error) {
                                //Toast.makeText(context, context.getString(R.string.uploaded), Toast.LENGTH_LONG).show();
                                event.setId(resp.getString("id"));
                                EventDatabase db = new EventDatabase(context);
                                event.setNew(false);
                                event.setModified(false);
                                db.update(event);

                            } else { //error happened
                                //TODO if server says session expired, prompt a new login
                                String errorMessage = resp.getString("error_msg");
                                if (errorMessage.equals("BAD_SESSION")) {
                                    Toast.makeText(context, R.string.BAD_SESSION, Toast.LENGTH_LONG).show();
                                    SessionManager.getInstance().logOut();
                                } else if (errorMessage.equals("BAD_EMAIL")) {
                                    Toast.makeText(context, R.string.BAD_EMAIL, Toast.LENGTH_LONG).show();
                                } else if (errorMessage.equals("BAD_PARAMS")) {
                                    Toast.makeText(context, R.string.BAD_PARAMS, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, R.string.BAD_SOMETHING, Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        catch (JSONException e) {
                            progress.hide();
                            Toast.makeText(context, R.string.BAD_SOMETHING, Toast.LENGTH_LONG).show();
                            e.printStackTrace(); //TODO ...
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.hide();
                if (error.networkResponse != null) {
                    System.out.println("Error while uploading: " + error.networkResponse + error.getMessage());
                    Toast.makeText(context, "Error while uploading: " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, context.getString(R.string.cannotConnect), Toast.LENGTH_LONG).show();
                }
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                super.getParams();
                Map<String, String> params = new HashMap<String, String>();
                SessionManager session = SessionManager.getInstance();
                params.put("email", session.getEmail());
                params.put("sessionID", session.getSessionID());
                params.put("eventID", event.getId());
                params.put("name", event.getName());
                params.put("when", Long.toString(event.getWhen().getTime()));
                params.put("long", Double.toString(event.getLongitude()));
                params.put("lat", Double.toString(event.getLatitude()));
                System.out.println(params);
                return params;
            }
        };
        AppControl.getInstance().addToRequestQueue(req, tag_req);
    }

    private void uploadUpdate(Event event) {
        //TODO ..
    }

    private void downloadSync() {
        String tag_req = "req_syncFull";
        StringRequest req = new StringRequest(Request.Method.POST, AppConfig.SYNC_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            System.out.println(response);
                            JSONObject resp = new JSONObject(response);

                            boolean error = resp.getBoolean("error");
                            progress.hide();
                            if (!error) {
                                JSONArray events = resp.getJSONArray("events");
                                EventDatabase db = new EventDatabase(context);
                                db.clear();
                                for (int i = 0; i < events.length(); i++) {
                                    JSONObject e = events.getJSONObject(i);
                                    Event event = new Event(e.getString("id"),
                                            e.getString("name"),
                                            e.getInt("owned") == 1,
                                            new Date(e.getLong("whenEvent")),
                                            e.getLong("longitude"),
                                            e.getLong("latitude"));
                                    db.addEvent(event);
                                }
                                JSONArray comments = resp.getJSONArray("comments");
                                for (int i = 0; i < comments.length(); i++) {
                                    JSONObject c = comments.getJSONObject(i);
                                    Comment comment = new Comment(
                                            c.getString("eventid"),
                                            c.getString("name"),
                                            c.getString("email"),
                                            new Date(c.getLong("cdate")),
                                            c.getString("post"));
                                    db.addComment(comment);
                                }
                                doneListener.syncDone();

                            } else { //error happened
                                //TODO if server says session expired, prompt a new login
                                String errorMessage = resp.getString("error_msg");
                                if (errorMessage.equals("BAD_SESSION")) {
                                    Toast.makeText(context, R.string.BAD_SESSION, Toast.LENGTH_LONG).show();
                                    SessionManager.getInstance().logOut();
                                } else if (errorMessage.equals("BAD_EMAIL")) {
                                    Toast.makeText(context, R.string.BAD_EMAIL, Toast.LENGTH_LONG).show();
                                } else if (errorMessage.equals("BAD_PARAMS")) {
                                    Toast.makeText(context, R.string.BAD_PARAMS, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, R.string.BAD_SOMETHING, Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        catch (JSONException e) {
                            progress.hide();
                            Toast.makeText(context, R.string.BAD_SOMETHING, Toast.LENGTH_LONG).show();
                            e.printStackTrace(); //TODO ...
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.hide();
                if (error.networkResponse != null) {
                    System.out.println("Error while uploading: " + error.networkResponse +  error.getMessage());
                    Toast.makeText(context, "Error while uploading: " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, context.getString(R.string.cannotConnect), Toast.LENGTH_LONG).show();
                }

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                super.getParams();
                Map<String, String> params = new HashMap<String, String>();
                SessionManager session = SessionManager.getInstance();
                params.put("email", session.getEmail());
                params.put("sessionID", session.getSessionID());
                String token = FirebaseInstanceId.getInstance().getToken();
                if (token != null) {
                    params.put("token", token);
                    if (AppConfig.DEBUG) System.out.println("Token: " + token);
                } else {
                    params.put("token", "");
                }
                if (AppConfig.DEBUG) System.out.println(params);
                return params;
            }
        };
        AppControl.getInstance().addToRequestQueue(req, tag_req);
    }


}
