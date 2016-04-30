package edu.stevens.cs522.chat.oneway.server.activities;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import edu.stevens.cs522.chat.oneway.server.R;

public class PreferencesActivity extends PreferenceActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.preferences);
        addPreferencesFromResource(R.xml.preferences);

//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putString("Name","Harneet");
//        editor.apply();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        EditTextPreference edtPref = (EditTextPreference) pref;
        pref.setSummary(edtPref.getText());
//        if (pref instanceof ListPreference) {
//            ListPreference listPref = (ListPreference) pref;
//            pref.setSummary(listPref.getEntry());
//        }
    }
}
