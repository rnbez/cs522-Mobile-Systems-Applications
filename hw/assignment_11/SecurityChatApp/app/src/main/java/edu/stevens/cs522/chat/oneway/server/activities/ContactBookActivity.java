package edu.stevens.cs522.chat.oneway.server.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.activities.fragments.ContactDetailsFragment;
import edu.stevens.cs522.chat.oneway.server.activities.fragments.ContactListFragment;
import edu.stevens.cs522.chat.oneway.server.adapters.ContactRowAdapter;
import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.managers.IQueryListener;
import edu.stevens.cs522.chat.oneway.server.managers.ISimpleQueryListener;
import edu.stevens.cs522.chat.oneway.server.managers.QueryBuilder;
import edu.stevens.cs522.chat.oneway.server.managers.SimpleQueryBuilder;
import edu.stevens.cs522.chat.oneway.server.managers.TypedCursor;
import edu.stevens.cs522.chat.oneway.server.utils.App;

public class ContactBookActivity
        extends FragmentActivity
        implements ContactListFragment.IContactListFragmentListener,
        ContactDetailsFragment.IContactDetailsFragmentListener {

    private static final String TAG = ContactBookActivity.class.getCanonicalName();
    //    private static final String PEER_DETAILS_KEY = TAG + "peer_details";
    private static final int DETAILS_REQUEST = 1;
    private static final int MAPS_REQUEST = DETAILS_REQUEST + 1;


    FragmentManager fragmentManager;
    ContactListFragment contactListFragment;
    ContactDetailsFragment contactDetailsFragment;
    int orientation;
    ContactRowAdapter conctactListCursorAdapter;
    Cursor conctactListCursor;
    List<Message> contactDetailsMessageList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layt__contact_book);

        this.fragmentManager = getSupportFragmentManager();
        this.orientation = getResources().getConfiguration().orientation;

        this.conctactListCursorAdapter = new ContactRowAdapter(this, null);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contacts_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chat_menu_maps:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                long userId = sharedPreferences.getLong(App.PREF_KEY_USERID, App.PREF_DEFAULT_USER_ID);
                double userLatitude = (double) sharedPreferences.getFloat(App.PREF_KEY_LATITUDE, 0);
                double userLongitude = (double) sharedPreferences.getFloat(App.PREF_KEY_LONGITUDE, 0);
//                startActivityForResult(new Intent(this, PreferencesActivity.class), MAPS_REQUEST);
//                Intent intent = getPackageManager().getLaunchIntentForPackage("com.chat_maps.chatmaps");
                Intent intent = new Intent();
                intent.setAction("com.chat_maps.chatmaps.SHOW_PEERS");
                intent.setType("*/*");
                intent.putExtra("com.chat_maps.chatmaps.EXTRA_USERID", userId);
                intent.putExtra("com.chat_maps.chatmaps.EXTRA_LATITUDE", userLatitude);
                intent.putExtra("com.chat_maps.chatmaps.EXTRA_LONGITUDE", userLongitude);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
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
        
        SimpleQueryBuilder.executeQuery(this,
                PeerContract.getMessagesUri(peer.getId()),
                MessageContract.DEFAULT_ENTITY_CREATOR,
                new ISimpleQueryListener<Message>() {
                    @Override
                    public void handleResults(List<Message> results) {
                        contactDetailsMessageList = results;
                        contactDetailsFragment.setMessageList(contactDetailsMessageList);
                    }
                });

    }

    @Override
    public Cursor getContactListCursor() {
        return conctactListCursorAdapter.getCursor();
    }

    @Override
    public void getContactMessagesAsync(long peerId) {
    }
}
