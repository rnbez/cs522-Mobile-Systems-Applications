package edu.stevens.cs522.chat.oneway.server.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.contracts.ChatroomContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;

/**
 * Created by Rafael on 2/23/2016.
 */
public class ChatroomRowAdapter extends ResourceCursorAdapter {
    protected final static int ROW_LAYOUT = android.R.layout.simple_list_item_2;

    public ChatroomRowAdapter(Context context, Cursor cursor) {
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
        TextView line = (TextView) view.findViewById(R.id.contactNameView);
        line.setText(ChatroomContract.getName(cursor));
    }
}
