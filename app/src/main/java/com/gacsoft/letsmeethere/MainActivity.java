package com.gacsoft.letsmeethere;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, EventSynchronizer.SyncDoneListener{
    ImageView logo;
    TextView username;
    Button button;
    Button futureButton;
    Button pastButton;
    Menu actionBar;
    ListView listView;
    List<Event> events;
    boolean future = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("");
        Toolbar toolBar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolBar);

        listView = (ListView) findViewById(R.id.eventList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                try {
                    Event event = (Event) adapterView.getItemAtPosition(position);
                    openEventViwer(event);
                } catch (java.lang.ArrayIndexOutOfBoundsException e) { //just in case
                }
            }
        });
        futureButton = (Button) findViewById(R.id.futureButton);
        pastButton = (Button) findViewById(R.id.pastButton);
        populateList();
    }

    public void populateList() {
        EventDatabase db = new EventDatabase(getApplicationContext());
        if (future) {
            events = db.getFutureEvents();
        } else {
            events = db.getPastEvents();
        }
        if (events == null) {
            return;
            //TODO do something if we deleted all events listView.
        }
        listView.setAdapter(new EventAdapter(this, events));
    }

    public void onResume() {
        super.onResume();
        fixButtonState();
        populateList();
        EventSynchronizer sync = new EventSynchronizer(this, this);
        sync.synchronize();
        populateList();
    }

    public void onLogInOutClicked(View view) {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            session.logOut();
            fixButtonState();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void fixButtonState() {
        if (username == null || button == null) return; //menu not yet initialized
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            username.setText(session.getUserName() + "  ");
            button.setText(R.string.logout);
        } else {
            username.setText("");
            button.setText(R.string.login);
        }
    }

    @Override
    public void onClick(View view) {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            session.logOut();
            fixButtonState();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setIcon(R.drawable.logo);
//        logo = new ImageView(this);
//        logo.setImageDrawable(getResources().getDrawable(R.drawable.logo));
//        menu.add(0, 0, 1, "").setActionView(logo).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        username = new TextView(this);

        username.setText("");
        username.setTextColor(getResources().getColor(R.color.black));
        //tv.setOnClickListener(this);
        username.setPadding(5, 0, 5, 0);
        username.setTypeface(null, Typeface.NORMAL);
        username.setTextSize(20);
        menu.add(0, 0, 1, "aaa").setActionView(username).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        button = new Button(this);
        button.setText("Logout");
        button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        button.setOnClickListener(this);
        menu.add(0, 0, 1, "Logout").setActionView(button).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        fixButtonState();
        return true;
    }

    public void onNewEventClicked(View view) {
        Intent intent = new Intent(this, EventViewerActivity.class);
        AppControl.eventStorage = new Event();
        startActivity(intent);
    }

    public void openEventViwer(Event event)  {
        Intent intent = new Intent(this, EventViewerActivity.class);
        AppControl.eventStorage = event;
        startActivity(intent);
    }

    @Override
    public void syncDone() {
        populateList();
        System.out.println("DONE");
    }

    public void onFutureClicked(View view) {
        if (future) return;
        futureButton.setEnabled(false);
        pastButton.setEnabled(true);
        future = true;
        populateList();
    }

    public void onPastClicked(View view) {
        if (!future) return;
        futureButton.setEnabled(true);
        pastButton.setEnabled(false);
        future = false;
        populateList();
    }


}
