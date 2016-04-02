package edu.stevens.cs522.chat.oneway.server.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;

/**
 * Created by Rafael on 2/23/2016.
 */
public class MessageAdapter extends ResourceCursorAdapter {
    protected final static int ROW_LAYOUT = android.R.layout.simple_list_item_2;

    public MessageAdapter(Context context, Cursor cursor) {
        super(context, ROW_LAYOUT, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cur, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.message_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView msgView = (TextView) view.findViewById(R.id.msgView);
        TextView senderView = (TextView) view.findViewById(R.id.senderView);
        msgView.setText(MessageContract.getMessage(cursor));
//        senderView.setText(MessageContract.getSender(cursor));
        long id = MessageContract.getId(cursor);
        if(id > 0) {
            senderView.setText("sent");
        }
        else {
            senderView.setText("pending");
        }
    }
}
