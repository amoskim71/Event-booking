package com.gacsoft.letsmeethere;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameEdit;
    private EditText emailEdit;
    private EditText passwordEdit;
    private EditText password2Edit; //password re-enter field
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle(R.string.registerTitle);

        progress = new ProgressDialog(this);
        progress.setCancelable(false);

        usernameEdit = (EditText)findViewById(R.id.register_username);
        emailEdit = (EditText)findViewById(R.id.register_email);
        passwordEdit = (EditText)findViewById(R.id.register_password);
        password2Edit = (EditText)findViewById(R.id.register_password2); //password re-enter field
    }

    public void onRegisterClicked(View view) {
        final String username = usernameEdit.getText().toString().trim();
        final String email = emailEdit.getText().toString();
        final String password = passwordEdit.getText().toString().trim();
        String password2 = password2Edit.getText().toString().trim(); //password re-enter field

        if (username.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.blankName), Toast.LENGTH_LONG).show();
            return;
        }
        if (email.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.blankEmail), Toast.LENGTH_LONG).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.blankPassword), Toast.LENGTH_LONG).show();
            return;
        }
        if (!password.equals(password2)) {
            Toast.makeText(getApplicationContext(), getString(R.string.passwordMismatch), Toast.LENGTH_LONG).show();
            return;
        }

        progress.setMessage(getString(R.string.registering));
        progress.show();

        String tag_req = "req_register";
        StringRequest req = new StringRequest(Request.Method.POST, AppConfig.REGISTER_URL,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        if (AppConfig.DEBUG) System.out.println(response);
                        JSONObject resp = new JSONObject(response);
                        boolean error = resp.getBoolean("error");
                        progress.hide();
                        if (!error) {
                            Toast.makeText(getApplicationContext(), getString(R.string.registerSuccess), Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
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
                        System.out.println("Error while registering: " + error.networkResponse +  error.getMessage());
                        Toast.makeText(getApplicationContext(), "Error while registering: " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
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
                params.put("name", username);
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

    public void toLoginClicked(View view) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
