package com.gacsoft.letsmeethere;
import android.app.Application;
import java.util.Calendar;
import java.util.Date;

import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Gacsoft on 9/4/2016.
 */
public class AppControl extends Application {
    private static AppControl ourInstance;
    public static final String APP_TAG = AppControl.class.getSimpleName();
    public static Event eventStorage;

    private RequestQueue requestQueue;

    public static synchronized AppControl getInstance() {
        return ourInstance;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        ourInstance = this;
        SessionManager.setContext(getApplicationContext());
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? APP_TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(APP_TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }

}
