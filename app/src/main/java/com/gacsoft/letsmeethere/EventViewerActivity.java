package com.gacsoft.letsmeethere;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Security;
import java.text.DateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class EventViewerActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener{

    private boolean editing;
    private boolean newEvent;
    Event event;
    EditText nameEdit;
    EditText dateEdit;
    EditText timeEdit;
    KeyListener nameEditListener;
    Button editButton;
    Button cancelButton;
    Button saveButton;
    Button inviteButton;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    String mLatitudeText;
    String mLongitudeText;
    GoogleMap mMap;
    Marker eventMarker;
    ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer);
        nameEdit = (EditText) findViewById(R.id.edit_Event_Name);
        dateEdit = (EditText) findViewById(R.id.edit_Event_Date);
        timeEdit = (EditText) findViewById(R.id.edit_Event_Time);
        editButton = (Button) findViewById(R.id.editButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        saveButton = (Button) findViewById(R.id.createButton);
        inviteButton = (Button) findViewById(R.id.inviteButton);
        nameEditListener = nameEdit.getKeyListener();

        progress = new ProgressDialog(this);
        progress.setCancelable(false);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                nameChanged();
            }
        };

        nameEdit.addTextChangedListener(tw);

        event = AppControl.eventStorage;
        if (event == null) finish(); //should never happen, but let's not crash
        if (event.getId() == null) { //creating new event
            inviteButton.setVisibility(View.INVISIBLE);
            event.setOwned(true);
            newEvent = true;
            setEditing(true);
            Date now = Calendar.getInstance().getTime();
            event.setWhen(now);
            event.setId(UUID.randomUUID().toString());
            DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
            dateEdit.setText(df.format(now));
            timeEdit.setText(android.text.format.DateFormat.format("kk:mm", now));
        } else { //viewing existing event
            if (event.isOwned()) {
                inviteButton.setVisibility(View.VISIBLE);
            } else {
                editButton.setVisibility(View.INVISIBLE);
            }
            saveButton.setText(R.string.save);
            setEditing(false);
            newEvent = false;
            loadFields();
        }
    }

    private void loadFields() {
        nameEdit.setText(event.getName());
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        dateEdit.setText(df.format(event.getWhen()));
        timeEdit.setText(android.text.format.DateFormat.format("kk:mm", event.getWhen()));
    }

    private void nameChanged() {
        String newName = nameEdit.getText().toString();
        event.setName(newName);
        if (eventMarker != null) eventMarker.setTitle(newName);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        progress.dismiss();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        map.setOnMapClickListener(this);
        setInitialMapMarker();
    }

    private void setInitialMapMarker() {
        if (eventMarker != null) return;
        if (mMap == null) return;

        if (newEvent) {
            if (mLastLocation == null) return;
            eventMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                .title("New Event")
                .draggable(false));
            eventMarker.showInfoWindow();
            event.setLongitude(mLastLocation.getLongitude());
            event.setLatitude(mLastLocation.getLatitude());
        } else { //existing event
            eventMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(event.getLatitude(), event.getLongitude()))
                    .title(event.getName())
                    .draggable(false));
            eventMarker.showInfoWindow();
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(event.getLatitude(), event.getLongitude()), 16);
        mMap.animateCamera(cameraUpdate);

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            setInitialMapMarker();
        } catch (SecurityException e) {}
    }

    @Override
    public void onMapClick(LatLng point) {
        if (!editing) return;
        if (mMap == null) return;
        if (eventMarker == null) {
            String name = nameEdit.getText().toString();
            if (name.equals("")) name = "New Event";
            eventMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(point.latitude, point.longitude))
                    .title(name)
                    .draggable(false));
        } else {
            eventMarker.setPosition(point);
        }
        eventMarker.showInfoWindow();
        event.setLongitude(point.longitude);
        event.setLatitude(point.latitude);
    }

    private void setEditing(boolean isEditing){
        if (isEditing) {
            editButton.setVisibility(View.INVISIBLE);
            inviteButton.setVisibility(View.INVISIBLE);
            if (!event.isOwned()) return;
            editing = true;
            saveButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            nameEdit.setKeyListener(nameEditListener);
        } else {
            editing = false;
            saveButton.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.INVISIBLE);
            if (event.isOwned()) {
                inviteButton.setVisibility(View.VISIBLE);
                editButton.setVisibility(View.VISIBLE);
            }
            nameEdit.setKeyListener(null);
        }
    }

    public void editClicked(View view) {
        if (editing) return;
        if (!event.isOwned()) {
            Toast.makeText(getApplicationContext(), getString(R.string.editNotAllowed), Toast.LENGTH_LONG).show();
            return;
        }
        setEditing(true);
    }

    public void cancelClicked(View view) {
        if (newEvent) {
            finish();
        } else {
            setEditing(false);
            loadFields();
        }
    }

    public void createClicked(View view) {
        if (!editing) return;
        if (newEvent) create();
        else save();
    }

    private void create() { //create new event
        if (!SessionManager.getInstance().isLoggedIn()) {
            Toast.makeText(getApplicationContext(), R.string.saveOffline, Toast.LENGTH_LONG).show();
            event.setId("");
            event.setNew(true);
            EventDatabase db = new EventDatabase(getApplicationContext());
            db.addEvent(event);
            setEditing(false);
        } else { //logged in

            progress.setMessage(getString(R.string.uploading));
            progress.show();

            String tag_req = "req_uploadNew";
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
                                    Toast.makeText(getApplicationContext(), getString(R.string.uploaded), Toast.LENGTH_LONG).show();
                                    event.setId(resp.getString("id"));
                                    setEditing(false);
                                    newEvent = false;
                                    EventDatabase db = new EventDatabase(getApplicationContext());
                                    db.addEvent(event);
                                } else { //error happened
                                    //TODO if server says session expired, prompt a new login
                                    String errorMessage = resp.getString("error_msg");
                                    if (errorMessage.equals("BAD_SESSION")) {
                                        Toast.makeText(getApplicationContext(), R.string.BAD_SESSION, Toast.LENGTH_LONG).show();
                                        SessionManager.getInstance().logOut();
                                    } else if (errorMessage.equals("BAD_EMAIL")) {
                                        Toast.makeText(getApplicationContext(), R.string.BAD_EMAIL, Toast.LENGTH_LONG).show();
                                    } else if (errorMessage.equals("BAD_PARAMS")) {
                                        Toast.makeText(getApplicationContext(), R.string.BAD_PARAMS, Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), R.string.BAD_SOMETHING, Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                            catch (JSONException e) {
                                progress.hide();
                                Toast.makeText(getApplicationContext(), R.string.BAD_SOMETHING, Toast.LENGTH_LONG).show();
                                e.printStackTrace(); //TODO ...
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progress.hide();
                    if (error.networkResponse != null) {
                        System.out.println("Error while uploading: " + error.networkResponse +  error.getMessage());
                        Toast.makeText(getApplicationContext(), "Error while uploading: " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.cannotConnect), Toast.LENGTH_LONG).show();
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
    }

    private void save() { //store existing event
        if (!SessionManager.getInstance().isLoggedIn()) {
            Toast.makeText(getApplicationContext(), R.string.saveOffline, Toast.LENGTH_LONG).show();
            event.setModified(true);
            EventDatabase db = new EventDatabase(getApplicationContext());
            db.update(event);
            setEditing(false);
        } else { //logged in

            progress.setMessage(getString(R.string.uploading));
            progress.show();
            event.setName(nameEdit.getText().toString());

            String tag_req = "req_uploadExisting";
            StringRequest req = new StringRequest(Request.Method.POST, AppConfig.UPDATE_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                System.out.println(response);
                                JSONObject resp = new JSONObject(response);

                                boolean error = resp.getBoolean("error");
                                progress.hide();
                                if (!error) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.uploaded), Toast.LENGTH_LONG).show();
                                    setEditing(false);
                                    EventDatabase db = new EventDatabase(getApplicationContext());
                                    db.update(event);
                                } else { //error happened
                                    //TODO if server says session expired, prompt a new login
                                    String errorMessage = resp.getString("error_msg");
                                    if (errorMessage.equals("BAD_SESSION")) {
                                        Toast.makeText(getApplicationContext(), R.string.BAD_SESSION, Toast.LENGTH_LONG).show();
                                        SessionManager.getInstance().logOut();
                                    } else if (errorMessage.equals("BAD_EMAIL")) {
                                        Toast.makeText(getApplicationContext(), R.string.BAD_EMAIL, Toast.LENGTH_LONG).show();
                                    } else if (errorMessage.equals("BAD_OWNER")) {
                                        Toast.makeText(getApplicationContext(), R.string.editNotAllowed, Toast.LENGTH_LONG).show();
                                    } else if (errorMessage.equals("BAD_PARAMS")) {
                                        Toast.makeText(getApplicationContext(), R.string.BAD_PARAMS, Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), R.string.BAD_SOMETHING, Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                            catch (JSONException e) {
                                progress.hide();
                                Toast.makeText(getApplicationContext(), R.string.BAD_SOMETHING, Toast.LENGTH_LONG).show();
                                e.printStackTrace(); //TODO ...
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progress.hide();
                    if (error.networkResponse != null) {
                        System.out.println("Error while uploading: " + error.networkResponse +  error.getMessage());
                        Toast.makeText(getApplicationContext(), "Error while uploading: " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.cannotConnect), Toast.LENGTH_LONG).show();
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
    }

    public void inviteClicked(View view) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            Toast.makeText(this, getString(R.string.inviteNoLogin), Toast.LENGTH_LONG).show();
            return;
        }
        AppControl.eventStorage = event;
        Intent intent = new Intent(this, InviteActivity.class);
        startActivity(intent);
    }


    public void dateClicked(View view) {
        if (!editing) return;
        Date when = event.getWhen();

        int year = 1900 + when.getYear();
        int month = when.getMonth();
        int day = when.getDate();
        DatePickerDialog dp = new DatePickerDialog(this, this, year, month, day);
        dp.show();
    }

    public void timeClicked(View view) {
        if (!editing) return;
        Date when = event.getWhen();

        int hour = when.getHours();
        int minutes = when.getMinutes();
        TimePickerDialog tp = new TimePickerDialog(this, this, hour, minutes, true);
        tp.show();
    }


    public void onDateSet(DatePicker view, int year, int month, int day) {
        Date when = event.getWhen();
        when.setYear(year - 1900);
        when.setMonth(month);
        when.setDate(day);

        event.setWhen(when);
        loadFields();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
        Date when = event.getWhen();
        when.setHours(hours);
        when.setMinutes(minutes);

        event.setWhen(when);
        loadFields();
    }

    public void onCommentsClicked(View view) {
        Intent intent = new Intent(this, CommentsActivity.class);
        startActivity(intent);
        finish();
    }
}
