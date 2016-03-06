package edu.stevens.cs522.chat.oneway.server.activities;


import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import edu.stevens.cs522.chat.oneway.server.R;

public class PreferencesActivity extends PreferenceActivity{


    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_PORT = "port";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.preferences);
        addPreferencesFromResource(R.xml.preferences);


    }
}
