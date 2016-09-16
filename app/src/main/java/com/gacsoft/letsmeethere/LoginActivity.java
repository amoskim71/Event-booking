package com.gacsoft.letsmeethere;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText emailEdit;
    EditText passwordEdit;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.loginTitle);

        progress = new ProgressDialog(this);
        progress.setCancelable(false);

        emailEdit = (EditText) findViewById(R.id.login_email);
        passwordEdit = (EditText) findViewById(R.id.login_password);
    }

    public void onLoginClicked(View view) {
        final String email = emailEdit.getText().toString();
        final String password = passwordEdit.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.blankEmail), Toast.LENGTH_LONG).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.blankPassword), Toast.LENGTH_LONG).show();
            return;
        }

        progress.setMessage("Logging in...");
        progress.show();

        String tag_req = "req_login";
        StringRequest req = new StringRequest(Request.Method.POST, AppConfig.LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {System.out.println(response);
                            JSONObject resp = new JSONObject(response);
                            boolean error = resp.getBoolean("error");
                            progress.hide();
                            if (!error) {

                                Toast.makeText(getApplicationContext(), getString(R.string.login_success), Toast.LENGTH_LONG).show();
                                SessionManager.getInstance().logIn(resp.getString("email"), resp.getString("name"), resp.getString("sessionID"));

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else { //error happened
                                String errorMessage = resp.getString("error_msg");
                                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (JSONException e) {
                            progress.hide();
                            e.printStackTrace(); //TODO ...
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.hide();
                if (error.networkResponse != null) {
                    System.out.println("Login error: " + error.networkResponse +  error.getMessage());
                    Toast.makeText(getApplicationContext(), "Login error: " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
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
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };
        AppControl.getInstance().addToRequestQueue(req, tag_req);
    }

    public void onStop() {
        super.onStop();
        progress.dismiss();
    }

    public void toRegisterClicked(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }
}
