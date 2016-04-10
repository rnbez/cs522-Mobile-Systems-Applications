package edu.stevens.cs522.chat.oneway.server.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.activities.fragments.ContactDetailsFragment;
import edu.stevens.cs522.chat.oneway.server.activities.fragments.ContactListFragment;
import edu.stevens.cs522.chat.oneway.server.adapters.ContactAdapter;
import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.managers.IQueryListener;
import edu.stevens.cs522.chat.oneway.server.managers.ISimpleQueryListener;
import edu.stevens.cs522.chat.oneway.server.managers.QueryBuilder;
import edu.stevens.cs522.chat.oneway.server.managers.SimpleQueryBuilder;
import edu.stevens.cs522.chat.oneway.server.managers.TypedCursor;

public class ContactBookActivity extends FragmentActivity
        implements ContactListFragment.IContactListFragmentListener {

    private static final String TAG = ContactBookActivity.class.getCanonicalName();
    //    private static final String PEER_DETAILS_KEY = TAG + "peer_details";
    private static final int DETAILS_REQUEST = 1;


    FragmentManager fragmentManager;
    ContactListFragment contactListFragment;
    ContactDetailsFragment contactDetailsFragment;
    int orientation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layt__contacts);

        fragmentManager = getSupportFragmentManager();

        this.orientation = getResources().getConfiguration().orientation;
        switch (this.orientation){
            case Configuration.ORIENTATION_PORTRAIT:
                // Check that the activity is using the layout version with
                // the fragment_container FrameLayout
                if (findViewById(R.id.fragment_container) != null) {

                    // However, if we're being restored from a previous state,
                    // then we don't need to do anything and should return or else
                    // we could end up with overlapping fragments.
                    if (savedInstanceState != null) {
                        return;
                    }

                    // Create a new Fragment to be placed in the activity layout
                    contactListFragment = new ContactListFragment();

                    // In case this activity was started with special instructions from an
                    // Intent, pass the Intent's extras to the fragment as arguments
                    contactListFragment.setArguments(getIntent().getExtras());

                    // Add the fragment to the 'fragment_container' FrameLayout
                    fragmentManager
                            .beginTransaction()
                            .add(R.id.fragment_container, contactListFragment)
                            .commit();
                }
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                if(fragmentManager.findFragmentById(R.id.layt__contact_book_contact_list) != null) {
                    contactListFragment = (ContactListFragment) fragmentManager.findFragmentById(R.id.layt__contact_book_contact_list);
                }
                if (findViewById(R.id.fragment_container) != null) {
                    if (savedInstanceState != null) {
                        return;
                    }
                    contactDetailsFragment = new ContactDetailsFragment();
                    contactDetailsFragment.setArguments(getIntent().getExtras());
                    fragmentManager
                            .beginTransaction()
                            .add(R.id.fragment_container, contactDetailsFragment)
                            .commit();
                }
                break;
            default:
        }



//        contactListFragment = (ContactListFragment) getSupportFragmentManager().findFragmentById(R.id.layt__contact_book_contact_list);
//        contactDetailsFragment = (ContactDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.layt__contact_book_contact_details);

//        contacts = (ListView) findViewById(R.id.contacts);
//        listEmptyView = (TextView) findViewById(R.id.listEmptyView);
//        listEmptyView.setVisibility(View.GONE);
//        initList();
//

        QueryBuilder.executeQuery(TAG,
                this,
                PeerContract.CONTENT_URI,
                PeerContract.CURSOR_LOADER_ID,
                PeerContract.DEFAULT_ENTITY_CREATOR,
                new IQueryListener<Peer>() {

                    @Override
                    public void handleResults(TypedCursor<Peer> typedCursor) {
                        contactListFragment.setListCursor(typedCursor.getCursor());
//                        cursorAdapter.swapCursor(typedCursor.getCursor());
//                        if (typedCursor.getCount() > 0) {
//                            listEmptyView.setVisibility(View.GONE);
//                        } else {
//                            listEmptyView.setVisibility(View.VISIBLE);
//                        }
                    }

                    @Override
                    public void closeResults() {
//                        cursorAdapter.swapCursor(null);
                        contactListFragment.setListCursor(null);
                    }
                });

    }

    @Override
    public void showContactDetails(Peer peer) {
        // Create fragment and give it an argument specifying the article it should show
        contactDetailsFragment = new ContactDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ContactDetailsFragment.PEER_DETAILS_KEY, peer);
        contactDetailsFragment.setArguments(args);

        FragmentTransaction transaction = fragmentManager.beginTransaction();

// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, contactDetailsFragment);
        transaction.addToBackStack(TAG + "contact_details_fragment");

// Commit the transaction
        transaction.commit();

        Uri uri = PeerContract.withExtendedPath(peer.getId());
        uri = PeerContract.withExtendedPath(uri, "messages");
        SimpleQueryBuilder.executeQuery(this,
                uri,
                MessageContract.DEFAULT_ENTITY_CREATOR,
                new ISimpleQueryListener<Message>() {
                    @Override
                    public void handleResults(List<Message> results) {
                        if (contactDetailsFragment != null){
                            contactDetailsFragment.setMessageList(results);
                        }
                    }
                });
    }


//    public void initList() {
//
//        this.cursorAdapter = new ContactAdapter(this, null);
//        this.contacts.setAdapter(this.cursorAdapter);
//        this.contacts.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
//
//        this.contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent detailsIntent = new Intent(getApplicationContext(), ContactDetailsActivity.class);
//                Peer peer = new Peer((Cursor) cursorAdapter.getItem(position));
//                detailsIntent.putExtra(ContactDetailsActivity.PEER_DETAILS_KEY, peer);
//                startActivity(detailsIntent);
//            }
//        });
//
//
//
//    }
}
