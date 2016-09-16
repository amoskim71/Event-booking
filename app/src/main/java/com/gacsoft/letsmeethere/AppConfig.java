package com.gacsoft.letsmeethere;

/**
 * Created by Gacsoft on 9/5/2016.
 */
public class AppConfig {
    private static final String SERVER_HOST = "http://192.168.1.141:81";
    private static final String APP_PATH = "/android_login_api/";
    public static final String REGISTER_URL = SERVER_HOST + APP_PATH + "register.php";
    public static final String LOGIN_URL = SERVER_HOST + APP_PATH + "login.php";
    public static final String NEWEVENT_URL = SERVER_HOST + APP_PATH + "newevent.php";
    public static final String UPDATE_URL = SERVER_HOST + APP_PATH + "update.php";
    public static final String SYNC_URL = SERVER_HOST + APP_PATH + "fullsync.php";
    public static final String INVITE_URL = SERVER_HOST + APP_PATH + "invite.php";

    public static final String PREFS_FILE = "LMHSavedPrefs";
    public static final String LOGGED_IN = "isLoggedIn";
    public static final String SESSION_ID = "sessionID";
    public static final String SAVED_EMAIL = "email";
    public static final String SAVED_NAME = "name";

    public static boolean DEBUG = true;

}
