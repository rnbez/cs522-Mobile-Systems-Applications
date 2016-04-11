package edu.stevens.cs522.chat.oneway.server.activities;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

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

public class ContactBookActivity
        extends FragmentActivity
        implements ContactListFragment.IContactListFragmentListener,
        ContactDetailsFragment.IContactDetailsFragmentListener {

    private static final String TAG = ContactBookActivity.class.getCanonicalName();
    //    private static final String PEER_DETAILS_KEY = TAG + "peer_details";
    private static final int DETAILS_REQUEST = 1;


    FragmentManager fragmentManager;
    ContactListFragment contactListFragment;
    ContactDetailsFragment contactDetailsFragment;
    int orientation;
    ContactAdapter conctactListCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layt__contact_book);

        this.fragmentManager = getSupportFragmentManager();
        this.orientation = getResources().getConfiguration().orientation;

        this.conctactListCursorAdapter = new ContactAdapter(this, null);
        boolean hasSavedInstanceState = savedInstanceState != null;

        switch (this.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                if (findViewById(R.id.fragment_container) != null) {
                    contactListFragment = new ContactListFragment();
                    contactListFragment.setArguments(getIntent().getExtras());
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    if (!hasSavedInstanceState) {
                        transaction.add(R.id.fragment_container, contactListFragment);
                    } else {
                        transaction.replace(R.id.fragment_container, contactListFragment);
                    }
                    transaction.commit();
                }
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                if (fragmentManager.findFragmentById(R.id.layt__contact_book_contact_list) != null) {
                    contactListFragment = (ContactListFragment) fragmentManager.findFragmentById(R.id.layt__contact_book_contact_list);
                }
                if (findViewById(R.id.fragment_container) != null) {
                    contactDetailsFragment = new ContactDetailsFragment();
                    contactDetailsFragment.setArguments(getIntent().getExtras());
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    if (!hasSavedInstanceState) {
                        transaction.add(R.id.fragment_container, contactDetailsFragment);
                    } else {
                        transaction.replace(R.id.fragment_container, contactDetailsFragment);
                    }
                    transaction.commit();
                }
                break;
            default:
        }

        QueryBuilder.executeQuery(TAG,
                this,
                PeerContract.CONTENT_URI,
                PeerContract.CURSOR_LOADER_ID,
                PeerContract.DEFAULT_ENTITY_CREATOR,
                new IQueryListener<Peer>() {

                    @Override
                    public void handleResults(TypedCursor<Peer> typedCursor) {
                        conctactListCursorAdapter.swapCursor(typedCursor.getCursor());
                        contactListFragment.setListCursor(typedCursor.getCursor());
                    }

                    @Override
                    public void closeResults() {
                        conctactListCursorAdapter.swapCursor(null);
                        contactListFragment.setListCursor(null);
                    }
                });
    }


    @Override
    public void showContactDetails(Peer peer) {
        switch (this.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                if (findViewById(R.id.fragment_container) != null) {
                    contactDetailsFragment = new ContactDetailsFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(ContactDetailsFragment.PEER_DETAILS_KEY, peer);
                    contactDetailsFragment.setArguments(args);

                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.fragment_container, contactDetailsFragment);
                    transaction.addToBackStack(TAG + "contact_details_fragment");
                    transaction.commit();
                }
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                if (findViewById(R.id.fragment_container) != null) {
                    contactDetailsFragment = new ContactDetailsFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(ContactDetailsFragment.PEER_DETAILS_KEY, peer);
                    contactDetailsFragment.setArguments(args);

                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.fragment_container, contactDetailsFragment);
//                    transaction.addToBackStack(TAG + "contact_details_fragment");
                    transaction.commit();
                }
                break;
            default:
        }


    }

    @Override
    public void getContactListAsync() {
        contactListFragment.setListCursor(conctactListCursorAdapter.getCursor());
    }

    @Override
    public void getContactMessagesAsync(long peerId) {
//        Uri uri = PeerContract.withExtendedPath(peerId);
//        uri = PeerContract.withExtendedPath(uri, "messages");
//        SimpleQueryBuilder.executeQuery(this,
//                uri,
//                MessageContract.DEFAULT_ENTITY_CREATOR,
//                new ISimpleQueryListener<Message>() {
//                    @Override
//                    public void handleResults(List<Message> results) {
//                        if (contactDetailsFragment != null){
//                            contactDetailsFragment.setMessageList(results);
//                        }
//                    }
//                });
    }
}
