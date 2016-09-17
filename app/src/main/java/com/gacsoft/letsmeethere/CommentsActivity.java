package com.gacsoft.letsmeethere;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {
    EditText nameEdit;
    EditText dateEdit;
    EditText timeEdit;
    ProgressDialog progress;

    TextView post;
    Event event;
    Comment comment;
    List<Comment> comments;
    ListView commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        nameEdit = (EditText) findViewById(R.id.edit_Event_Name);
        dateEdit = (EditText) findViewById(R.id.edit_Event_Date);
        timeEdit = (EditText) findViewById(R.id.edit_Event_Time);
        commentList = (ListView) findViewById(R.id.commentsList);
        post = (TextView) findViewById(R.id.post);
        event = AppControl.eventStorage;
        progress = new ProgressDialog(this);
        progress.setCancelable(false);

        loadFields();
        loadComments();    }

    private void loadFields() {
        nameEdit.setText(event.getName());
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        dateEdit.setText(df.format(event.getWhen()));
        timeEdit.setText(android.text.format.DateFormat.format("kk:mm", event.getWhen()));
    }

    private void loadComments() {
        EventDatabase db = new EventDatabase(this);
        comments = db.getComments(event.getId());
        commentList.setAdapter(new CommentAdapter(this, comments));
    }

    public void onPostClicked(View view) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            Toast.makeText(getApplicationContext(), R.string.postOffline, Toast.LENGTH_LONG).show();
        } else { //logged in
            progress.setMessage(getString(R.string.sendingComment));
            progress.show();
            comment = new Comment(
                    event.getId(),
                    event.getName(),
                    SessionManager.getInstance().getEmail(),
                    Calendar.getInstance().getTime(),
                    post.getText().toString()
            );

            String tag_req = "req_sendComment";
            StringRequest req = new StringRequest(Request.Method.POST, AppConfig.COMMENT_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                System.out.println(response);
                                JSONObject resp = new JSONObject(response);

                                boolean error = resp.getBoolean("error");
                                progress.hide();
                                if (!error) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.sentComment), Toast.LENGTH_LONG).show();
                                    EventDatabase db = new EventDatabase(getApplicationContext());
                                    db.addComment(comment);
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
                    params.put("post", comment.getPost());
                    System.out.println(params);
                    return params;
                }
            };
            AppControl.getInstance().addToRequestQueue(req, tag_req);
        }
    }

    public void onEventClicked(View view) {
        Intent intent = new Intent(this, EventViewerActivity.class);
        startActivity(intent);
        finish();
    }
}
