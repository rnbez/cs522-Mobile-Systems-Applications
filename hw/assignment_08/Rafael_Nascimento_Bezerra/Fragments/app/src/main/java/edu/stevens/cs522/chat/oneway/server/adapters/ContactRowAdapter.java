package edu.stevens.cs522.chat.oneway.server.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.utils.App;

/**
 * Created by Rafael on 2/23/2016.
 */
public class ContactRowAdapter extends ResourceCursorAdapter {
    protected final static int ROW_LAYOUT = android.R.layout.simple_list_item_2;

    public ContactRowAdapter(Context context, Cursor cursor) {
        super(context, ROW_LAYOUT, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cur, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.contact_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String userName = prefs.getString(App.PREF_KEY_USERNAME, App.PREF_DEFAULT_USER_NAME);
        TextView line = (TextView) view.findViewById(R.id.contactNameView);
        String text = PeerContract.getName(cursor);
        if(text.equalsIgnoreCase(userName)) text += " (me)";
        line.setText(text);
    }
}
