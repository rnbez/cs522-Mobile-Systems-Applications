package edu.stevens.cs522.chat.oneway.server.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.entities.Message;

/**
 * Created by Rafael on 2/12/2016.
 */
public class ListAdapter extends ArrayAdapter<Message> {
    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public ListAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Message item = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(super.getContext()).inflate(R.layout.message, parent, false);
        }
        TextView msgView = (TextView) convertView.findViewById(R.id.msgView);
        TextView authorView = (TextView) convertView.findViewById(R.id.authorView);
        msgView.setText(item.getMessageText());
        authorView.setText(item.getSender());
        return convertView;
    }
}
