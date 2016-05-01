package edu.stevens.cs522.chat.oneway.server.activities.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import edu.stevens.cs522.chat.oneway.server.utils.App;

/**
 * Created by Rafael on 5/1/2016.
 */
public class BaseFragmentActivity extends FragmentActivity {
    private char[] databaseKey;

    public final char[] getDatabaseKey() {
        return databaseKey;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        databaseKey = getIntent().getCharArrayExtra(App.SECURITY_DATABASE_KEY);
        databaseKey = "1234".toCharArray();
        if (databaseKey == null){
            throw new IllegalArgumentException("SECURITY_DATABASE_KEY...");
        }
    }
}
