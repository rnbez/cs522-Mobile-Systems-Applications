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
import edu.stevens.cs522.chat.oneway.server.utils.App;

/**
 * Created by Rafael on 2/23/2016.
 */
public class MessageRowAdapter extends ResourceCursorAdapter {
    protected final static int ROW_LAYOUT = android.R.layout.simple_list_item_2;
    private String userName;

    public MessageRowAdapter(Context context, Cursor cursor) {
        super(context, ROW_LAYOUT, cursor, 0);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        userName = prefs.getString(App.PREF_KEY_USERNAME, App.PREF_DEFAULT_USER_NAME);
    }

    @Override
    public View newView(Context context, Cursor cur, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.message_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView contentView = (TextView) view.findViewById(R.id.msg_row_content);
        TextView senderView = (TextView) view.findViewById(R.id.msg_row_sender);
        TextView statusView = (TextView) view.findViewById(R.id.msg_row_status);
        contentView.setText(MessageContract.getMessage(cursor));

        String sender = MessageContract.getSender(cursor);
        if(sender.equals(userName)) {
            senderView.setText("me");
            long id = MessageContract.getSequentialNumber(cursor);
            if(id > 0) {
                statusView.setText("sent");
            }
            else {
                statusView.setText("pending");
            }
        }
        else{
            senderView.setText(MessageContract.getSender(cursor));
            statusView.setText("");
        }


    }
}
