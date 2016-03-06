package edu.stevens.cs522.chat.oneway.server.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.adapters.ContactAdapter;
import edu.stevens.cs522.chat.oneway.server.adapters.MessageAdapter;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.managers.IQueryListener;
import edu.stevens.cs522.chat.oneway.server.managers.QueryBuilder;
import edu.stevens.cs522.chat.oneway.server.managers.TypedCursor;

public class ContactBookActivity extends Activity {

    private static final String TAG = ContactBookActivity.class.getCanonicalName();
//    private static final String PEER_DETAILS_KEY = TAG + "peer_details";
    private static final int DETAILS_REQUEST = 1;

    ListView contacts;
    TextView listEmptyView;
    ContactAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_book);

        contacts = (ListView) findViewById(R.id.contacts);
        listEmptyView = (TextView) findViewById(R.id.listEmptyView);
        listEmptyView.setVisibility(View.GONE);
        initList();


        QueryBuilder.executeQuery(TAG,
                this,
                PeerContract.CONTENT_URI,
                PeerContract.CURSOR_LOADER_ID,
                PeerContract.DEFAULT_ENTITY_CREATOR,
                new IQueryListener<Peer>() {

                    @Override
                    public void handleResults(TypedCursor<Peer> typedCursor) {
                        cursorAdapter.swapCursor(typedCursor.getCursor());
                        if (typedCursor.getCount() > 0) {
                            listEmptyView.setVisibility(View.GONE);
                        } else {
                            listEmptyView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void closeResults() {
                        cursorAdapter.swapCursor(null);
                    }
                });

    }


    public void initList() {

        this.cursorAdapter = new ContactAdapter(this, null);
        this.contacts.setAdapter(this.cursorAdapter);
        this.contacts.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        this.contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailsIntent = new Intent(getApplicationContext(), ContactDetailsActivity.class);
                Peer peer = new Peer((Cursor) cursorAdapter.getItem(position));
                detailsIntent.putExtra(ContactDetailsActivity.PEER_DETAILS_KEY, peer);
                startActivity(detailsIntent);
            }
        });



    }
}
