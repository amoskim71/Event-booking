package com.gacsoft.letsmeethere;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.app.LoaderManager;
import android.content.Loader;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class InviteActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
            AdapterView.OnItemClickListener{

    Cursor cursor;
    /*
     * Defines an array that contains column names to move from
     * the Cursor to the ListView.
     */
    @SuppressLint("InlinedApi")
    private final static String[] FROM_COLUMNS = {
            Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                    ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Email.DATA
    };
    /*
     * Defines an array that contains resource ids for the layout views
     * that get the Cursor column contents. The id is pre-defined in
     * the Android framework, so it is prefaced with "android.R.id"
     */
    private final static int[] TO_IDS = {
            R.id.contactName,
            R.id.contactEmail
    };

    ListView mContactsList;
    // Define variables for the contact the user selects
    // The contact's _ID value
    long mContactId;
    // The contact's LOOKUP_KEY
    String mContactKey;
    // A content URI for the selected contact
    Uri mContactUri;
    // An adapter that binds the result Cursor to the ListView
    private SimpleCursorAdapter mCursorAdapter;

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION =
            {
                    ContactsContract.RawContacts._ID,
                    Build.VERSION.SDK_INT
                            >= Build.VERSION_CODES.HONEYCOMB ?
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                            ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Email.DATA

            };

    @SuppressLint("InlinedApi")
    private static final String SELECTION =
            ContactsContract.CommonDataKinds.Email.DATA + " <> ''"; //only contacts with non-blank email

    String order = "CASE WHEN "
            + ContactsContract.Contacts.DISPLAY_NAME
            + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
            + ContactsContract.Contacts.DISPLAY_NAME
            + ", "
            + ContactsContract.CommonDataKinds.Email.DATA
            + " COLLATE NOCASE";

    List<String> invited;
    ListView inviteList;
    EditText inviteEdit;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        PermissionDialog.askPermission(this, Manifest.permission.READ_CONTACTS);
        invited = new ArrayList<String>();
        inviteList = (ListView) findViewById(R.id.invitelist);
        inviteEdit = (EditText) findViewById(R.id.emailEdit);
        inviteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                invited.remove(i);
                refreshList();
            }
        });
        inviteList.setAdapter(new ArrayAdapter<String>(this, R.layout.email_list_item, invited));

        mContactsList = (ListView) findViewById(R.id.contactlist);
        mCursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.contact_list_item,
                null,
                FROM_COLUMNS, TO_IDS,
                0);
        mContactsList.setAdapter(mCursorAdapter);
        mContactsList.setOnItemClickListener(this);
        getLoaderManager().initLoader(0, null, this);
        progress = new ProgressDialog(this);
        progress.setCancelable(false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                PROJECTION,
                SELECTION,
                null,//SelectionArgs
                order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        this.cursor = cursor;
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(
            AdapterView<?> parent, View item, int position, long rowID) {
        cursor.moveToPosition(position);
        inviteEdit.setText(cursor.getString(2));
    }

    public void addClicked(View view) {
        String email = inviteEdit.getText().toString().trim();
        if (!validateEmail(email)) {
            Toast.makeText(this, R.string.invalidEmail, Toast.LENGTH_LONG).show();
            return;
        }
        if (invited.contains(email)) {
            inviteEdit.setText("");
            return;
        }
        invited.add(email);
        inviteEdit.setText("");
        refreshList();
    }

    public void cancelClicked(View view) {
        finish();
    }

    public void refreshList() {
        inviteList.setAdapter(new ArrayAdapter<String>(this, R.layout.email_list_item, invited));
    }

    public boolean validateEmail(String email) {
        Pattern emailPattern = Pattern.compile(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                        "\\@" +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                        "(" +
                        "\\." +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                        ")+"
        );
        return emailPattern.matcher(email).matches();
    }

    public void inviteClicked(View view) {
        if (invited.isEmpty()) {
            Toast.makeText(this, getString(R.string.emptyInvite), Toast.LENGTH_LONG).show();
            return;
        }

        progress.setMessage(getString(R.string.sendingInvites));
        progress.show();

        String tag_req = "req_invite";
        StringRequest req = new StringRequest(Request.Method.POST, AppConfig.INVITE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            System.out.println(response);
                            JSONObject resp = new JSONObject(response);

                            boolean error = resp.getBoolean("error");
                            progress.hide();
                            if (!error) {
                                Toast.makeText(getApplication(), getString(R.string.inviteSent), Toast.LENGTH_LONG).show();
                                finish();
                            } else { //error happened
                                //TODO if server says session expired, prompt a new login
                                String errorMessage = resp.getString("error_msg");
                                if (errorMessage.equals("BAD_SESSION")) {
                                    Toast.makeText(getApplication(), R.string.BAD_SESSION, Toast.LENGTH_LONG).show();
                                    SessionManager.getInstance().logOut();
                                } else if (errorMessage.equals("BAD_EMAIL")) {
                                    Toast.makeText(getApplication(), R.string.BAD_EMAIL, Toast.LENGTH_LONG).show();
                                } else if (errorMessage.equals("BAD_PARAMS")) {
                                    Toast.makeText(getApplication(), R.string.BAD_PARAMS, Toast.LENGTH_LONG).show();
                                } else if (errorMessage.equals("BAD_OWNER")) {
                                    Toast.makeText(getApplication(), R.string.wrongOwner, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplication(), R.string.BAD_SOMETHING, Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        catch (JSONException e) {
                            progress.hide();
                            Toast.makeText(getApplication(), R.string.BAD_SOMETHING, Toast.LENGTH_LONG).show();
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
                String emails = TextUtils.join(",", invited);
                SessionManager session = SessionManager.getInstance();
                params.put("session", session.getSessionID());
                params.put("email", session.getEmail());
                params.put("eventID", AppControl.eventStorage.getId());
                params.put("invites", emails);
                System.out.println(params);
                return params;
            }
        };
        AppControl.getInstance().addToRequestQueue(req, tag_req);
    }

    public void onStop() {
        super.onStop();
        progress.dismiss();
    }
}
