package edu.stevens.cs522.chat.oneway.server.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import edu.stevens.cs522.chat.oneway.server.activities.PreferencesActivity;

/**
 * Created by Rafael on 3/13/2016.
 */
public class App {
    public static final String APP_NAMESPACE = "edu.stevens.cs522.chat.oneway.server";
    public static final String CAMERA_APP_PACKAGE = "edu.stevens.cs522.capture";
    public static final String CAMERA_APP_NAMESPACE = "edu.stevens.cs522.capture.client.CaptureActivity";
    public static final String EXTRA_SECURITY_DATABASE_KEY = "security_database_key";
    public static final String DATABASE_KEY_URI_PARAM = "database_key_uri_param";

    public static final String PREF_KEY_USERID = "userid";
    public static final String PREF_KEY_LAST_SEQNUM = "last_seq_num";
    public static final String PREF_KEY_CHATROOM = "chatroom";
    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_HOST = "host";
    public static final String PREF_KEY_REGISTRATION_ID = "reg_id";
    public static final String PREF_KEY_LATITUDE = "latitude";

    public static final String PREF_KEY_LONGITUDE = "longitude";
    final static public long PREF_DEFAULT_USER_ID = 0;
    final static public long PREF_DEFAULT_LAST_SEQNUM = 0;
    final static public String PREF_DEFAULT_CHATROOM = "_default";
    final static public String PREF_DEFAULT_USER_NAME = "no name";


    public static String DEFAULT_HOST = "";
    public static final String DEFAULT_ENCODING = "UTF-8";
}
