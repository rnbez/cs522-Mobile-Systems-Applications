package edu.stevens.cs522.chat.oneway.server.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.List;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.managers.IQueryListener;
import edu.stevens.cs522.chat.oneway.server.managers.ISimpleQueryListener;
import edu.stevens.cs522.chat.oneway.server.managers.QueryBuilder;
import edu.stevens.cs522.chat.oneway.server.managers.SimpleQueryBuilder;
import edu.stevens.cs522.chat.oneway.server.managers.TypedCursor;

public class ContactDetailsActivity extends Activity {

    private static final String TAG = ContactDetailsActivity.class.getCanonicalName();
    public static final String PEER_DETAILS_KEY = TAG + "peer_details";

    TextView ipAddrView, nameView, portView;
    ListView messagesListView;

    Peer contact;
    SimpleCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_details);

        Intent callingIntent = getIntent();
        if (callingIntent.hasExtra(PEER_DETAILS_KEY)) {
            contact = (Peer) callingIntent.getParcelableExtra(PEER_DETAILS_KEY);
        }

        ipAddrView = (TextView) findViewById(R.id.contact_details_ip_addr_view);
        nameView = (TextView) findViewById(R.id.contact_details_name_view);
        portView = (TextView) findViewById(R.id.contact_details_port_view);
        messagesListView = (ListView) findViewById(R.id.contact_details_messages_listview);

        ipAddrView.setText(contact.getAddress().toString());
        nameView.setText(contact.getName());
        portView.setText(contact.getPort());

        cursorAdapter = new SimpleCursorAdapter(this,
                R.layout.simple_list_row,
                null,
                new String[]{ MessageContract.MESSAGE_TEXT },
                new int[]{ R.id.simple_list_row_textview },
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        messagesListView.setAdapter(this.cursorAdapter);

        Uri uri = PeerContract.withExtendedPath(contact.getId());
        uri = PeerContract.withExtendedPath(uri, "messages");

        QueryBuilder.executeQuery(TAG,
                this,
                MessageContract.CONTENT_URI,
                MessageContract.CURSOR_LOADER_ID,
                MessageContract.DEFAULT_ENTITY_CREATOR,
                new IQueryListener<Message>() {
                    @Override
                    public void handleResults(TypedCursor<Message> cursor) {
                        cursorAdapter.swapCursor(cursor.getCursor());
                    }

                    @Override
                    public void closeResults() {
                        cursorAdapter.swapCursor(null);
                    }
                });
    }
}
