package com.gacsoft.letsmeethere;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInstaller;

/**
 * Created by Gacsoft on 9/4/2016.
 */
public class SessionManager {

    private static SharedPreferences prefs;
    private static Editor editor;
    //private static Context _context;
    private static SessionManager instance;

    private SessionManager() {}

    public static void setContext(Context context) {
        //_context = context;
        prefs = context.getSharedPreferences(AppConfig.PREFS_FILE, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void logIn(String email, String name, String sessionID) {
        editor.putBoolean(AppConfig.LOGGED_IN, true);
        editor.putString(AppConfig.SAVED_EMAIL, email);
        editor.putString(AppConfig.SAVED_NAME, name);
        editor.putString(AppConfig.SESSION_ID, sessionID);
        editor.commit();
    }

    public void logOut() {
        editor.putBoolean(AppConfig.LOGGED_IN, false);
        editor.putString(AppConfig.SESSION_ID, "");
        editor.commit();
    }

    public boolean isLoggedIn(){
        return prefs.getBoolean(AppConfig.LOGGED_IN, false);
    }

    public String getSessionID() { return prefs.getString(AppConfig.SESSION_ID, ""); }

    public String getUserName() { return prefs.getString(AppConfig.SAVED_NAME, ""); }

    public String getEmail() { return prefs.getString(AppConfig.SAVED_EMAIL, ""); }
}
